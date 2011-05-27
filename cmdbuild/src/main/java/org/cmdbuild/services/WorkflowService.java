package org.cmdbuild.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.xml.security.utils.Base64;
import org.cmdbuild.config.WorkflowProperties;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CachedSharkWSFactory;
import org.cmdbuild.workflow.SharkWSFacade;
import org.cmdbuild.workflow.WorkflowCache;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.client.utilities.SharkWSFactory;

public class WorkflowService {

	/**
	 * Marker interface
	 */
	public interface WorkflowOperation<T> {
		T execute(WMSessionHandle handle, SharkWSFactory factory, UserContext userCtx) throws Exception;
	}

	SharkWSFactory factory;
	WAPI wapi;
	WMConnectInfo connectInfoAdmin;
	WMConnectInfo connectInfoTempl;
	String sharkEndpoint;

	String base64authentication;
	private String sharkWSUser;
	private String sharkWSPassword;

	List<Lookup> statuses;

	boolean enabled = false;

	Boolean configured = false;

	Set<SharkWSFacade.SharkEventListener> sharkEventListeners;

	private static WorkflowService instance = null;

	public static WorkflowService getInstance() {
		if (instance == null) {
			instance = new WorkflowService();
		}
		return instance;
	}

	private WorkflowService() {
	}

	public void configure() {
		WorkflowProperties props = WorkflowProperties.getInstance();
		this.configured = true;
		this.enabled = props.isEnabled();
		if (enabled) {
			sharkEndpoint = props.getEndpoint();
			factory = new CachedSharkWSFactory(sharkEndpoint);
			sharkEventListeners = new HashSet<SharkWSFacade.SharkEventListener>();
			initAdminConnectionInfo(props);
			initConnectionInfoTemplate(props);
			initWAPIConnection();
		}
		Log.WORKFLOW.info("Workflow service is: " + (enabled ? "enabled" : "disabled"));
	}

	private void initConnectionInfoTemplate(WorkflowProperties props) {
		connectInfoTempl = new WMConnectInfo();
		connectInfoTempl.setEngineName(props.getEngine());
		connectInfoTempl.setScope(props.getScope());
	}

	private void initAdminConnectionInfo(WorkflowProperties props) {
		connectInfoAdmin = new WMConnectInfo(props.getUser(), props.getPassword(), props.getEngine(), props.getScope());
	}

	private void initWAPIConnection() {
		try {
			wapi = factory.getWAPIConnection();
		} catch (Exception e) {
			Log.OTHER.error("cannot get WAPI connection!");
			throw WorkflowExceptionType.WF_WAPI_CONNECTION_ERROR.createException();
		}
	}

	public void setBase64Authentication(String user, String password) {
		sharkWSUser = user;
		sharkWSPassword = password;
		String toEnc = user + ":" + password;
		base64authentication = Base64.encode(toEnc.getBytes());
	}

	public String getSharkWSUser() {
		return sharkWSUser;
	}

	public String getSharkWSPassword() {
		return sharkWSPassword;
	}

	public WAPI getWapi() {
		if (wapi == null) {
			checkConfigured();
			try {
				wapi = factory.getWAPIConnection();
			} catch (Exception e) {
				throw WorkflowExceptionType.WF_WAPI_CONNECTION_ERROR.createException();
			}
		}
		return wapi;
	}

	/**
	 * Execute the WorkflowOperation with ADMINISTRATION connection
	 * 
	 * @param <T>
	 * @param operation
	 * @param currentUser
	 * @param currentRole
	 * @return
	 * @throws Exception
	 */
	public <T> T executeAdmin(WorkflowOperation<T> operation) throws Exception {
		return execute(operation, null, true);
	}

	/**
	 * Execute the WorkflowOperation with currentUser credential connection
	 * 
	 * @param <T>
	 * @param operation
	 * @param currentUser
	 * @param currentRole
	 * @return
	 * @throws Exception
	 */
	public <T> T execute(WorkflowOperation<T> operation, UserContext userCtx) throws Exception {
		return execute(operation, userCtx, false);
	}

	/**
	 * Execute the WorkflowOperation with
	 * (useAdminConnection?Administratio:currentUser) connection
	 * 
	 * @param <T>
	 * @param operation
	 * @param currentUser
	 * @param currentRole
	 * @param useAdminConnection
	 * @return
	 * @throws Exception
	 */
	public <T> T execute(WorkflowOperation<T> operation, UserContext userCtx, boolean useAdminConnection)
			throws Exception {
		checkConfigured();

		WMSessionHandle handle = null;
		Log.WORKFLOW.debug("obtain session handle...");
		if (useAdminConnection) {
			handle = getWapi().connect(connectInfoAdmin);
		} else {
			WorkflowProperties props = WorkflowProperties.getInstance();
			String adminUser = props.getUser();
			String adminPassword = props.getPassword();
			String connectionGroup = userCtx.getDefaultGroup().getName(); // !!!!!!!!!!!!!!!
			WMConnectInfo connInfo = new WMConnectInfo(adminUser + "@" + connectionGroup,
					adminPassword, connectInfoTempl.getEngineName(), connectInfoTempl.getScope());
			handle = getWapi().connect(connInfo);
		}
		try {
			Log.WORKFLOW.debug("call to execute");
			return operation.execute(handle, factory, userCtx);
		} catch (Exception e) {
			Log.WORKFLOW.debug("exception in execute method call");
			throw e;
		} finally {
			if (handle != null) {
				Log.WORKFLOW.debug("disconnect from workflow engine");
				factory.getWAPIConnection().disconnect(handle);
			}
		}
	}

	private void checkConfigured() {
		synchronized (this.configured) {
			if (!this.configured) {
				configure();
			}
		}
	}

	public void reloadCache() {
		WorkflowCache.reload();
	}

	public SharkWSFactory getFactory() {
		return factory;
	}

	public boolean isEnabled() {
		this.checkConfigured();
		return enabled;
	}

	public List<Lookup> getStatusesLookup() {
		if (statuses == null) {
			statuses = new ArrayList<Lookup>();
			for (Lookup lkp : SchemaCache.getInstance().getLookupList(ProcessAttributes.FlowStatus.toString(), null)) {
				statuses.add(lkp);
			}
		}
		return statuses;
	}

	public Lookup getStatusLookupFor(String status) {
		// prevent old "open" state search
		if ("open".equalsIgnoreCase(status)) {
			status = SharkConstants.STATE_OPEN_RUNNING;
		}
		for (Lookup lkp : this.getStatusesLookup()) {
			if (status.equalsIgnoreCase(lkp.getCode()))
				return lkp;
		}
		return null;
	}

	public String resolveArbitraryPerformerExpression(String procInstId, String actInstId, String expr) {

		HttpClient client;
		client = new HttpClient();
		GetMethod method = new GetMethod(this.sharkEndpoint + "/ActivityPerformerResolver");

		setAuthAndQS(method, new NameValuePair("processinstanceid", procInstId), new NameValuePair(
				"activityinstanceid", actInstId), new NameValuePair("expression", expr));

		int status;
		if (200 == (status = execute(client, method))) {
			try {
				return method.getResponseBodyAsString();
			} catch (Exception e) {
				// ignored
				Log.WORKFLOW.error("cannot get resolve arbitrary performer expression", e);
			}
		} else {
			Log.WORKFLOW.error("request status: " + status + " to: " + this.sharkEndpoint
					+ "/ActivityPerformerResolver");
		}

		return null;
	}

	private int execute(HttpClient client, HttpMethodBase method) {
		try {
			return client.executeMethod(method);
		} catch (Exception e) {
			// Not handled with exceptions
			Log.WORKFLOW.warn("Cannot execute HTTP method " + method.getName());
		}
		return -1;
	}

	private void addAuth(HttpMethodBase method) {
		Header auth = new Header("Authorization", "Basic " + this.base64authentication);
		method.addRequestHeader(auth);
	}

	private void setQueryString(HttpMethodBase method, NameValuePair... params) {
		method.setQueryString(params);
	}

	private void setAuthAndQS(HttpMethodBase method, NameValuePair... params) {
		addAuth(method);
		setQueryString(method, params);
	}

	public Set<SharkWSFacade.SharkEventListener> getSharkEventListeners() {
		return sharkEventListeners;
	}

	public synchronized void sendSharkEvent(SharkWSFacade.SharkEvent evtType, UserContext userCtx,
			WMSessionHandle handle, SharkWSFacade facade, Object... vars) {
		Set<SharkWSFacade.SharkEventListener> toRemove = new HashSet<SharkWSFacade.SharkEventListener>();
		for (SharkWSFacade.SharkEventListener listener : this.sharkEventListeners) {
			listener.handle(evtType, userCtx, facade, handle, factory, this, vars);
			if (listener instanceof SharkWSFacade.OneShotSharkEventListener) {
				toRemove.add(listener);
			}
		}

		this.sharkEventListeners.removeAll(toRemove);
	}

	public synchronized void addSharkEventListener(SharkWSFacade.SharkEventListener listener) {
		if (enabled) {
			this.sharkEventListeners.add(listener);
		}
	}

	public synchronized void removeSharkEventListener(SharkWSFacade.SharkEventListener listener) {
		if (enabled) {
			this.sharkEventListeners.remove(listener);
		}
	}
}

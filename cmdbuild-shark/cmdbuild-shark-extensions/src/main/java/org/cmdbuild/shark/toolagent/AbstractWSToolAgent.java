package org.cmdbuild.shark.toolagent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.Lookup;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.shark.util.ClientPasswordCallback;
import org.cmdbuild.shark.util.CmdbuildUtils;
import org.cmdbuild.shark.util.CmdbuildUtils.CmdbuildAttributeStruct;
import org.cmdbuild.shark.util.CmdbuildUtils.CmdbuildTableStruct;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;

public abstract class AbstractWSToolAgent extends AbstractConditionalToolAgent {

	public final static String SYSTEM_USER = "system";
	public static final String PASSWORD_ATTRIBUTE = "CMDBuild.EndPoint.Password";
	public static final String USER_ATTRIBUTE = "CMDBuild.EndPoint.User";
	public static final String CMDBUILD_ENDPOINT = "CMDBuild.WS.EndPoint";

	protected void innerInvoke(final WMSessionHandle shandle, final long handle, final WMEntity appInfo,
			final WMEntity toolInfo, final String applicationName, final String procInstId, final String assId,
			final AppParameter[] parameters, final Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {
		// WARNING applicationName is not the name of the application (tool)!

		final String toolInfoID = this.toolInfo.getId();

		try {
			final JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
			proxyFactory.setServiceClass(Private.class);
			proxyFactory.setAddress(getEndpoint());
			final Private stub = (Private) proxyFactory.create();

			// if(config == null) {
			// HttpClient client = CmdbuildUtils.getHttpClient();
			// config =
			// ConfigurationContextFactory.createConfigurationContextFromFileSystem(configPath,null);
			// config.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, client);
			// }
			// T stub = createServiceStub(config);

			// do authentication here
			final Map<String, Object> outProps = new HashMap<String, Object>();
			final Client client = ClientProxy.getClient(stub);
			final Endpoint cxfEndpoint = client.getEndpoint();

			// Manual WSS4JOutInterceptor interceptor process
			outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
			outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);

			final WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
			cxfEndpoint.getOutInterceptors().add(wssOut);

			addPerformerWSUserPassword(stub, outProps);

			invokeWebService(stub, parameters, toolInfoID);
		} catch (final Exception e) {
			if (!returnOnException(e, toolInfoID, parameters)) {
				throw new ToolAgentGeneralException(e);
			}
		}
	}

	protected abstract void invokeWebService(final Private stub, final AppParameter[] params, final String toolInfoID)
			throws Exception;

	protected String getEndpoint() {
		return cus.getProperty(CMDBUILD_ENDPOINT);
	}

	protected void addWSUserPassword(final Map<String, Object> options) throws Exception {
		addSystemWSUserPassword(options);
	}

	protected void addSystemWSUserPassword(final Map<String, Object> options) throws Exception {
		addWSAuthForUser(options, AbstractWSToolAgent.SYSTEM_USER);
	}

	protected void addWSAuthForUser(final Map<String, Object> options, final String username) throws Exception {
		addWSAuthForUser(options, username, null);
	}

	protected void addWSAuthForUser(final Map<String, Object> outProps, final String username, final String groupname)
			throws Exception {
		final StringBuffer wsUsername = new StringBuffer(cus.getProperty(USER_ATTRIBUTE));
		wsUsername.append("#").append(username);
		if (groupname != null) {
			wsUsername.append("@").append(groupname);
		}
		final String wsPassword = cus.getProperty(PASSWORD_ATTRIBUTE);
		outProps.put(WSHandlerConstants.USER, wsUsername.toString());
		final ClientPasswordCallback pwdCallback = new ClientPasswordCallback(wsUsername.toString(), wsPassword);
		outProps.put(WSHandlerConstants.PW_CALLBACK_REF, pwdCallback);
	}

	protected void addPerformerWSUserPassword(final Private stub, final Map<String, Object> outProps) throws Exception {
		addWSUserPassword(outProps);
		final String currentUsername = CmdbuildUtils.getCurrentUserNameForProcessInstance(stub, cmdbuildProcessClass,
				cmdbuildProcessId);
		final String groupname = CmdbuildUtils.getCurrentGroupName(shandle);
		addWSAuthForUser(outProps, currentUsername, groupname);
	}

	protected boolean returnOnException(final Exception exception, final String toolInfoID,
			final AppParameter[] parameters) {
		return false;
	}

	public static String sharkAttributeToWSString(final Private stub, final CmdbuildTableStruct tableStruct,
			final Card card, final String attrName, final Object attrValue) throws Exception {
		String serialized = null;
		if (attrValue != null) {
			if (attrValue instanceof String) {
				serialized = AbstractWSToolAgent
						.resolveParameter(stub, card, attrName, (String) attrValue, tableStruct);
			} else if (attrValue instanceof ReferenceType) {
				serialized = (((ReferenceType) attrValue).getId() + "");
			} else if (attrValue instanceof LookupType) {
				serialized = (((LookupType) attrValue).getId() + "");
			} else if (attrValue instanceof Date) {
				final Date date = (Date) attrValue;
				serialized = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date);
			} else {
				serialized = (attrValue.toString());
			}
		}
		return serialized;
	}

	protected static String resolveParameter(final Private stub, final Card updateCard, final String attrName,
			final String attrValue, final CmdbuildTableStruct tableStruct) throws Exception {
		final CmdbuildAttributeStruct attrStruct = tableStruct.getAttribute(attrName);

		if (attrStruct == null) {
			System.out.println("cannot find attribute struct for '" + attrName + "' in table '" + tableStruct.getName()
					+ "'");
			throw new Exception("Field '" + attrName + "' not found in table '" + tableStruct.getName() + "'!");
		}

		if (attrStruct.getType().equals("REFERENCE")) {
			System.out.println("Attribute " + attrName + " is a reference: " + attrStruct.getReferenceClass());
			if (!isNumeric(attrValue)) {
				// this is the description of the card, select the first that
				// match
				final String refClassName = attrStruct.getReferenceClass();

				final List<Attribute> attributeList = new ArrayList<Attribute>();
				final Attribute attr = new Attribute();
				attr.setName("Id");
				attributeList.add(attr);

				final Query query = new Query();
				final Filter filter = new Filter();
				filter.setName("Description");
				filter.getValue().add(attrValue);
				filter.setOperator("EQUALS");
				query.setFilter(filter);

				final List<Card> cards = stub.getCardList(refClassName, attributeList, query, null, null, null, null,
						null).getCards();

				return (cards != null && cards.size() > 0) ? String.valueOf(cards.get(0).getId()) : null;
			}
		} else if (attrStruct.getType().equals("LOOKUP")) {
			System.out.println("Attribute " + attrName + " is a lookup: " + attrStruct.getLookupType());
			if (!isNumeric(attrValue)) {
				// this is the description of the lookup, select the lookup id
				final String lkpType = attrStruct.getLookupType();
				final List<Lookup> lookups = stub.getLookupList(lkpType, attrValue, false);
				return (lookups != null && lookups.size() > 0) ? String.valueOf(lookups.get(0).getId()) : null;
			}
		}
		return attrValue;
	}

	private static boolean isNumeric(final String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (final NumberFormatException nfe) {
			return false;
		}
	}

}

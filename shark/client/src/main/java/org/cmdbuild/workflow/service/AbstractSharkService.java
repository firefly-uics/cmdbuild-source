package org.cmdbuild.workflow.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.workflow.CMWorkflowException;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstanceState;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttributeIterator;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.PackageAdministration;
import org.enhydra.shark.api.client.wfservice.SharkInterface;
import org.enhydra.shark.api.common.ActivityFilterBuilder;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.client.utilities.SharkInterfaceWrapper;

/**
 * Base class for Shark implementations.
 */
public abstract class AbstractSharkService implements CMWorkflowService {

	protected static final String DEFAULT_ENGINE_NAME = StringUtils.EMPTY;
	protected static final String DEFAULT_SCOPE = StringUtils.EMPTY;

	protected abstract class TransactedExecutor<T> {
		public T execute() throws CMWorkflowException {
			try {
				beginTransaction();
				final T result = command();
				commitTransaction();
				return result;
			} catch (final Exception e) {
				rollbackTransaction();
				throw new CMWorkflowException(e);
			}
		}

		private final void beginTransaction() throws Exception {
			SharkInterfaceWrapper.getUserTransaction().begin();
		}

		private final void commitTransaction() throws Exception {
			SharkInterfaceWrapper.getUserTransaction().commit();
		}

		private final void rollbackTransaction() {
			try {
				SharkInterfaceWrapper.getUserTransaction().rollback();
			} catch (final Exception e) {
			}
		}

		protected abstract T command() throws Exception;
	}

	private volatile SharkInterface shark;
	private volatile WMSessionHandle shandle;

	protected AbstractSharkService(final Properties props) {
		configureSharkInterfaceWrapper(props);
	}

	private void configureSharkInterfaceWrapper(final Properties props) {
		try {
			SharkInterfaceWrapper.setProperties(props, true);
		} catch (final RuntimeException e) {
			// Otherwise it ignores even unchecked exceptions
			throw e;
		} catch (final Exception e) {
			// Can never happen with this configuration! Shark APIs love to
			// throw java.lang.Exception even when it can't. Take a look at
			// SharkInterfaceWrapper.setProperty(...) and have a good laugh!
		}
	}

	@Override
	public final String[] getPackageVersions(final String pkgId) throws CMWorkflowException {
		return new TransactedExecutor<String[]>() {
			@Override
			protected String[] command() throws Exception {
				return shark().getPackageAdministration().getPackageVersions(handle(), pkgId);
			}
		}.execute();
	}

	@Override
	public void uploadPackage(final String pkgId, final byte[] pkgDefData) throws CMWorkflowException {
		new TransactedExecutor<Void>() {
			@Override
			protected Void command() throws Exception {
				final PackageAdministration pa = shark().getPackageAdministration();
				if (pa.getPackageVersions(handle(), pkgId).length == 0) {
					pa.uploadPackage(handle(), pkgDefData);
				} else {
					pa.updatePackage(handle(), pkgId, pkgDefData);
				}
				return null;
			}
		}.execute();
	}

	@Override
	public byte[] downloadPackage(final String pkgId, final String pkgVer) throws CMWorkflowException {
		return new TransactedExecutor<byte[]>() {
			@Override
			protected byte[] command() throws Exception {
				return shark().getPackageAdministration().getPackageContent(handle(), pkgId, pkgVer);
			}
		}.execute();
	}

	@Override
	public byte[][] downloadAllPackages() throws CMWorkflowException {
		return new TransactedExecutor<byte[][]>() {
			@Override
			protected byte[][] command() throws Exception {
				final PackageAdministration pa = shark().getPackageAdministration();
				final String[] pkgIds = pa.getOpenedPackageIds(handle());
				final byte[][] out = new byte[pkgIds.length][];
				for (int i = 0; i < pkgIds.length; ++i) {
					final String pkgId = pkgIds[i];
					final String pkgVer = pa.getCurrentPackageVersion(handle(), pkgId);
					final byte[] rawPkg = pa.getPackageContent(handle(), pkgId, pkgVer);
					out[i] = rawPkg;
				}
				return out;
			}
		}.execute();
	}

	@Override
	public String startProcess(final String pkgId, final String procDefId) throws CMWorkflowException {
		return new TransactedExecutor<String>() {
			@Override
			protected String command() throws Exception {
				final String uniqueProcDefId = shark().getXPDLBrowser().getUniqueProcessDefinitionName(handle(), pkgId,
						StringUtils.EMPTY, procDefId);
				final String procInstId = wapi().createProcessInstance(handle(), uniqueProcDefId, null);
				final String newProcInstId = wapi().startProcess(handle(), procInstId);
				return newProcInstId;
			}
		}.execute();
	}

	private final SharkInterface shark() throws Exception {
		if (shark == null) {
			synchronized (this) {
				if (shark == null) {
					shark = SharkInterfaceWrapper.getShark();
				}
			}
		}
		return shark;
	}

	private final WMSessionHandle handle() throws Exception {
		if (shandle == null) {
			synchronized (this) {
				if (shandle == null) {
					final WMConnectInfo wmci = getConnectionInfo();
					shandle = wapi().connect(wmci);
				}
			}
		}
		return shandle;
	}

	abstract protected WMConnectInfo getConnectionInfo();

	private final WAPI wapi() throws Exception {
		return shark().getWAPIConnection();
	}

	@Override
	public Map<String, Object> getProcessInstanceVariables(final String procInstId) throws CMWorkflowException {
		return new TransactedExecutor<Map<String, Object>>() {
			@Override
			protected Map<String, Object> command() throws Exception {
				final Map<String, Object> variables = new HashMap<String, Object>();
				final WMAttributeIterator iterator = wapi().listProcessInstanceAttributes(handle(), procInstId, null,
						false);
				for (final WMAttribute attribute : iterator.getArray()) {
					final String name = attribute.getName();
					final Object value = attribute.getValue();
					variables.put(name, value);
				}
				return variables;
			}
		}.execute();
	}

	@Override
	public void setProcessInstanceVariables(final String procInstId, final Map<String, Object> variables)
			throws CMWorkflowException {
		new TransactedExecutor<Void>() {
			@Override
			protected Void command() throws Exception {
				for (final String name : variables.keySet()) {
					final Object value = variables.get(name);
					wapi().assignProcessInstanceAttribute(handle(), procInstId, name, value);
				}
				return null;
			}
		}.execute();
	}

	@Override
	public WSActivityInstInfo[] findOpenActivitiesForProcessInstance(final String procInstId) throws CMWorkflowException {
		return new TransactedExecutor<WSActivityInstInfo[]>() {
			@Override
			protected WSActivityInstInfo[] command() throws Exception {
				final WMFilter filter = openActivitiesForProcessInstance(procInstId);
				final WMActivityInstance[] ais = wapi().listActivityInstances(handle(), filter, false).getArray();
				final WSActivityInstInfo[] out = new WSActivityInstInfo[ais.length];
				for (int i = 0; i < ais.length; ++i) {
					out[i] = new WMActivityInstanceWrapper(ais[i]);
				}
				return out;
			}

			private WMFilter openActivitiesForProcessInstance(final String procInstId) throws Exception {
				final ActivityFilterBuilder fb = shark().getActivityFilterBuilder();
				return fb.and(handle(),
						fb.addProcessIdEquals(handle(), procInstId),
						fb.addStateStartsWith(handle(), SharkConstants.STATEPREFIX_OPEN)
					);
			}
		}.execute();
	}

	@Override
	public WSActivityInstInfo[] findOpenActivitiesForProcess(final String procDefId) throws CMWorkflowException {
		return new TransactedExecutor<WSActivityInstInfo[]>() {
			@Override
			protected WSActivityInstInfo[] command() throws Exception {
				final WMFilter filter = openActivitiesForProcess(procDefId);
				final WMActivityInstance[] ais = wapi().listActivityInstances(handle(), filter, false).getArray();
				final WSActivityInstInfo[] out = new WSActivityInstInfo[ais.length];
				for (int i = 0; i < ais.length; ++i) {
					out[i] = new WMActivityInstanceWrapper(ais[i]);
				}
				return out;
			}

			private WMFilter openActivitiesForProcess(final String procDefId) throws Exception {
				final ActivityFilterBuilder fb = shark().getActivityFilterBuilder();
				return fb.and(handle(),
						fb.addProcessDefIdEquals(handle(), procDefId),
						fb.addStateStartsWith(handle(), SharkConstants.STATEPREFIX_OPEN)
					);
			}
		}.execute();
	}

	@Override
	public void abortActivityInstance(final String procInstId, final String actInstId) throws CMWorkflowException {
		new TransactedExecutor<Void>() {
			@Override
			protected Void command() throws Exception {
				// From Shark's FAQ, "terminate [...] tries to follow the next activity(s), [...] abort [...] doesn't."
				wapi().changeActivityInstanceState(handle(), procInstId, actInstId, WMActivityInstanceState.CLOSED_ABORTED);
				return null;
			}
		}.execute();
	}
}

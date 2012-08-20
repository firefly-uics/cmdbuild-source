package org.cmdbuild.workflow.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.IdentityTypesConverter;
import org.cmdbuild.workflow.TypesConverter;
import org.cmdbuild.workflow.event.NullWorkflowEventManager;
import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstanceState;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttributeIterator;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstanceState;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.PackageAdministration;
import org.enhydra.shark.api.client.wfservice.SharkInterface;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.common.ActivityFilterBuilder;
import org.enhydra.shark.api.common.ProcessFilterBuilder;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.client.utilities.SharkInterfaceWrapper;
import org.enhydra.shark.utilities.MiscUtilities;

/**
 * Base class for Shark implementations.
 */
public abstract class AbstractSharkService implements CMWorkflowService {

	protected static final String DEFAULT_ENGINE_NAME = StringUtils.EMPTY;
	protected static final String DEFAULT_SCOPE = StringUtils.EMPTY;
	protected static final String LAST_VERSION = StringUtils.EMPTY;

	private static final TypesConverter IDENTITY_TYPES_CONVERTER = new IdentityTypesConverter();
	private static final WorkflowEventManager NULL_EVENT_MANAGER = new NullWorkflowEventManager();

	/*
	 * Transactions should be handled with Spring
	 */
	private abstract class TransactedExecutor<T> {

		protected SharkInterface shark;
		protected WAPI wapi;
		protected WMSessionHandle handle;

		public T execute() throws CMWorkflowException {
			try {
				beginTransaction();
				handle = initAndConnect();
				final T result = command();
				commitTransaction();
				processEvents();
				return result;
			} catch (final Exception e) {
				rollbackTransaction();
				purgeEvents();
				throw new CMWorkflowException(e);
			} finally {
				disconnect();
			}
		}

		private final void beginTransaction() throws Exception {
			SharkInterfaceWrapper.getUserTransaction().begin();
		}

		private final WMSessionHandle initAndConnect() throws Exception {
			shark = shark();
			wapi = wapi();
			final WMConnectInfo wmci = getConnectionInfo();
			return wapi.connect(wmci);
		}

		private final void disconnect() {
			if (handle != null) {
				try {
					wapi.disconnect(handle);
				} catch (final Exception e) {
					// ignore errors on disconnection
				}
				handle = null;
			}
		}

		private void processEvents() throws CMWorkflowException {
			eventManager.processEvents(handle.getId());
		}

		private void purgeEvents() {
			eventManager.purgeEvents(handle.getId());
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

	/**
	 * It should be accessed through the {@see shark()} method only.
	 */
	private volatile SharkInterface _shark;

	/**
	 * It should be accessed through the {@see wapi()} method only.
	 */
	private volatile WAPI _wapi;

	private WorkflowEventManager eventManager;
	private TypesConverter typesConverter;

	protected AbstractSharkService(final Properties props) {
		configureSharkInterfaceWrapper(props);
		typesConverter = IDENTITY_TYPES_CONVERTER;
		eventManager = NULL_EVENT_MANAGER;
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

	public void setVariableConverter(final TypesConverter variableConverter) {
		Validate.notNull(variableConverter);
		this.typesConverter = variableConverter;
	}

	public void setEventManager(final WorkflowEventManager eventManager) {
		Validate.notNull(eventManager);
		this.eventManager = eventManager;
	}

	private WAPI wapi() throws Exception {
		if (_wapi == null) {
			synchronized (this) {
				if (_wapi == null) {
					_wapi = shark().getWAPIConnection();
					configureWAPI(_wapi);
				}
			}
		}
		return _wapi;
	}

	private SharkInterface shark() throws Exception {
		if (_shark == null) {
			synchronized (this) {
				if (_shark == null) {
					_shark = SharkInterfaceWrapper.getShark();
				}
			}
		}
		return _shark;
	}

	/**
	 * It can be overridden to add something to the WAPI interface ({@see RemoteSharkService}).
	 * 
	 * @throws Exception
	 */
	protected void configureWAPI(final WAPI wapi) {
	}

	abstract protected WMConnectInfo getConnectionInfo();



	@Override
	public final String[] getPackageVersions(final String pkgId) throws CMWorkflowException {
		return new TransactedExecutor<String[]>() {
			@Override
			protected String[] command() throws Exception {
				return shark.getPackageAdministration().getPackageVersions(handle, pkgId);
			}
		}.execute();
	}

	@Override
	public WSPackageDefInfo uploadPackage(final String pkgId, final byte[] pkgDefData) throws CMWorkflowException {
		return new TransactedExecutor<WSPackageDefInfo>() {
			@Override
			protected WSPackageDefInfo command() throws Exception {
				final PackageAdministration pa = shark.getPackageAdministration();
				final WMEntity uploadedPackage;
				if (pkgId == null || pa.getPackageVersions(handle, pkgId).length == 0) {
					uploadedPackage = pa.uploadPackage(handle, pkgDefData);
				} else {
					uploadedPackage = pa.updatePackage(handle, pkgId, pkgDefData);
				}
				return newWSPackageDefInfo(uploadedPackage.getPkgId(), uploadedPackage.getPkgVer());
			}

			private WSPackageDefInfo newWSPackageDefInfo(final String id, final String version) {
				return new WSPackageDefInfo() {

					@Override
					public String getPackageId() {
						return id;
					}

					@Override
					public String getPackageVersion() {
						return version;
					}

				};
			}
		}.execute();
	}

	@Override
	public byte[] downloadPackage(final String pkgId, final String pkgVer) throws CMWorkflowException {
		return new TransactedExecutor<byte[]>() {
			@Override
			protected byte[] command() throws Exception {
				return shark.getPackageAdministration().getPackageContent(handle, pkgId, pkgVer);
			}
		}.execute();
	}

	@Override
	public WSPackageDef[] downloadAllPackages() throws CMWorkflowException {
		return new TransactedExecutor<WSPackageDef[]>() {
			@Override
			protected WSPackageDef[] command() throws Exception {
				final PackageAdministration pa = shark.getPackageAdministration();
				final String[] pkgIds = pa.getOpenedPackageIds(handle);
				final WSPackageDef[] out = new WSPackageDef[pkgIds.length];
				for (int i = 0; i < pkgIds.length; ++i) {
					final String id = pkgIds[i];
					final String version = pa.getCurrentPackageVersion(handle, id);
					final byte[] data = pa.getPackageContent(handle, id, version);
					out[i] = newWSPackageDef(id, version, data);
				}
				return out;
			}

			private WSPackageDef newWSPackageDef(final String id, final String version, final byte[] data) {
				return new WSPackageDef() {

					@Override
					public String getPackageId() {
						return id;
					}

					@Override
					public String getPackageVersion() {
						return version;
					}

					@Override
					public byte[] getData() {
						return data;
					}

				};
			}
		}.execute();
	}

	@Override
	public WSProcessInstInfo startProcess(final String pkgId, final String procDefId) throws CMWorkflowException {
		return startProcess(pkgId, procDefId, Collections.<String, Object> emptyMap());
	}

	@Override
	public WSProcessInstInfo startProcess(final String pkgId, final String procDefId,
			final Map<String, ?> variables) throws CMWorkflowException {
		return new TransactedExecutor<WSProcessInstInfo>() {
			@Override
			protected WSProcessInstInfo command() throws Exception {
				final String uniqueProcDefId = shark.getXPDLBrowser().getUniqueProcessDefinitionName(handle, pkgId,
						LAST_VERSION, procDefId);
				final String procInstId = wapi.createProcessInstance(handle, uniqueProcDefId, null);
				setProcessInstanceVariablesNotTransacted(procInstId, variables, wapi, handle);
				final String newProcInstId = wapi.startProcess(handle, procInstId);
				return newWSProcessInstInfo(uniqueProcDefId, newProcInstId);
			}

			private WSProcessInstInfo newWSProcessInstInfo(final String uniqueProcDefId, final String procInstId) {
				return new WSProcessInstInfo() {

					@Override
					public String getPackageId() {
						return MiscUtilities.getProcessMgrPkgId(uniqueProcDefId);
					}

					@Override
					public String getPackageVersion() {
						return MiscUtilities.getProcessMgrVersion(uniqueProcDefId);
					}

					@Override
					public String getProcessDefinitionId() {
						return MiscUtilities.getProcessMgrProcDefId(uniqueProcDefId);
					}

					@Override
					public String getProcessInstanceId() {
						return procInstId;
					}

					@Override
					public WSProcessInstanceState getStatus() {
						return WSProcessInstanceState.OPEN; // Guessed
					}

				};
			}
		}.execute();
	}

	@Override
	public WSProcessInstInfo[] listOpenProcessInstances(final String procDefId) throws CMWorkflowException {
		return new TransactedExecutor<WSProcessInstInfo[]>() {
			@Override
			protected WSProcessInstInfo[] command() throws Exception {
				final WMProcessInstance[] pis = wapi.listProcessInstances(handle, openProcessInstances(procDefId),
						false).getArray();
				final WSProcessInstInfo[] out = new WSProcessInstInfo[pis.length];
				for (int i = 0; i < pis.length; ++i) {
					out[i] = WSProcessInstInfoImpl.newInstance(pis[i]);
				}
				return out;
			}

			private WMFilter openProcessInstances(final String procDefId) throws Exception {
				final ProcessFilterBuilder fb = shark.getProcessFilterBuilder();
				return fb.and(handle, fb.addProcessDefIdEquals(handle, procDefId),
						fb.addStateStartsWith(handle, SharkConstants.STATEPREFIX_OPEN));
			}
		}.execute();
	}

	@Override
	public WSProcessInstInfo getProcessInstance(final String procInstId) throws CMWorkflowException {
		return new TransactedExecutor<WSProcessInstInfo>() {
			@Override
			protected WSProcessInstInfo command() throws Exception {
				final WMProcessInstance pi = wapi.getProcessInstance(handle, procInstId);
				return WSProcessInstInfoImpl.newInstance(pi);
			}
		}.execute();
	}

	@Override
	public Map<String, Object> getProcessInstanceVariables(final String procInstId) throws CMWorkflowException {
		return getProcessInstanceVariables(procInstId, typesConverter);
	}

	@Override
	public Map<String, Object> getRawProcessInstanceVariables(final String procInstId) throws CMWorkflowException {
		return getProcessInstanceVariables(procInstId, IDENTITY_TYPES_CONVERTER);
	}

	private Map<String, Object> getProcessInstanceVariables(final String procInstId, final TypesConverter variableConverter) throws CMWorkflowException {
		return new TransactedExecutor<Map<String, Object>>() {
			@Override
			protected Map<String, Object> command() throws Exception {
				final Map<String, Object> variables = new HashMap<String, Object>();
				final WMAttributeIterator iterator = wapi.listProcessInstanceAttributes(handle, procInstId, null,
						false);
				for (final WMAttribute attribute : iterator.getArray()) {
					final String name = attribute.getName();
					final Object value = attribute.getValue();
					final Object nativeValue = variableConverter.fromWorkflowType(value);
					variables.put(name, nativeValue);
				}
				return variables;
			}
		}.execute();
	}

	@Override
	public void setProcessInstanceVariables(final String procInstId, final Map<String, ?> variables)
			throws CMWorkflowException {
		new TransactedExecutor<Void>() {
			@Override
			protected Void command() throws Exception {
				setProcessInstanceVariablesNotTransacted(procInstId, variables, wapi, handle);
				return null;
			}
		}.execute();
	}

	private void setProcessInstanceVariablesNotTransacted(final String procInstId, final Map<String, ?> variables,
			final WAPI wapi, final WMSessionHandle handle)
			throws Exception {
		for (final String name : variables.keySet()) {
			final Object nativeValue = variables.get(name);
			final Object sharkValue = typesConverter.toWorkflowType(nativeValue);
			wapi.assignProcessInstanceAttribute(handle, procInstId, name, sharkValue);
		}
	}

	@Override
	public WSActivityInstInfo[] findOpenActivitiesForProcessInstance(final String procInstId)
			throws CMWorkflowException {
		return new TransactedExecutor<WSActivityInstInfo[]>() {
			@Override
			protected WSActivityInstInfo[] command() throws Exception {
				final WMFilter filter = openActivitiesForProcessInstance(procInstId);
				final WMActivityInstance[] ais = wapi.listActivityInstances(handle, filter, false).getArray();
				final WSActivityInstInfo[] out = new WSActivityInstInfo[ais.length];
				for (int i = 0; i < ais.length; ++i) {
					out[i] = WSActivityInstInfoImpl.newInstance(ais[i]);
				}
				return out;
			}

			private WMFilter openActivitiesForProcessInstance(final String procInstId) throws Exception {
				final ActivityFilterBuilder fb = shark.getActivityFilterBuilder();
				return fb.and(handle, fb.addProcessIdEquals(handle, procInstId),
						fb.addStateStartsWith(handle, SharkConstants.STATEPREFIX_OPEN));
			}
		}.execute();
	}

	@Override
	public WSActivityInstInfo[] findOpenActivitiesForProcess(final String procDefId) throws CMWorkflowException {
		return new TransactedExecutor<WSActivityInstInfo[]>() {
			@Override
			protected WSActivityInstInfo[] command() throws Exception {
				final WMFilter filter = openActivitiesForProcess(procDefId);
				final WMActivityInstance[] ais = wapi.listActivityInstances(handle, filter, false).getArray();
				final WSActivityInstInfo[] out = new WSActivityInstInfo[ais.length];
				for (int i = 0; i < ais.length; ++i) {
					out[i] = WSActivityInstInfoImpl.newInstance(ais[i]);
				}
				return out;
			}

			private WMFilter openActivitiesForProcess(final String procDefId) throws Exception {
				final ActivityFilterBuilder fb = shark.getActivityFilterBuilder();
				return fb.and(handle, fb.addProcessDefIdEquals(handle, procDefId),
						fb.addStateStartsWith(handle, SharkConstants.STATEPREFIX_OPEN));
			}
		}.execute();
	}

	@Override
	public void abortActivityInstance(final String procInstId, final String actInstId) throws CMWorkflowException {
		// From Shark's FAQ,
		// "terminate [...] tries to follow the next activity(s), [...] abort [...] doesn't."
		changeActivityInstanceStates(procInstId, actInstId, WMActivityInstanceState.CLOSED_ABORTED);
	}

	@Override
	public void advanceActivityInstance(final String procInstId, final String actInstId) throws CMWorkflowException {
		changeActivityInstanceStates(procInstId, actInstId, WMActivityInstanceState.OPEN_RUNNING,
				WMActivityInstanceState.CLOSED_COMPLETED);
	}

	private void changeActivityInstanceStates(final String procInstId, final String actInstId,
			final WMActivityInstanceState... states) throws CMWorkflowException {
		new TransactedExecutor<Void>() {
			@Override
			protected Void command() throws Exception {
				for (final WMActivityInstanceState state : states) {
					wapi.changeActivityInstanceState(handle, procInstId, actInstId, state);
				}
				return null;
			}
		}.execute();
	}

	@Override
	public void abortProcessInstance(final String procInstId) throws CMWorkflowException {
		changeProcessInstanceState(procInstId, WMProcessInstanceState.CLOSED_ABORTED);
	}

	@Override
	public void suspendProcessInstance(final String procInstId) throws CMWorkflowException {
		changeProcessInstanceState(procInstId, WMProcessInstanceState.OPEN_NOTRUNNING_SUSPENDED);
	}

	@Override
	public void resumeProcessInstance(final String procInstId) throws CMWorkflowException {
		changeProcessInstanceState(procInstId, WMProcessInstanceState.OPEN_RUNNING);
	}

	private void changeProcessInstanceState(final String procInstId, final WMProcessInstanceState state)
			throws CMWorkflowException {
		new TransactedExecutor<Void>() {
			@Override
			protected Void command() throws Exception {
				wapi.changeProcessInstanceState(handle, procInstId, state);
				return null;
			}
		}.execute();
	}
}

package org.cmdbuild.workflow.service;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.Validate;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.TypesConverter;
import org.cmdbuild.workflow.event.NullWorkflowEventManager;
import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstanceState;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.client.utilities.SharkInterfaceWrapper;

public abstract class TransactedSharkService extends AbstractSharkService {

	/*
	 * Transactions should be handled with Spring
	 */
	private abstract class TransactedExecutor<T> {

		public T execute() throws CMWorkflowException {
			try {
				beginTransaction();
				initAndConnect();
				final T result = command();
				commitTransaction();
				processEvents();
				return result;
			} catch (final CMWorkflowException e) {
				rollbackTransaction();
				purgeEvents();
				throw e;
			} finally {
				disconnect();
			}
		}

		private final void beginTransaction() throws CMWorkflowException {
			try {
				SharkInterfaceWrapper.getUserTransaction().begin();
			} catch (final Exception e) {
				throw new CMWorkflowException(e);
			}
		}

		private final void initAndConnect() throws CMWorkflowException {
			final WMConnectInfo wmci = getConnectionInfo();
			try {
				WMSessionHandle handle = wapi().connect(wmci);
				localHandle.set(handle);
			} catch (final Exception e) {
				throw new CMWorkflowException(e);
			}
		}

		private final void disconnect() {
			final WMSessionHandle handle = localHandle.get();
			if (handle != null) {
				try {
					wapi().disconnect(handle);
				} catch (final Exception e) {
					// ignore errors on disconnection
				}
				localHandle.remove();
			}
		}

		private void processEvents() throws CMWorkflowException {
			final WMSessionHandle handle = localHandle.get();
			if (handle != null) {
				eventManager.processEvents(handle().getId());
			}
		}

		private void purgeEvents() {
			final WMSessionHandle handle = localHandle.get();
			if (handle != null) {
				eventManager.purgeEvents(handle.getId());
			}
		}

		private final void commitTransaction() throws CMWorkflowException {
			try {
				SharkInterfaceWrapper.getUserTransaction().commit();
			} catch (final Exception e) {
				throw new CMWorkflowException(e);
			}
		}

		private final void rollbackTransaction() {
			try {
				SharkInterfaceWrapper.getUserTransaction().rollback();
			} catch (final Exception e) {
			}
		}

		protected abstract T command() throws CMWorkflowException;
	}

	private final ThreadLocal<WMSessionHandle> localHandle;

	private static final WorkflowEventManager NULL_EVENT_MANAGER = new NullWorkflowEventManager();
	private WorkflowEventManager eventManager;

	protected TransactedSharkService(final Properties props) {
		super(props);
		localHandle = new ThreadLocal<WMSessionHandle>();
		eventManager = NULL_EVENT_MANAGER;
	}

	public void setEventManager(final WorkflowEventManager eventManager) {
		Validate.notNull(eventManager);
		this.eventManager = eventManager;
	}

	protected WMSessionHandle handle() {
		return localHandle.get();
	}

	@Override
	public final String[] getPackageVersions(final String pkgId) throws CMWorkflowException {
		return new TransactedExecutor<String[]>() {
			@Override
			protected String[] command() throws CMWorkflowException {
				return TransactedSharkService.super.getPackageVersions(pkgId);
			}
		}.execute();
	}

	@Override
	public WSPackageDefInfo uploadPackage(final String pkgId, final byte[] pkgDefData) throws CMWorkflowException {
		return new TransactedExecutor<WSPackageDefInfo>() {
			@Override
			protected WSPackageDefInfo command() throws CMWorkflowException {
				return TransactedSharkService.super.uploadPackage(pkgId, pkgDefData);
			}
		}.execute();
	}

	@Override
	public byte[] downloadPackage(final String pkgId, final String pkgVer) throws CMWorkflowException {
		return new TransactedExecutor<byte[]>() {
			@Override
			protected byte[] command() throws CMWorkflowException {
				return TransactedSharkService.super.downloadPackage(pkgId, pkgVer);
			}
		}.execute();
	}

	@Override
	public WSPackageDef[] downloadAllPackages() throws CMWorkflowException {
		return new TransactedExecutor<WSPackageDef[]>() {
			@Override
			protected WSPackageDef[] command() throws CMWorkflowException {
				return TransactedSharkService.super.downloadAllPackages();
			}
		}.execute();
	}

	@Override
	public WSProcessInstInfo startProcess(final String pkgId, final String procDefId, final Map<String, ?> variables)
			throws CMWorkflowException {
		return new TransactedExecutor<WSProcessInstInfo>() {
			@Override
			protected WSProcessInstInfo command() throws CMWorkflowException {
				return TransactedSharkService.super.startProcess(pkgId, procDefId, variables);
			}
		}.execute();
	}

	@Override
	public WSProcessInstInfo[] listOpenProcessInstances(final String procDefId) throws CMWorkflowException {
		return new TransactedExecutor<WSProcessInstInfo[]>() {
			@Override
			protected WSProcessInstInfo[] command() throws CMWorkflowException {
				return TransactedSharkService.super.listOpenProcessInstances(procDefId);
			}
		}.execute();
	}

	@Override
	public WSProcessInstInfo getProcessInstance(final String procInstId) throws CMWorkflowException {
		return new TransactedExecutor<WSProcessInstInfo>() {
			@Override
			protected WSProcessInstInfo command() throws CMWorkflowException {
				return TransactedSharkService.super.getProcessInstance(procInstId);
			}
		}.execute();
	}

	@Override
	public Map<String, Object> getProcessInstanceVariables(final String procInstId,
			final TypesConverter variableConverter) throws CMWorkflowException {
		return new TransactedExecutor<Map<String, Object>>() {
			@Override
			protected Map<String, Object> command() throws CMWorkflowException {
				return TransactedSharkService.super.getProcessInstanceVariables(procInstId, variableConverter);
			}
		}.execute();
	}

	@Override
	public void setProcessInstanceVariables(final String procInstId, final Map<String, ?> variables)
			throws CMWorkflowException {
		new TransactedExecutor<Void>() {
			@Override
			protected Void command() throws CMWorkflowException {
				TransactedSharkService.super.setProcessInstanceVariables(procInstId, variables);
				return null;
			}
		}.execute();
	}

	@Override
	public WSActivityInstInfo[] findOpenActivitiesForProcessInstance(final String procInstId)
			throws CMWorkflowException {
		return new TransactedExecutor<WSActivityInstInfo[]>() {
			@Override
			protected WSActivityInstInfo[] command() throws CMWorkflowException {
				return TransactedSharkService.super.findOpenActivitiesForProcessInstance(procInstId);
			}
		}.execute();
	}

	@Override
	public WSActivityInstInfo[] findOpenActivitiesForProcess(final String procDefId) throws CMWorkflowException {
		return new TransactedExecutor<WSActivityInstInfo[]>() {
			@Override
			protected WSActivityInstInfo[] command() throws CMWorkflowException {
				return TransactedSharkService.super.findOpenActivitiesForProcess(procDefId);
			}
		}.execute();
	}

	@Override
	public void abortActivityInstance(final String procInstId, final String actInstId) throws CMWorkflowException {
		new TransactedExecutor<Void>() {
			@Override
			protected Void command() throws CMWorkflowException {
				TransactedSharkService.super.abortActivityInstance(procInstId, actInstId);
				return null;
			}
		}.execute();
	}

	@Override
	public void advanceActivityInstance(final String procInstId, final String actInstId) throws CMWorkflowException {
		new TransactedExecutor<Void>() {
			@Override
			protected Void command() throws CMWorkflowException {
				TransactedSharkService.super.advanceActivityInstance(procInstId, actInstId);
				return null;
			}
		}.execute();
	}

	@Override
	protected void changeProcessInstanceState(final String procInstId, final WMProcessInstanceState newState)
			throws CMWorkflowException {
		new TransactedExecutor<Void>() {
			@Override
			protected Void command() throws CMWorkflowException {
				TransactedSharkService.super.changeProcessInstanceState(procInstId, newState);
				return null;
			}
		}.execute();
	}

}

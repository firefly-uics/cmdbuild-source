package org.cmdbuild.workflow.service;

import java.util.Properties;

import org.cmdbuild.workflow.CMWorkflowException;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.PackageAdministration;
import org.enhydra.shark.api.client.wfservice.SharkInterface;
import org.enhydra.shark.client.utilities.SharkInterfaceWrapper;

/**
 * Base class for Shark implementations.
 */
public abstract class AbstractSharkService implements CMWorkflowService {

	protected static final String DEFAULT_ENGINE_NAME = "";
	protected static final String DEFAULT_SCOPE = "";

	protected abstract class TransactedExecutor<T> {

		public T execute() throws CMWorkflowException {
			try {
				beginTransaction();
				final T result = command();
				commitTransaction();
				return result;
			} catch (Exception e) {
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
			} catch (Exception e) {
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
		} catch (RuntimeException e) {
			// Otherwise it ingores even unchecked exceptions
			throw e;
		} catch (Exception e) {
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
				PackageAdministration pa = shark().getPackageAdministration();
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

}

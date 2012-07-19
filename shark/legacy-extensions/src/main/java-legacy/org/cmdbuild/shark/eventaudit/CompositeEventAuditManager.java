package org.cmdbuild.shark.eventaudit;

import java.util.ArrayList;
import java.util.List;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.eventaudit.AssignmentEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.CreateProcessEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.DataEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.EventAuditException;
import org.enhydra.shark.api.internal.eventaudit.EventAuditManagerInterface;
import org.enhydra.shark.api.internal.eventaudit.StateEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class CompositeEventAuditManager implements EventAuditManagerInterface {

	List<EventAuditManagerInterface> managers;

	CallbackUtilities cus;

	public void configure(final CallbackUtilities cus) throws Exception {
		this.cus = cus;
		cus.debug(null, "Configure CompositeEventAuditManager...");
		this.managers = new ArrayList<EventAuditManagerInterface>();

		configureManagers(cus);

		doWithManagers(new DoWithManager<Exception>() {
			public void doWithManager(EventAuditManagerInterface arg0)
					throws Exception {
				cus.info(null, "CompositeEventAuditManager - configure manager: " + arg0.getClass().getCanonicalName());
				arg0.configure(cus);
			}
		});

		cus.debug(null, "...CompositeEventAuditManager configured, managers: "
				+ managers.size());
	}

	private interface NonVoidMethod<T> {
		public T doWithManager(EventAuditManagerInterface manager)
				throws EventAuditException;
	}

	@SuppressWarnings("unchecked")
	private interface ListMethod extends NonVoidMethod<List> {
	};

	private interface BoolMethod extends NonVoidMethod<Boolean> {
	};

	private interface DoWithManager<T extends Exception> {
		public void doWithManager(EventAuditManagerInterface manager) throws T;
	}

	private interface DoWithManagerDef extends
			DoWithManager<EventAuditException> {
	}

	@SuppressWarnings("unchecked")
	private List doWithManagers(ListMethod item) throws EventAuditException {
		List out = new ArrayList();
		for (EventAuditManagerInterface mngr : managers) {
			List tmp = item.doWithManager(mngr);
			if(tmp != null){
				out.addAll(item.doWithManager(mngr));
			}
		}
		return out;
	}

	private boolean doWithManager(BoolMethod item) throws EventAuditException {
		boolean out = false;
		boolean first = true;
		for (EventAuditManagerInterface mngr : managers) {
			if (first) {
				out = item.doWithManager(mngr);
				first = false;
			} else {
				item.doWithManager(mngr);
			}
		}
		return out;
	}

	private void doWithManagers(DoWithManagerDef item)
			throws EventAuditException {
		for (EventAuditManagerInterface mngr : managers) {
			item.doWithManager(mngr);
		}
	}

	@SuppressWarnings("unchecked")
	private void doWithManagers(DoWithManager item) throws Exception {
		for (EventAuditManagerInterface mngr : managers) {
			item.doWithManager(mngr);
		}
	}

	@SuppressWarnings("unchecked")
	private void configureManagers(CallbackUtilities cus) {
		String tmp = cus.getProperty("compositeAuditManager.ClassNames");
		cus.info(null, "CompositeEventAuditManager ClassNames: " + tmp);
		String[] classNames = tmp.split(",");
		for (String className : classNames) {
			cus.info(null, " --> " + className);
			Class<? extends EventAuditManagerInterface> theClass;
			try {
				theClass = (Class<? extends EventAuditManagerInterface>) Class
						.forName(className.trim());
				managers.add(theClass.newInstance());
			} catch (Exception e) {
				// Skip this manager
				System.out.println("Cannot instantiate CompositeEventAuditManager class " + className);
			}
		}

	}
	
	@SuppressWarnings("unchecked")
	public <T extends EventAuditManagerInterface> T getManager(Class<T> theClass) {
		for(EventAuditManagerInterface mngr : this.managers) {
			if(mngr.getClass() == theClass) {
				return (T)mngr;
			}
		}
		return null;
	}

	public void delete(final WMSessionHandle arg0,
			final AssignmentEventAuditPersistenceObject arg1)
			throws EventAuditException {
		doWithManagers(new DoWithManagerDef() {
			public void doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				mngr.delete(arg0, arg1);
			}
		});
	}

	public void delete(final WMSessionHandle arg0,
			final CreateProcessEventAuditPersistenceObject arg1)
			throws EventAuditException {
		doWithManagers(new DoWithManagerDef() {
			public void doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				mngr.delete(arg0, arg1);
			}
		});
	}

	public void delete(final WMSessionHandle arg0,
			final DataEventAuditPersistenceObject arg1)
			throws EventAuditException {
		doWithManagers(new DoWithManagerDef() {
			public void doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				mngr.delete(arg0, arg1);
			}
		});
	}

	public void delete(final WMSessionHandle arg0,
			final StateEventAuditPersistenceObject arg1)
			throws EventAuditException {
		doWithManagers(new DoWithManagerDef() {
			public void doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				mngr.delete(arg0, arg1);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List listActivityHistoryInfoWhere(final WMSessionHandle arg0,
			final String arg1, final int arg2, final int arg3,
			final boolean arg4) throws EventAuditException {
		return doWithManagers(new ListMethod() {
			public List doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				return mngr.listActivityHistoryInfoWhere(arg0, arg1, arg2,
						arg3, arg4);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List listProcessDefinitionHistoryInfoWhere(
			final WMSessionHandle arg0, final String arg1, final boolean arg2)
			throws EventAuditException {
		return doWithManagers(new ListMethod() {
			public List doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				return mngr.listProcessDefinitionHistoryInfoWhere(arg0, arg1,
						arg2);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List listProcessHistoryInfoWhere(final WMSessionHandle arg0,
			final String arg1, final int arg2, final int arg3,
			final boolean arg4, final boolean arg5, final boolean arg6)
			throws EventAuditException {
		return doWithManagers(new ListMethod() {
			public List doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				return mngr.listProcessHistoryInfoWhere(arg0, arg1, arg2, arg3,
						arg4, arg5, arg6);
			}
		});
	}

	public void persist(final WMSessionHandle arg0,
			final AssignmentEventAuditPersistenceObject arg1)
			throws EventAuditException {
		doWithManagers(new DoWithManagerDef() {
			public void doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				mngr.persist(arg0, arg1);
			}
		});
	}

	public void persist(final WMSessionHandle arg0,
			final CreateProcessEventAuditPersistenceObject arg1)
			throws EventAuditException {
		doWithManagers(new DoWithManagerDef() {
			public void doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				mngr.persist(arg0, arg1);
			}
		});
	}

	public void persist(final WMSessionHandle arg0,
			final DataEventAuditPersistenceObject arg1)
			throws EventAuditException {
		doWithManagers(new DoWithManagerDef() {
			public void doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				mngr.persist(arg0, arg1);
			}
		});
	}

	public void persist(final WMSessionHandle arg0,
			final StateEventAuditPersistenceObject arg1)
			throws EventAuditException {
		doWithManagers(new DoWithManagerDef() {
			public void doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				mngr.persist(arg0, arg1);
			}
		});
	}

	public boolean restore(final WMSessionHandle arg0,
			final AssignmentEventAuditPersistenceObject arg1)
			throws EventAuditException {
		return doWithManager(new BoolMethod() {
			public Boolean doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				return mngr.restore(arg0, arg1);
			}
		});
	}

	public boolean restore(final WMSessionHandle arg0,
			final CreateProcessEventAuditPersistenceObject arg1)
			throws EventAuditException {
		return doWithManager(new BoolMethod() {
			public Boolean doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				return mngr.restore(arg0, arg1);
			}
		});
	}

	public boolean restore(final WMSessionHandle arg0,
			final DataEventAuditPersistenceObject arg1)
			throws EventAuditException {
		return doWithManager(new BoolMethod() {
			public Boolean doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				return mngr.restore(arg0, arg1);
			}
		});
	}

	public boolean restore(final WMSessionHandle arg0,
			final StateEventAuditPersistenceObject arg1)
			throws EventAuditException {
		return doWithManager(new BoolMethod() {
			public Boolean doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				return mngr.restore(arg0, arg1);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List restoreActivityHistory(final WMSessionHandle arg0,
			final String arg1, final String arg2) throws EventAuditException {
		return doWithManagers(new ListMethod() {
			public List doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				return mngr.restoreActivityHistory(arg0, arg1, arg2);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List restoreProcessHistory(final WMSessionHandle arg0,
			final String arg1) throws EventAuditException {
		return doWithManagers(new ListMethod() {
			public List doWithManager(EventAuditManagerInterface mngr)
					throws EventAuditException {
				return mngr.restoreProcessHistory(arg0, arg1);
			}
		});
	}

}

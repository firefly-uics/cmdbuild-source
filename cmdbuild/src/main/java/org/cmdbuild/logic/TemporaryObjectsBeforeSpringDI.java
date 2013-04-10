package org.cmdbuild.logic;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.auth.context.DefaultPrivilegeContextFactory;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.dao.driver.AbstractDBDriver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dao.view.user.UserDataView;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.lock.EmptyLockCard;
import org.cmdbuild.logic.data.access.lock.InMemoryLockCard;
import org.cmdbuild.logic.data.access.lock.LockCardManager;
import org.cmdbuild.logic.data.access.lock.LockCardManager.LockCardConfiguration;
import org.cmdbuild.privileges.fetchers.DataViewRowAndColumnPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.factories.CMClassPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.FilterPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.PrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.ViewPrivilegeFetcherFactory;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.Group;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.store.DBDashboardStore;
import org.cmdbuild.services.store.report.JDBCReportStore;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.workflow.UpdateOperationListenerImpl;
import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.cmdbuild.workflow.service.AbstractSharkService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Lists;

@Legacy("Spring should be used")
public class TemporaryObjectsBeforeSpringDI {

	private static ApplicationContext applicationContext = applicationContext();

	private static final DefaultPrivilegeContextFactory privilegeCtxFactory;
	private static final WorkflowEventManager workflowEventManager;

	private static final LockCardManager inMemoryLockCardManager;
	private static final LockCardManager emptyLockCardManager;

	static {
		final AbstractSharkService workflowService = applicationContext.getBean(AbstractSharkService.class);
		privilegeCtxFactory = new DefaultPrivilegeContextFactory();
		workflowEventManager = null;
		workflowService.setUpdateOperationListener(new UpdateOperationListenerImpl(workflowEventManager));
		inMemoryLockCardManager = new InMemoryLockCard(getLockCardConfiguration());
		emptyLockCardManager = new EmptyLockCard();
	}

	public static Iterable<PrivilegeFetcherFactory> getPrivilegeFetcherFactories() {
		final DBDataView dbDataView = applicationContext.getBean(DBDataView.class);
		final List<PrivilegeFetcherFactory> factories = Lists.newArrayList();
		factories.add(new CMClassPrivilegeFetcherFactory(dbDataView));
		factories.add(new ViewPrivilegeFetcherFactory(dbDataView));
		factories.add(new FilterPrivilegeFetcherFactory(dbDataView));
		return factories;
	}

	/**
	 * @deprecated used by legacy dao and cache manager
	 */
	@Deprecated
	public static AbstractDBDriver getDriver() {
		return applicationContext.getBean(AbstractDBDriver.class);
	}

	public static DefaultPrivilegeContextFactory getPrivilegeContextFactory() {
		return privilegeCtxFactory;
	}

	public static ReportStore getReportStore() {
		return new JDBCReportStore();
	}

	public static DashboardLogic getDashboardLogic() {
		return new DashboardLogic(getSystemView(), new DBDashboardStore(getSystemView()), new SessionVars().getUser());
	}

	public static GISLogic getGISLogic() {
		return new GISLogic(UserContext.systemContext());
	}

	public static CMDataView getUserDataView() {
		final DBDataView dbDataView = applicationContext.getBean(DBDataView.class);
		return new UserDataView(dbDataView, getOperationUser().getPrivilegeContext(), getRowPrivilegeFetcher());
	}

	private static RowAndColumnPrivilegeFetcher getRowPrivilegeFetcher() {
		return new DataViewRowAndColumnPrivilegeFetcher(getSystemView(), getOperationUser().getPrivilegeContext());
	}

	public static OperationUser getOperationUser() {
		return new SessionVars().getUser();
	}

	public static CMDataView getSystemView() {
		final DBDataView dbDataView = applicationContext.getBean(DBDataView.class);
		return dbDataView;
	}

	public static DataAccessLogic getDataAccessLogic() {
		LockCardManager lockCardManager;
		if (CmdbuildProperties.getInstance().getLockCard()) {
			inMemoryLockCardManager.updateLockCardConfiguration(getLockCardConfiguration());
			lockCardManager = inMemoryLockCardManager;
		} else {
			lockCardManager = emptyLockCardManager;
		}

		return new DataAccessLogic(getUserDataView(), lockCardManager);
	}

	public static LockCardConfiguration getLockCardConfiguration() {
		final boolean showUser = CmdbuildProperties.getInstance().getLockCardUserVisible();
		final long timeout = CmdbuildProperties.getInstance().getLockCardTimeOut();

		return new LockCardConfiguration() {

			@Override
			public boolean isLockerUsernameVisible() {
				return showUser;
			}

			@Override
			public long getExpirationTimeInMilliseconds() {
				return timeout * 1000; // To have milliseconds
			}

		};
	}

	public static DataDefinitionLogic getDataDefinitionLogic() {
		return new DataDefinitionLogic(getSystemView());
	}

	public static DataAccessLogic getSystemDataAccessLogic() {
		return new DataAccessLogic(getSystemView(), new EmptyLockCard());
	}

	public static WorkflowLogic getSystemWorkflowLogic() {
		throw new UnsupportedOperationException("to be implemented, needed for scheduled jobs");
		// return new
		// WorkflowLogic(getWorkflowEngine(UserContext.systemContext()));
	}

	public static WorkflowEventManager getWorkflowEventManager() {
		return workflowEventManager;
	}

	public static class SimplifiedUserContext {
		private final UserContext userContext;

		public SimplifiedUserContext(final UserContext userContext) {
			this.userContext = userContext;
		}

		public List<String> getGroupNames() {
			final List<String> groupNames = new ArrayList<String>();
			for (final Group g : userContext.getGroups()) {
				groupNames.add(g.getName());
			}
			return groupNames;
		}

		public boolean isAdmin() {
			return userContext.privileges().isAdmin();
		}
	}

}

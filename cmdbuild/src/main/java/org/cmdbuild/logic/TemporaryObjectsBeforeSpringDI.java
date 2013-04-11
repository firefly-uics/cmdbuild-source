package org.cmdbuild.logic;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.List;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.driver.AbstractDBDriver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.privileges.fetchers.factories.CMClassPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.FilterPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.PrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.ViewPrivilegeFetcherFactory;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.UpdateOperationListenerImpl;
import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.cmdbuild.workflow.service.AbstractSharkService;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Lists;

@Legacy("Spring should be used")
public class TemporaryObjectsBeforeSpringDI {

	private static ApplicationContext applicationContext = applicationContext();

	private static final WorkflowEventManager workflowEventManager;

	static {
		final AbstractSharkService workflowService = applicationContext.getBean(AbstractSharkService.class);
		workflowEventManager = applicationContext.getBean(WorkflowEventManager.class);
		workflowService.setUpdateOperationListener(new UpdateOperationListenerImpl(workflowEventManager));
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

	public static GISLogic getGISLogic() {
		return new GISLogic(UserContext.systemContext());
	}

	public static OperationUser getOperationUser() {
		return applicationContext.getBean(OperationUser.class);
	}

	public static CMDataView getSystemView() {
		return applicationContext.getBean(DBDataView.class);
	}

	public static DataAccessLogic getDataAccessLogic() {
		return applicationContext.getBean("userDataAccessLogic", DataAccessLogic.class);
	}

	public static DataAccessLogic getSystemDataAccessLogic() {
		return applicationContext.getBean("systemDataAccessLogic", DataAccessLogic.class);
	}

	public static WorkflowLogic getSystemWorkflowLogic() {
		throw new UnsupportedOperationException("to be implemented, needed for scheduled jobs");
		// return new
		// WorkflowLogic(getWorkflowEngine(UserContext.systemContext()));
	}

	public static WorkflowEventManager getWorkflowEventManager() {
		return workflowEventManager;
	}

}

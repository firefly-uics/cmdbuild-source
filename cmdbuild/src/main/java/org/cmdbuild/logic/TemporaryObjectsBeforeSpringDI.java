package org.cmdbuild.logic;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.driver.AbstractDBDriver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.services.auth.UserContext;
import org.springframework.context.ApplicationContext;

@Legacy("Spring should be used")
public class TemporaryObjectsBeforeSpringDI {

	private static ApplicationContext applicationContext = applicationContext();

	/**
	 * @deprecated used by legacy dao
	 */
	@Deprecated
	public static AbstractDBDriver getDriver() {
		return applicationContext.getBean(AbstractDBDriver.class);
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

}

package org.cmdbuild.logic;

import org.cmdbuild.auth.CMAccessControlManager;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.driver.CachingDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dao.view.user.UserDataView;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.auth.AccessControlManagerWrapper;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CMWorkflowEngine;
import org.cmdbuild.workflow.WorkflowEngineWrapper;

@Legacy("Spring should be used")
public class TemporaryObjectsBeforeSpringDI {

	final static CachingDriver driver;

	static {
		final javax.sql.DataSource datasource = DBService.getInstance().getDataSource();
		driver = new PostgresDriver(datasource);
	}

	public static CachingDriver getDriver() {
		return driver;
	}

	public static CMDataView getUserContextView(UserContext userCtx) {
		final CMAccessControlManager acm = new AccessControlManagerWrapper(userCtx.privileges());
		return new UserDataView(new DBDataView(driver), acm);
	}

	public static CMWorkflowEngine getWorkflowEngine(UserContext userCtx) {
		return new WorkflowEngineWrapper(userCtx);
	}
}

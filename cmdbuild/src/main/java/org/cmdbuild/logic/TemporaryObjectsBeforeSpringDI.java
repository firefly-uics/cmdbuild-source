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
import org.cmdbuild.workflow.xpdl.PackageHandler;
import org.cmdbuild.workflow.xpdl.SharkPackageHandler;

@Legacy("Spring should be used")
public class TemporaryObjectsBeforeSpringDI {

	final static CachingDriver driver;
	final static PackageHandler packageHandler;

	static {
		final javax.sql.DataSource datasource = DBService.getInstance().getDataSource();
		driver = new PostgresDriver(datasource);
		packageHandler = new SharkPackageHandler();
	}

	public static CachingDriver getDriver() {
		return driver;
	}

	public static CMDataView getUserContextView(UserContext userCtx) {
		final CMAccessControlManager acm = new AccessControlManagerWrapper(userCtx.privileges());
		return new UserDataView(new DBDataView(driver), acm);
	}

	public static CMWorkflowEngine getWorkflowEngine(UserContext userCtx) {
		return new WorkflowEngineWrapper(userCtx, packageHandler);
	}
}

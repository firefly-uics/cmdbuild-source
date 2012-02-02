package org.cmdbuild.logic;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.driver.CachingDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dao.view.user.UserDataView;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.auth.OperationUserWrapper;
import org.cmdbuild.services.auth.UserContext;

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
		final OperationUser user = new OperationUserWrapper(userCtx);
		return new UserDataView(new DBDataView(driver), user);
	}
}

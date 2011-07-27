package org.cmdbuild.logic;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.auth.UserContext;

public class TemporaryObjectsBeforeSpringDI {

	final static DBDriver driver;

	static {
		final javax.sql.DataSource datasource = DBService.getInstance().getDataSource();
		driver = new PostgresDriver(datasource);
	}

	public static DBDriver getDriver() {
		return driver;
	}

	public static CMDataView getUserContextView(UserContext userCtx) {
		return getSystemView(); // TODO UserContextView()
	}

	public static CMDataView getSystemView() {
		return new DBDataView(driver);
	}
}

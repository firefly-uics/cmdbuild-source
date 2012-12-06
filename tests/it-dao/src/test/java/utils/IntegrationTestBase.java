package utils;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.view.DBDataView;
import org.junit.After;
import org.junit.BeforeClass;

/**
 * Class containing methods for initializing the integration tests database
 */
public abstract class IntegrationTestBase {

	private static final DBInitializer dbInitializer = new DBInitializer();

	private final GenericRollbackDriver rollbackDriver;
	private final DBDataView dbView;

	protected IntegrationTestBase() {
		final DBDriver pgDriver = dbInitializer.getDriver();
		this.rollbackDriver = new GenericRollbackDriver(pgDriver);
		this.dbView = new DBDataView(rollbackDriver);
	}

	public DBDriver dbDriver() {
		return rollbackDriver;
	}

	public DBDataView dbDataView() {
		return dbView;
	}

	@BeforeClass
	public static void initialize() {
		dbInitializer.initialize();
	}

	@After
	public void rollback() {
		rollbackDriver.rollback();
	}

}

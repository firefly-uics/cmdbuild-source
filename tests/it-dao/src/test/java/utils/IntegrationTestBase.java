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

	private final DBDriver testDriver;
	private final DBDataView dbView;

	protected IntegrationTestBase() {
		this.testDriver = createTestDriver();
		this.dbView = new DBDataView(testDriver);
	}

	/**
	 * Override if you need to decorate the default.
	 */
	protected DBDriver createBaseDriver() {
		return dbInitializer.getDriver();
	}

	/**
	 * Override if you don't need/want the rollback driver.
	 */
	protected DBDriver createTestDriver() {
		return new GenericRollbackDriver(createBaseDriver());
	}

	public DBDriver dbDriver() {
		return testDriver;
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
		if (testDriver instanceof GenericRollbackDriver) {
			GenericRollbackDriver.class.cast(testDriver).rollback();
		}
	}

}

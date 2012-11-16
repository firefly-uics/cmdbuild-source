package utils;

import static org.junit.Assert.fail;

import org.apache.commons.configuration.ConfigurationException;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.view.DBDataView;
import org.junit.After;
import org.junit.BeforeClass;

/**
 * Class containing methods for initializing the integration tests database
 */
public abstract class IntegrationTestBase {

	protected final GenericRollbackDriver rollbackDriver;
	protected final DBDataView dbView;

	protected IntegrationTestBase() {
		final DBDriver pgDriver = DBInitializer.getDBDriver();
		this.rollbackDriver = new GenericRollbackDriver(pgDriver);
		this.dbView = new DBDataView(rollbackDriver);
	}

	@BeforeClass
	public static void init() {
		try {
			DBInitializer.initDatabase();
		} catch (final ConfigurationException e) {
			e.printStackTrace();
			fail();
		}
	}

	@After
	public void rollback() {
		rollbackDriver.rollback();
	}

}

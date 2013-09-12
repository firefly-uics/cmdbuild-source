package utils;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.junit.AfterClass;
import org.springframework.jdbc.core.JdbcTemplate;

import utils.IntegrationTestBase;

public class IntegrationTestBimBase extends IntegrationTestBase {
	
	//Here we do not need the rollback driver.
	protected DBDriver createTestDriver() {
		return createBaseDriver();
	}

	protected JdbcTemplate jdbcTemplate(){
		if (dbDriver() instanceof PostgresDriver) {
			return PostgresDriver.class.cast(dbDriver()).getJdbcTemplate();
		}
		return null;
	}
	
	//Instead of doing the rollback, we drop the database.
	@AfterClass
	public static void dropDataBase() {
		dbInitializer.drop();
	}

}

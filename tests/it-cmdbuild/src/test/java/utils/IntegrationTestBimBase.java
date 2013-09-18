package utils;

import static org.mockito.Mockito.mock;

import javax.sql.DataSource;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.context.SystemPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.junit.AfterClass;
import org.springframework.jdbc.core.JdbcTemplate;

import utils.IntegrationTestBase;

public class IntegrationTestBimBase extends IntegrationTestBase {
	
	protected LookupLogic lookupLogic(){
		final AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
		final PrivilegeContext privilegeCtx = new SystemPrivilegeContext();
		final CMGroup cmGroup = mock(CMGroup.class);
		final OperationUser operationUser = new OperationUser(authenticatedUser, privilegeCtx, cmGroup);
		return new LookupLogic(lookupStore(), operationUser, dbDataView());
	}
	
	protected DataSource dataSource(){
		return jdbcTemplate().getDataSource();
	}
	
	
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

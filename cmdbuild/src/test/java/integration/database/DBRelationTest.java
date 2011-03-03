package integration.database;

import static org.junit.Assert.fail;
import integration.database.fixtures.DBDataFixture;
import integration.database.fixtures.DomainInfo;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.services.DBService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/*
 * This test is very fragile, because it cannot rely on the automatic transaction rollback
 */
@RunWith(value = Parameterized.class)
public class DBRelationTest extends DBDataFixture {

	private static final String C1 = "C1";
	private static final String C2 = "C2";

	private DomainInfo D;

	public DBRelationTest(DomainInfo domainInfo) {
		D = domainInfo;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ new DomainInfo("D_11", C1, C2, IDomain.CARDINALITY_11) },
			{ new DomainInfo("D_N1", C1, C2, IDomain.CARDINALITY_N1) },
			{ new DomainInfo("D_1N", C1, C2, IDomain.CARDINALITY_1N) },
			{ new DomainInfo("D_NN", C1, C2, IDomain.CARDINALITY_NN) }
		});
	}

	@Before
	public void setupClassHierarchy() throws SQLException {
		DBService.getConnection().setAutoCommit(true);
		createDBClass(C1);
		createDBClass(C2);
		createDBDomain(D);
	}

	@After
	public void tearDownClassHierarchy() throws SQLException {
		truncateDomain(D);
		truncateClass(C1);
		truncateClass(C2);
		deleteDBDomain(D);
		deleteDBClass(C1);
		deleteDBClass(C2);
		DBService.getConnection().setAutoCommit(false);
	}

	@Test
	public void shouldNotCreateDuplicateRelations() throws SQLException, InterruptedException {
		final int c1 = insertCardRow(C1);
		final int c2 = insertCardRow(C2);
		insertRelation(D, C1, c1, C2, c2);
		try {
			insertRelation(D, C1, c1, C2, c2);
			fail("Duplicate relation created without errors!");
		} catch(SQLException e) {};
	}

}

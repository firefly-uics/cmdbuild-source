package integrationNotWorking.database;

import static integrationNotWorking.database.matcher.SqlExceptionMatcher.hasType;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import integrationNotWorking.database.fixtures.DBDataFixture;
import integrationNotWorking.database.fixtures.DomainInfo;

import java.sql.SQLException;

import org.cmdbuild.dao.backend.postgresql.PGCMBackend.CMSqlException;
import org.cmdbuild.elements.interfaces.IDomain;
import org.junit.Ignore;
import org.junit.Test;

public class DBDomainTest extends DBDataFixture {

	protected static final String aClass = "AClass";
	protected static final String anotherClass = "AnotherClass";
	protected static final String aDomain = "ADomain";

	@Ignore // TODO: MAKE IT PASS!
	@Test
	public void domainCreationShouldBeConsistent() throws SQLException {
		try {
			createInconsistentDBDomain(aClass, "_"+anotherClass);
			fail();
		} catch (SQLException e) {
			assertThat(e, hasType(CMSqlException.CM_FORBIDDEN_OPERATION));
		}
		rollbackTransaction();
		try {
			createInconsistentDBDomain("_"+aClass, anotherClass);
			fail();
		} catch (SQLException e) {
			assertThat(e, hasType(CMSqlException.CM_FORBIDDEN_OPERATION));
		}
	}

	private void createInconsistentDBDomain(String commentClass1Name, String commentClass2Name) throws SQLException {
		createDBClass(aClass);
		createDBClass(anotherClass);
		DomainInfo domain = new DomainInfo(aDomain, aClass, anotherClass, IDomain.CARDINALITY_11);
		String domainComment = createDomainComment(domain.getName()+" Description",
				commentClass1Name, commentClass2Name, domain.getCardinality());
		createDBDomainWithComment(domain, domainComment);
	}

	@Ignore
	@Test
	public void relationsWorkOn11Domains() throws SQLException {
//		createRelationDBFixure(IDomain.CARDINALITY_11);		
//		int card1Id = insertCardRow(aClassName, DESCRIPTION_NULL);
//		int card2Id = insertCardRow(anotherClassName, DESCRIPTION_NULL);
//		TODO TEST CREATE RELATION WITH NOT EXISTENT CARDS
	}

//	private void createRelationDBFixure(String cardinality) throws SQLException {
//		createDBClass(anotherClassName, ITable.BaseTable);
//		createDBDomain(aDomainName, aClassName, anotherClassName, cardinality);
//	}
}

package integrationNotWorking.database;

import static integrationNotWorking.database.matcher.HasTriggerMatcher.hasTrigger;
import static integrationNotWorking.database.matcher.SqlExceptionMatcher.hasType;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import integrationNotWorking.database.fixtures.DBDataFixture;
import integrationNotWorking.database.fixtures.DomainInfo;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import org.cmdbuild.dao.backend.postgresql.PGCMBackend.CMSqlException;
import org.cmdbuild.elements.interfaces.IDomain;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@SuppressWarnings("serial")
@RunWith(value = Parameterized.class)
public class DBReferenceTest extends DBDataFixture {

	private static final String S1 = "S1";
	private static final String C11 = "C11";
	private static final String C12 = "C12";
	private static final String S2 = "S2";
	private static final String C21 = "C21";
	private static final String C22 = "C22";

	private static final String Reference = "Reference";

	private DomainInfo D;

	public DBReferenceTest(DomainInfo domainInfo) {
		D = domainInfo;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ new DomainInfo("D_N1", S1, S2, IDomain.CARDINALITY_N1) },
			{ new DomainInfo("D_1N", S2, S1, IDomain.CARDINALITY_1N) }
		});
	}

	private final String restrictTriggerName(String sourceClass, String referenceAttribute) {
		return String.format("_Constr_%s_%s", sourceClass, referenceAttribute);
	}

	private final String referenceTriggerName(String sourceClass, String referenceAttribute) {
		return String.format("_UpdRel_%s_%s", sourceClass, referenceAttribute);
	}

	@Before
	public void setupClassHierarchy() throws SQLException {
		createDBSuperClass(S1);
		createDBClass(C11, S1);
		createDBSuperClass(S2);
		createDBClass(C21, S2);
		createDBDomain(D);
	}

	@Test
	public void referenceCreationSetsTriggersAndConstraints() throws SQLException {
		createDBReference(S1, Reference, D);
		createDBClass(C12, S1);
		createDBClass(C22, S2);

		assertThat(S1, hasTrigger(referenceTriggerName(S1, Reference)));
		assertThat(C11, hasTrigger(referenceTriggerName(S1, Reference)));
		assertThat(C12, hasTrigger(referenceTriggerName(S1, Reference)));
		assertThat(S2, hasTrigger(restrictTriggerName(S1, Reference)));
		assertThat(C21, hasTrigger(restrictTriggerName(S1, Reference)));
		assertThat(C22, hasTrigger(restrictTriggerName(S1, Reference)));

		deleteDBAttribute(S1, Reference);

		assertThat(S1, not(hasTrigger(referenceTriggerName(S1, Reference))));
		assertThat(C11, not(hasTrigger(referenceTriggerName(S1, Reference))));
		assertThat(C12, not(hasTrigger(referenceTriggerName(S1, Reference))));
		assertThat(S2, not(hasTrigger(restrictTriggerName(S1, Reference))));
		assertThat(C21, not(hasTrigger(restrictTriggerName(S1, Reference))));
		assertThat(C22, not(hasTrigger(restrictTriggerName(S1, Reference))));
	}

	@Test
	public void aNewCardHasNoHistory() throws SQLException {
		createDBReference(C11, Reference, D);
		final int refTargetId = insertCardRow(C21);
		final int refSourceId = insertCardRow(C11, new HashMap<String, String>() {{
			put(Reference, Integer.toString(refTargetId));
		}});
		assertEquals(0, countHistoryItems(C11, refSourceId));
	}

	@Test
	public void restrictConstraintsAreEnforced() throws SQLException {
		createDBReference(C11, Reference, D);
		final int c21cardId = insertCardRow(C21);
		insertCardRow(C11, new TreeMap<String, String>() {
			{
				put(Reference, String.valueOf(c21cardId));
			}
		});

		try {
			updateCardRow(C21, c21cardId, STATUS_INACTIVE);
			fail();
		} catch (SQLException e) {
			assertThat(e, hasType(CMSqlException.CM_RESTRICT_VIOLATION));
		}
	}

	@Test
	public void relationOperationsUpdateReferences() throws SQLException {
		createDBReference(S1, Reference, D);
		final int refSourceAId = insertCardRow(C11);
		final int refSourceBId = insertCardRow(C11);
		final int refTargetAId = insertCardRow(C21);
//		final int refTargetBId = insertCardRow(C21);

		assertNull(getRowCardValues(C11, refSourceAId).get(Reference));
		assertNull(getRowCardValues(C11, refSourceBId).get(Reference));

//		final int relId = 
		insertReferenceRelation(D, C11, refSourceAId, C21, refTargetAId);

		assertEquals(refTargetAId, getRowCardValues(C11, refSourceAId).get(Reference));
		assertNull(getRowCardValues(C11, refSourceBId).get(Reference));

		// FIXME It's a transaction problem... let's try with savepoints
//		updateReferenceRelation(D, relId, C11, refSourceAId, C21, refTargetBId);
//		assertEquals(refTargetBId, getRowCardValues(C11, refSourceAId).get(Reference));
//		assertNull(getRowCardValues(C11, refSourceBId).get(Reference));
//
//		updateReferenceRelation(D, relId, C11, refSourceBId, C21, refTargetBId);
//		assertNull(getRowCardValues(C11, refSourceAId).get(Reference));
//		assertEquals(refTargetBId, getRowCardValues(C11, refSourceBId).get(Reference));
//
//		deleteRelation(D, relId);
//		assertNull(getRowCardValues(C11, refSourceAId).get(Reference));
//		assertNull(getRowCardValues(C11, refSourceBId).get(Reference));
	}

	@Ignore
	@Test
	public void referenceOperationsUpdateRelations() throws SQLException {
		// TODO
	}

	@Test
	public void classesCanBeDeletedWhenReferenceAttributesAreInherited() throws SQLException {
		createDBClass(C12, S1);
		createDBReference(S1, Reference, D);

		deleteDBClass(C11);

		assertThat(S1, hasTrigger(referenceTriggerName(S1, Reference)));
		assertThat(C12, hasTrigger(referenceTriggerName(S1, Reference)));
		assertThat(S2, hasTrigger(restrictTriggerName(S1, Reference)));
		assertThat(C21, hasTrigger(restrictTriggerName(S1, Reference)));
	}

}

package integration.database;

import static integration.database.matcher.IsParentTableOfMatcher.isParentTableOf;
import static integration.database.matcher.SqlExceptionMatcher.hasType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import integration.database.fixtures.DBDataFixture;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.CMBackend.CMSqlException;
import org.cmdbuild.dao.backend.postgresql.CMBackend.SqlState;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.junit.Ignore;
import org.junit.Test;

public class DBClassTest extends DBDataFixture {

	protected static final String aClass = "AClass";
	protected static final String aClassOnZzzSchema = "zzz.AClass";
	protected static final String anotherClass = "AnotherClass";

	@Test
	public void classCanBeCreatedAtBaseLevel() throws SQLException {
		createDBClass(aClass, ITable.BaseTable);
		assertThat(ITable.BaseTable, isParentTableOf(aClass));
		createDBSuperClass(anotherClass, ITable.BaseTable);
		assertThat(ITable.BaseTable, isParentTableOf(anotherClass));
	}

	@Test
	public void classCanBeCreatedOnADifferentSchema() throws SQLException {
		createDBClass(aClassOnZzzSchema, ITable.BaseTable);
		assertThat(ITable.BaseTable, isParentTableOf(aClassOnZzzSchema));
//		createDBSuperClass(anotherClass, aClassOnZzzSchema);
//		assertThat(anotherClass, isParentTableOf(aClassOnZzzSchema));
	}

	@Test
	public void legacyClassCreationShouldBeConsistent() throws SQLException {
		try {
			createInconsistentDBClass(true, false);
			fail();
		} catch (SQLException e) {
			assertThat(e, hasType(CMSqlException.CM_FORBIDDEN_OPERATION));
		}
		rollbackTransaction();
		try {
			createInconsistentDBClass(false, true);
			fail();
		} catch (SQLException e) {
			assertThat(e, hasType(CMSqlException.CM_FORBIDDEN_OPERATION));
		}
	}

	private void createInconsistentDBClass(boolean commentSuperClass, boolean parameterSuperClass) throws SQLException {
		String classComment = createClassComment(Mode.WRITE, aClass+" Description", commentSuperClass);
		legacyCreateDBClassWithComment(aClass, ITable.BaseTable, parameterSuperClass, classComment);
	}

	@Ignore
	@Test
	public void classModificationShouldBeConsistent() {
		// TODO
	}

	@Test
	public void idAndBeginDateAreSetOnInsert() throws SQLException {
		createDBClass(aClass);
		Date now = new java.util.Date();
		int cardId = insertCardRow(aClass);
		assertTrue(cardId > 0);

		Map<String, Object> rowValues = getRowCardValues(aClass, cardId);
		assertEquals(cardId, rowValues.get(CardAttributes.Id.toString()));
		assertNear(now, (java.sql.Timestamp)rowValues.get(CardAttributes.BeginDate.toString()), 3000);
	}

	private void assertNear(Date date1, Date date2, long tolerance) {
		long time1 = date1.getTime();
		long time2 = date2.getTime();
		if (time1 < time2) {
			assertTrue(time2 < time1 + tolerance);
		} else {
			assertTrue(time1 < time2 + tolerance);
		}
	}

	@Test
	public void insertNeedsClassId() throws SQLException {
		createDBClass(aClass);
		try {
			insertCardRowWithoutClassId(aClass);
			fail();
		} catch (SQLException e) {
			assertEquals(SqlState.not_null_violation.getErrorCode(), e.getSQLState());
		}
	}

	@Test
	public void updateAlwaysCreatesHistoryRow() throws SQLException {
		createDBClass(aClass);
		int cardId = insertCardRow(aClass, DESCRIPTION_NULL);
		assertEquals(0, countHistoryItems(aClass, cardId));
		updateCardRow(aClass, cardId, DESCRIPTION_NOT_NULL);
		assertEquals(1, countHistoryItems(aClass, cardId));
		assertEquals(null, getLastHistoryRowValues(aClass, cardId).get(CardAttributes.Description.toString()));
		// update creates a history row even if the data has not changed
		// the java code handles this by not issuing the update
		updateCardRow(aClass, cardId, DESCRIPTION_NOT_NULL);
		assertEquals(2, countHistoryItems(aClass, cardId));
		assertEquals(NOT_NULL_TEXT_VALUE, getLastHistoryRowValues(aClass, cardId).get(CardAttributes.Description.toString()));
	}

	@Test
	public void deleteIsProhibited() throws SQLException {
		createDBClass(aClass);
		int cardId = insertCardRow(aClass, DESCRIPTION_NULL);
		try {
			deleteCardRow(aClass, cardId);
			fail();
		} catch (SQLException e) {
			assertThat(e, hasType(CMSqlException.CM_FORBIDDEN_OPERATION));
			// TODO: should be CM_FORBIDDEN_OPERATION
			//assertEquals(SqlState.foreign_key_violation.getErrorCode(), e.getSQLState());
		}
	}

	@Test
	public void logicDeleteAddsHistoryRow() throws SQLException {
		createDBClass(aClass);
		int cardId = insertCardRow(aClass, DESCRIPTION_NULL);
		updateCardRow(aClass, cardId, STATUS_INACTIVE);
		assertEquals(
				ElementStatus.INACTIVE.value(),
				(String)getRowCardValues(aClass, cardId).get(CardAttributes.Status.toString())
			);
		assertEquals(1, countHistoryItems(aClass, cardId));
	}
}

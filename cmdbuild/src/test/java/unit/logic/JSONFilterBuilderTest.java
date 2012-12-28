package unit.logic;

import static org.junit.Assert.*;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.clause.where.AndWhereClause;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.OrWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.logic.FilterMapper;
import org.cmdbuild.logic.JSONFilterBuilder;
import org.cmdbuild.logic.JSONFilterMapper;
import org.cmdbuild.logic.WhereClauseBuilder;
import org.json.JSONObject;
import org.junit.Test;

public class JSONFilterBuilderTest {

	private static final CMClass entryType = DBClass.newClass().withName("Clazz").build();

	@Test(expected = IllegalArgumentException.class)
	public void notExpectedKeyShouldThrowException() throws Exception {
		// given
		String filter = "{not_expected_key: {attribute: Code, operator: contain, value: [od]}}";
		WhereClauseBuilder whereClauseBuilder = new JSONFilterBuilder(new JSONObject(filter), entryType);

		// when
		WhereClause wc = whereClauseBuilder.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void notExcpectedOperatorShouldThrowException() throws Exception {
		// given
		String filter = "{simple: {attribute: Code, operator: fake_operator, value: [od]}}";
		WhereClauseBuilder whereClauseBuilder = new JSONFilterBuilder(new JSONObject(filter), entryType);

		// when
		WhereClause wc = whereClauseBuilder.build();
	}

	@Test
	public void shouldSuccessfullyCreateSimpleWhereClause() throws Exception {
		// given
		String filter = "{simple: {attribute: Code, operator: contain, value: [od]}}";
		WhereClauseBuilder whereClauseBuilder = new JSONFilterBuilder(new JSONObject(filter), entryType);

		// when
		WhereClause wc = whereClauseBuilder.build();

		// then
		assertTrue(wc instanceof SimpleWhereClause);
	}

	@Test(expected = IllegalArgumentException.class)
	public void andClauseWithOnlyOneConditionShouldFail() throws Exception {
		// given
		String filter = "{and: [{simple: {attribute: Code, operator: contain, value: [od]}}]}";
		WhereClauseBuilder whereClauseBuilder = new JSONFilterBuilder(new JSONObject(filter), entryType);

		// when
		WhereClause wc = whereClauseBuilder.build();
	}

	@Test
	public void shouldSuccessfullyCreateAndWhereClause() throws Exception {
		// given
		String filter = "{and: [{simple: {attribute: Code, operator: contain, value: [od]}}, "
				+ "{simple: {attribute: Description, operator: like, value: [DEsc]}}]}";
		WhereClauseBuilder whereClauseBuilder = new JSONFilterBuilder(new JSONObject(filter), entryType);

		// when
		WhereClause wc = whereClauseBuilder.build();

		// then
		assertTrue(wc instanceof AndWhereClause);
	}

	@Test(expected = IllegalArgumentException.class)
	public void orClauseWithOnlyOneConditionShouldFail() throws Exception {
		// given
		String filter = "{or: [{simple: {attribute: Code, operator: contain, value: [od]}}]}";
		WhereClauseBuilder whereClauseBuilder = new JSONFilterBuilder(new JSONObject(filter), entryType);

		// when
		WhereClause wc = whereClauseBuilder.build();
	}

	@Test
	public void shouldSuccessfullyCreateOrWhereClause() throws Exception {
		// given
		String filter = "{or: [{simple: {attribute: Code, operator: contain, value: [od]}}, "
				+ "{simple: {attribute: Description, operator: like, value: [DEsc]}}]}";
		WhereClauseBuilder whereClauseBuilder = new JSONFilterBuilder(new JSONObject(filter), entryType);

		// when
		WhereClause wc = whereClauseBuilder.build();

		// then
		assertTrue(wc instanceof OrWhereClause);
	}

	@Test
	public void shouldSuccessfullyCreateAndWhereClauseWithMoreThanTwoConditions() throws Exception {
		// given
		String filter = "{and: [{simple: {attribute: Code, operator: contain, value: [od]}}, "
				+ "{simple: {attribute: Age, operator: greater, value: [5]}}, "
				+ "{simple: {attribute: Description, operator: like, value: [DEsc]}}]}";
		WhereClauseBuilder whereClauseBuilder = new JSONFilterBuilder(new JSONObject(filter), entryType);

		// when
		WhereClause wc = whereClauseBuilder.build();

		// then
		assertTrue(wc instanceof AndWhereClause);
		AndWhereClause awc = (AndWhereClause) wc;
		assertEquals(awc.getClauses().size(), 3);
	}

	@Test
	public void shouldSuccessfullyCreateOrWhereClauseWithMoreThanTwoConditions() throws Exception {
		// given
		String filter = "{or: [{simple: {attribute: Code, operator: contain, value: [od]}}, "
				+ "{simple: {attribute: Age, operator: greater, value: [5]}}, "
				+ "{simple: {attribute: Description, operator: like, value: [DEsc]}}]}";
		WhereClauseBuilder whereClauseBuilder = new JSONFilterBuilder(new JSONObject(filter), entryType);

		// when
		WhereClause wc = whereClauseBuilder.build();

		// then
		assertTrue(wc instanceof OrWhereClause);
		OrWhereClause owc = (OrWhereClause) wc;
		assertEquals(owc.getClauses().size(), 3);
	}

}
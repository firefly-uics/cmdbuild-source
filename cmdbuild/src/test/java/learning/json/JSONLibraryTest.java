package learning.json;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.*;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.*;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.*;
import static org.cmdbuild.dao.query.clause.where.NotWhereClause.*;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.*;
import static org.cmdbuild.dao.query.clause.where.NullOperatorAndValue.*;
import static org.cmdbuild.dao.query.clause.where.GreaterThanOperatorAndValue.*;
import static org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue.*;
import static org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue.*;
import static org.cmdbuild.dao.query.clause.where.BeginsWithOperatorAndValue.*;
import static org.cmdbuild.dao.query.clause.where.EndsWithOperatorAndValue.*;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.*;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.OperatorAndValue;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.google.common.collect.Lists;

public class JSONLibraryTest {

	@Test
	public void shouldDeserializeOnlyKeyValueJsonString() {
		// given
		String simpleJson = "{start: 0, limit: 20}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(simpleJson);
		} catch (JSONException e) {
			fail();
		}

		// then
		try {
			assertThat((Integer) object.get("start"), is(equalTo(0)));
			assertThat((Integer) object.get("limit"), is(equalTo(20)));
		} catch (JSONException e) {
			fail();
		}
	}

	@Test(expected = JSONException.class)
	public void shouldThrowExceptionIfNotExistentKey() throws JSONException {
		// given
		String simpleJson = "{start: 0, limit: 20}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(simpleJson);
		} catch (JSONException e) {
			fail();
		}

		// then
		Object notExistentObject = object.get("not_existent_key");
	}

	@Test
	public void shouldDeserializeJsonStringWithArrayOfValues() {
		// given
		String jsonString = "{start: 0, limit: 20, array: [10, 50, 100]}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(jsonString);
		} catch (JSONException e) {
			fail();
		}

		// then
		try {
			JSONArray deserializedArray = object.getJSONArray("array");
			int first = deserializedArray.getInt(0);
			assertEquals(first, 10);
		} catch (JSONException e) {
			fail();
		}
	}

	@Test
	public void shouldReturnFalseIfTheJsonObjectDoesNotHaveASpecificKey() {
		// given
		String jsonString = "{start: 0, limit: 20, array: [10, 50, 100]}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(jsonString);
		} catch (JSONException e) {
			fail();
		}

		// then
		assertFalse(object.has("not_existent_key"));
	}

	@Test
	public void shouldReturnTrueIfTheJsonObjectContainsASpecificKey() {
		// given
		String jsonString = "{start: 0, limit: 20, array: [10, 50, 100]}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(jsonString);
		} catch (JSONException e) {
			fail();
		}

		// then
		assertTrue(object.has("array"));
		assertTrue(object.has("start"));
	}

	@Test
	public void keysOfJsonObjectAreCaseSensitive() {
		// given
		String jsonString = "{start: 0, limit: 20, array: [10, 50, 100]}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(jsonString);
		} catch (JSONException e) {
			fail();
		}

		// then
		assertFalse(object.has("arRAy"));
	}

	@Test
	public void shouldDeserializeJsonStringWithArrayOfObjects() {
		// given
		String jsonString = "{start: 0, limit: 20, array: [{attribute: 'code', operator: 'equal', value: 5}, "
				+ "{attribute: 'description', operator: 'like', value: 'desc'}]}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(jsonString);
		} catch (JSONException e) {
			fail();
		}

		// then
		try {
			JSONArray deserializedArray = object.getJSONArray("array");
			JSONObject firstObject = deserializedArray.getJSONObject(0);
			String attributeName = firstObject.getString("attribute");
			String operator = firstObject.getString("operator");
			Object attributeValue = firstObject.get("value");
			assertEquals(attributeName, "code");
			assertEquals(operator, "equal");
			assertEquals(attributeValue, 5);
		} catch (JSONException e) {
			fail();
		}
	}

	/*
	 * Testes for FilterMapper...
	 */
	
	@Test
	public void shouldDeserializeACompositeFilterSpecifiedInJson() {
		// given
		String filterString = "{" + "filter: {"
				+ "and: [{simple: {attribute: 'code', operator: 'contain', value: ['st']}}, "
				+ "{simple: {attribute: 'description', operator: 'contain', value: ['esc']}}]" + "}" + "}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(filterString);
		} catch (JSONException e) {
			fail();
		}

		// then
		try {
			JSONObject filterObject = object.getJSONObject("filter");
			WhereClause wc = buildWhereClauseFromFilter(filterObject, "Table");
		} catch (JSONException e) {
			fail();
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailIfNotExistentOperator() {
		// given
		String filterString = "{" + "filter: {"
				+ "and: [{simple: {attribute: 'code', operator: 'fake_operator', value: ['st']}}, "
				+ "{simple: {attribute: 'description', operator: 'contain', value: ['esc']}}]" + "}" + "}";
		
		// when
		JSONObject object = null;
		try {
			object = new JSONObject(filterString);
		} catch (JSONException e) {
			fail();
		}
		
		// then
		try {
			JSONObject filterObject = object.getJSONObject("filter");
			WhereClause wc = buildWhereClauseFromFilter(filterObject, "Table");
		} catch (JSONException e) {
			fail();
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailIfMalformedFilter() {
		// given
		String filterString = "{" + "filter: {"
				+ "and: [{fake: {attribute: 'code', operator: 'like', value: ['st']}}, "
				+ "{bla: {attribute: 'description', operator: 'contain', value: ['esc']}}]" + "}" + "}";
		
		// when
		JSONObject object = null;
		try {
			object = new JSONObject(filterString);
		} catch (JSONException e) {
			fail();
		}
		
		// then
		try {
			JSONObject filterObject = object.getJSONObject("filter");
			WhereClause wc = buildWhereClauseFromFilter(filterObject, "Table");
		} catch (JSONException e) {
			fail();
		}
	}

	private WhereClause buildWhereClauseFromFilter(JSONObject filter, String entryTypeName) throws JSONException {
		// base case
		if (filter.has("simple")) {
			JSONObject simpleCondition = filter.getJSONObject("simple");
			String attributeName = simpleCondition.getString("attribute");
			String operator = simpleCondition.getString("operator");
			JSONArray values = simpleCondition.getJSONArray("value");
			return buildSimpleWhereClause(attribute(entryTypeName, attributeName), operator, values);
		} else if (filter.has("and")) {
			JSONArray andConditions = filter.getJSONArray("and");
			Validate.isTrue(andConditions.length() >= 2);
			JSONObject firstAnd = andConditions.getJSONObject(0);
			JSONObject secondAnd = andConditions.getJSONObject(1);
			return and(buildWhereClauseFromFilter(firstAnd, entryTypeName), //
					buildWhereClauseFromFilter(secondAnd, entryTypeName), //
					createOptionalWhereClauses(andConditions, entryTypeName));
		} else if (filter.has("or")) {
			JSONArray orConditions = filter.getJSONArray("and");
			Validate.isTrue(orConditions.length() >= 2);
			JSONObject firstAnd = orConditions.getJSONObject(0);
			JSONObject secondAnd = orConditions.getJSONObject(1);
			return or(buildWhereClauseFromFilter(firstAnd, entryTypeName), //
					buildWhereClauseFromFilter(secondAnd, entryTypeName), //
					createOptionalWhereClauses(orConditions, entryTypeName));
		}
		throw new IllegalArgumentException("The filter is not well formed");
	}

	private WhereClause buildSimpleWhereClause(QueryAliasAttribute attribute, String operator, JSONArray values)
			throws JSONException {
		if (operator.equals("equal")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, eq(values.get(0)));
		} else if (operator.equals("notequal")) {
			Validate.isTrue(values.length() == 1);
			return not(condition(attribute, eq(values.get(0))));
		} else if (operator.equals("null")) {
			Validate.isTrue(values.length() == 0);
			return condition(attribute, isNull());
		} else if (operator.equals("notnull")) {
			Validate.isTrue(values.length() == 0);
			return not(condition(attribute, isNull()));
		} else if (operator.equals("greater")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, gt(values.get(0)));
		} else if (operator.equals("less")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, lt(values.get(0)));
		} else if (operator.equals("between")) {
			Validate.isTrue(values.length() == 2);
			and(condition(attribute, gt(values.get(0))), condition(attribute, lt(values.get(1))));
		} else if (operator.equals("like")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, contains(values.get(0)));
		} else if (operator.equals("contain")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, contains(values.get(0)));
		} else if (operator.equals("notcontain")) {
			Validate.isTrue(values.length() == 1);
			return not(condition(attribute, contains(values.get(0))));
		} else if (operator.equals("begin")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, beginsWith(values.get(0)));
		} else if (operator.equals("notbegin")) {
			Validate.isTrue(values.length() == 1);
			return not(condition(attribute, beginsWith(values.get(0))));
		} else if (operator.equals("end")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, endsWith(values.get(0)));
		} else if (operator.equals("notend")) {
			Validate.isTrue(values.length() == 1);
			return not(condition(attribute, endsWith(values.get(0))));
		}
		throw new IllegalArgumentException("The operator " + operator + " is not supported");
	}
	

	private WhereClause[] createOptionalWhereClauses(JSONArray conditions, String entryTypeName) throws JSONException {
		List<WhereClause> optionalWhereClauses = Lists.newArrayList();
		for (int i = 2; i < conditions.length(); i++) {
			JSONObject andCond = conditions.getJSONObject(i);
			optionalWhereClauses.add(buildWhereClauseFromFilter(andCond, entryTypeName));
		}
		return optionalWhereClauses.toArray(new WhereClause[optionalWhereClauses.size()]);
	}

}
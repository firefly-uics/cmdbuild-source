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

	@Test
	public void shouldQuoteSpecialCharacters() throws Exception {
		// given
		String date = "16/12/1998";

		// when
		String quotedDate = JSONObject.quote(date);

		// then
		assertEquals("\"" + "16/12/1998" + "\"", quotedDate);
		JSONObject dateObjectCreatedSuccessfully = new JSONObject("{key: " + quotedDate + "}");
	}

	@Test(expected = Exception.class)
	public void shouldThrowExceptionIfUnquotedSpecialChar() throws Exception {
		// given
		String date = "16/12/1998";

		// when
		new JSONObject("{key: " + date + "}");
	}

}
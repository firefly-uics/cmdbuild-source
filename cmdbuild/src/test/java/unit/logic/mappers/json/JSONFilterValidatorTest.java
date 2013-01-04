package unit.logic.mappers.json;

import org.cmdbuild.logic.validators.JSONFilterValidator;
import org.cmdbuild.logic.validators.Validator;
import org.json.JSONObject;
import org.junit.Test;

public class JSONFilterValidatorTest {

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotValidateIfFilterIsNull() throws Exception {
		// given
		Validator validator = new JSONFilterValidator(null);

		// when
		validator.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotValidateIfFilterKeyDoesNotExist() throws Exception {
		// given
		JSONObject malformedFilter = new JSONObject("{not_existent_key: value}");
		Validator validator = new JSONFilterValidator(malformedFilter);

		// when
		validator.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotValidateIfFilterObjectDoesNotContainExpectedValue() throws Exception {
		// given
		JSONObject malformedFilter = new JSONObject("{filter: not_expected_value}");
		Validator validator = new JSONFilterValidator(malformedFilter);

		// when
		validator.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotValidateIfFilterObjectIsEmpty() throws Exception {
		// given
		JSONObject malformedFilter = new JSONObject("{filter: {}}");
		Validator validator = new JSONFilterValidator(malformedFilter);

		// when
		validator.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotValidateIfFilterObjectDoesNotContainCorrectKeys() throws Exception {
		// given
		JSONObject malformedFilter = new JSONObject("{filter: {not_expected_key: bla, not_expected_2: blabla}}");
		Validator validator = new JSONFilterValidator(malformedFilter);

		// when
		validator.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotValidateIfAttributeObjectIsEmpty() throws Exception {
		// given
		JSONObject malformedFilter = new JSONObject("{filter: {attribute: {}}}");
		Validator validator = new JSONFilterValidator(malformedFilter);

		// when
		validator.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotValidateIfAttributeObjectDoesNotContainCorrectKeys() throws Exception {
		// given
		JSONObject malformedFilter = new JSONObject("{filter: {attribute: {not_expected: 1, not_exp: 2}}}");
		Validator validator = new JSONFilterValidator(malformedFilter);

		// when
		validator.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotValidateIfSImpleObjectDoesNotContainCorrectKeys() throws Exception {
		// given
		JSONObject malformedFilter = new JSONObject("{filter: {attribute: {simple: {}}}}");
		Validator validator = new JSONFilterValidator(malformedFilter);

		// when
		validator.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotValidateIfSimpleObjectDoesNotContainAllCorrectKeys() throws Exception {
		// given
		JSONObject malformedFilter = new JSONObject(
				"{filter: {attribute: {simple: {not_expected_key: 1, attribute: bla, operator: equal}}}}");
		Validator validator = new JSONFilterValidator(malformedFilter);

		// when
		validator.validate();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldNotValidateFilterWithNotValidValuesInSimpleConditions() throws Exception {
		// given
		JSONObject malformedFilter = new JSONObject(
				"{filter: {attribute: {simple: {attribute: bla, operator: equal, value: bla}}}}");
		Validator validator = new JSONFilterValidator(malformedFilter);
		
		// when
		validator.validate();
	}
	
	@Test
	public void shouldValidateFilterWithAttributeConditions() throws Exception {
		// given
		JSONObject correctFilter = new JSONObject(
				"{filter: {attribute: {simple: {attribute: bla, operator: equal, value: [1]}}}}");
		Validator validator = new JSONFilterValidator(correctFilter);
		
		// when
		validator.validate();
	}
	
	@Test
	public void shouldValidateFilterWithAttributeConditionsAndQueryCondition() throws Exception {
		// given
		JSONObject correctFilter = new JSONObject(
				"{filter: {attribute: {simple: {attribute: bla, operator: equal, value: [1]}}, query: full_text_query}}");
		Validator validator = new JSONFilterValidator(correctFilter);
		
		// when
		validator.validate();
	}


}
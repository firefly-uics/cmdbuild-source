package org.cmdbuild.logic.validation.json;

import static com.google.common.collect.Iterators.contains;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.cmdbuild.logic.mapping.json.Constants.AND_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.CARDS_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.CQL_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.DOMAIN_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.FULL_TEXT_QUERY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.OR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.RELATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.SIMPLE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.SRC_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.TYPE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.VALUE_KEY;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.logic.validation.Validator;
import org.cmdbuild.logic.validation.Validator.ValidationError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonFilterValidator implements Validator {

	private static final String MALFORMED_MSG = "Malformed filter";
	private final JSONObject filterObject;

	public JsonFilterValidator(final JSONObject filter) {
		try {
			Validate.notNull(filter);
			this.filterObject = filter;
		} catch (final Exception e) {
			throw new ValidationError(e);
		}
	}

	@Override
	public void validate() throws ValidationError {
		try {
			final boolean isEmptyFilter = !filterObject.keys().hasNext();
			if (!isEmptyFilter) {
				validateInnerFilterObjects();
			}
		} catch (final Exception e) {
			throw new ValidationError(e);
		}
	}

	private void validateInnerFilterObjects() throws Exception {
		Validate.isTrue(jsonHasOneOfKeys(filterObject, //
				ATTRIBUTE_KEY, FULL_TEXT_QUERY_KEY, RELATION_KEY, CQL_KEY), MALFORMED_MSG);
		validateAttributeFilter(filterObject);
		validateQueryFilter(filterObject);
		validateRelationFilter(filterObject);
		validateCQLFilter(filterObject);
	}

	private void validateAttributeFilter(final JSONObject filterObject) throws JSONException {
		if (filterObject.has(ATTRIBUTE_KEY)) {
			final JSONObject attributeFilter = filterObject.getJSONObject(ATTRIBUTE_KEY);
			Validate.isTrue(jsonHasOneOfKeys(attributeFilter, SIMPLE_KEY, AND_KEY, OR_KEY), MALFORMED_MSG);
			if (attributeFilter.has(SIMPLE_KEY)) {
				final JSONObject simpleClauseObject = attributeFilter.getJSONObject(SIMPLE_KEY);
				Validate.isTrue(jsonHasAllKeys(simpleClauseObject, //
						ATTRIBUTE_KEY, OPERATOR_KEY, VALUE_KEY), MALFORMED_MSG);
				validateSimpleClauseValues(simpleClauseObject);
			}
		}
	}

	private void validateSimpleClauseValues(final JSONObject simpleClauseObject) throws JSONException {
		simpleClauseObject.getJSONArray(VALUE_KEY);
	}

	private void validateQueryFilter(final JSONObject filterObject) throws JSONException {
		if (filterObject.has(FULL_TEXT_QUERY_KEY)) {
			filterObject.getString(FULL_TEXT_QUERY_KEY);
		}
	}

	private void validateRelationFilter(final JSONObject filterObject) throws JSONException {
		if (filterObject.has(RELATION_KEY)) {
			final JSONArray conditions = filterObject.getJSONArray(RELATION_KEY);
			for (int i = 0; i < conditions.length(); i++) {
				final JSONObject condition = conditions.getJSONObject(i);
				Validate.isTrue(jsonHasAllKeys(condition, DOMAIN_KEY, SRC_KEY, TYPE_KEY), MALFORMED_MSG);
				Validate.isTrue(isNotBlank((String) condition.get(DOMAIN_KEY)), MALFORMED_MSG);
				Validate.isTrue(asList("_1", "_2").contains(condition.get(SRC_KEY)), MALFORMED_MSG);
				Validate.isTrue(asList("any", "noone", "oneof").contains(condition.get(TYPE_KEY)), MALFORMED_MSG);
				if ("oneof".equals(condition.get(TYPE_KEY))) {
					Validate.isTrue(condition.has(CARDS_KEY), MALFORMED_MSG);
					final JSONArray cards = condition.getJSONArray(CARDS_KEY);
					Validate.isTrue(cards.length() > 0);
					for (int j = 0; j < cards.length(); j++) {
						final JSONObject card = cards.getJSONObject(j);
						Validate.isTrue(jsonHasAllKeys(card, "Id", "ClassName"), MALFORMED_MSG);
						Validate.isTrue(card.getInt("Id") > 0, MALFORMED_MSG);
						Validate.isTrue(isNotBlank((String) card.get("ClassName")), MALFORMED_MSG);
					}
				}
			}

		}
	}

	private void validateCQLFilter(final JSONObject filterObject) {
		// empty until CQL filter will be implemented
	}

	private boolean jsonHasOneOfKeys(final JSONObject jsonObject, final String... keys) {
		return jsonHasOneOfKeys(jsonObject, asList(keys));
	}

	private boolean jsonHasOneOfKeys(final JSONObject jsonObject, final List<String> keys) {
		final Iterator keysIterator = jsonObject.keys();
		while (keysIterator.hasNext()) {
			final String key = (String) keysIterator.next();
			if (keys.contains(key)) {
				return true;
			}
		}
		return false;
	}

	private boolean jsonHasAllKeys(final JSONObject jsonObject, final String... keys) {
		return jsonHasAllKeys(jsonObject, asList(keys));
	}

	private boolean jsonHasAllKeys(final JSONObject jsonObject, final List<String> keys) {
		for (final String key : keys) {
			if (!contains(jsonObject.keys(), key)) {
				return false;
			}
		}
		return true;
	}

}

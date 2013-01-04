package org.cmdbuild.logic.mappers.json;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.logic.mappers.FilterValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class JSONFilterValidator implements FilterValidator {

	private static final String FILTER_KEY = "filter";
	private static final String ATTRIBUTE_KEY = "attribute";
	private static final String FULL_TEXT_QUERY_KEY = "query";
	private static final String RELATION_KEY = "relation";
	private static final String CQL_KEY = "CQL";
	private static final String MALFORMED_MSG = "Malformed filter";
	private JSONObject globalFilterObject;

	public JSONFilterValidator(JSONObject filter) {
		Validate.notNull(filter);
		this.globalFilterObject = filter;
	}

	public void validate() throws IllegalArgumentException {
		boolean isEmptyFilter = !globalFilterObject.keys().hasNext();
		if (!isEmptyFilter) {
			validateGlobalFilterObject();
			validateInnerFilterObjects();
		}
	}

	private void validateGlobalFilterObject() {
		Validate.notNull(globalFilterObject);
		if (!globalFilterObject.has(FILTER_KEY)) {
			throw new IllegalArgumentException(MALFORMED_MSG);
		}
	}

	private void validateInnerFilterObjects() {
		try {
			JSONObject filterObject = globalFilterObject.getJSONObject(FILTER_KEY);
			List<String> possibleKeys = Lists.newArrayList(ATTRIBUTE_KEY, FULL_TEXT_QUERY_KEY, RELATION_KEY, CQL_KEY);
			if (!jsonObjectContainsAnyOf(filterObject, possibleKeys)) {
				throw new IllegalArgumentException(MALFORMED_MSG);
			}
			validateAttributeFilter(filterObject);
			validateQueryFilter(filterObject);
			validateRelationFilter(filterObject);
			validateCQLFilter(filterObject);
		} catch (JSONException ex) {
			throw new IllegalArgumentException(MALFORMED_MSG);
		}
	}

	private void validateAttributeFilter(JSONObject filterObject) throws JSONException {
		if (filterObject.has(ATTRIBUTE_KEY)) {
			JSONObject attributeFilter = filterObject.getJSONObject(ATTRIBUTE_KEY);
			List<String> possibleKeys = Lists.newArrayList("simple", "and", "or");
			if (!jsonObjectContainsAnyOf(attributeFilter, possibleKeys)) {
				throw new IllegalArgumentException(MALFORMED_MSG);
			}
			if (attributeFilter.has("simple")) {
				JSONObject simpleClauseObject = attributeFilter.getJSONObject("simple");
				List<String> simpleClausePossibleKeys = Lists.newArrayList("attribute", "operator", "value");
				if (!jsonObjectContainsAll(simpleClauseObject, simpleClausePossibleKeys)) {
					throw new IllegalArgumentException(MALFORMED_MSG);
				}
				validateSimpleClauseValues(simpleClauseObject);
			}
		}
	}

	private boolean jsonObjectContainsAnyOf(JSONObject jsonObject, List<String> possibleKeys) {
		Iterator keysIterator = jsonObject.keys();
		while (keysIterator.hasNext()) {
			String key = (String) keysIterator.next();
			if (possibleKeys.contains(key)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean jsonObjectContainsAll(JSONObject jsonObject, List<String> possibleKeys) {
		Iterator keysIterator = jsonObject.keys();
		while (keysIterator.hasNext()) {
			String key = (String) keysIterator.next();
			if (!possibleKeys.contains(key)) {
				return false;
			}
		}
		return true;
	}
	
	private void validateSimpleClauseValues(JSONObject simpleClauseObject) throws JSONException {
		JSONArray values = simpleClauseObject.getJSONArray("value");
	}

	private void validateQueryFilter(JSONObject filterObject) throws JSONException {
		if (filterObject.has(FULL_TEXT_QUERY_KEY)) {
			String value = filterObject.getString(FULL_TEXT_QUERY_KEY);
		}
	}

	private void validateRelationFilter(JSONObject filterObject) {
		// empty until relation filter will be implemented
	}

	private void validateCQLFilter(JSONObject filterObject) {
		// empty until CQL filter will be implemented
	}

}

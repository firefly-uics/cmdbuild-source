package org.cmdbuild.services.soap.utils;

import static org.cmdbuild.logic.mapping.json.Constants.DIRECTION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.PROPERTY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.CQL_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FULL_TEXT_QUERY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.*;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.CQLQuery;
import org.cmdbuild.services.soap.types.Filter;
import org.cmdbuild.services.soap.types.FilterOperator;
import org.cmdbuild.services.soap.types.Order;
import org.cmdbuild.services.soap.types.Query;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a mapper used to convert from {@link Attribute} to
 * {@link JSONArray} and vice versa
 */
public class SoapToJsonUtils {

	private SoapToJsonUtils() {
		// prevents instantiation
	}

	/**
	 * 
	 * @param attributes
	 *            an array of Attribute objects (SOAP)
	 * @return a JSONArray containing the names of the attributes
	 */
	public static JSONArray toJsonArray(final Attribute[] attributes) {
		JSONArray jsonArray = new JSONArray();
		if (attributes != null) {
			for (Attribute soapAttribute : attributes) {
				jsonArray.put(soapAttribute.getName());
			}
		}
		return jsonArray;

	}

	public static JSONArray toJsonArray(final Order[] sorters, final Attribute[] attributesSubsetForSelect) {
		final JSONArray jsonArray = new JSONArray();
		if (sorters != null) {
			for (final Order order : sorters) {
				JSONObject object = new JSONObject();
				try {
					object.put(PROPERTY_KEY, order.getColumnName());
					object.put(DIRECTION_KEY, order.getType());
				} catch (JSONException e) {
					// empty
				}
				jsonArray.put(object);
			}
		} else {
			if (attributesSubsetForSelect != null && attributesSubsetForSelect.length > 0) {
				Attribute attributeUsedForOrder = attributesSubsetForSelect[0];
				JSONObject object = new JSONObject();
				try {
					object.put(PROPERTY_KEY, attributeUsedForOrder.getName());
					object.put(DIRECTION_KEY, "ASC");
				} catch (JSONException e) {
					// empty
				}
				jsonArray.put(object);
			}
		}
		return jsonArray;
	}

	public static JSONObject createJsonFilterFrom(final Query queryType, final String fullTextQuery,
			final CQLQuery cqlQuery) {
		final JSONObject filterObject = new JSONObject();
		try {
			if (queryType != null) {
				JSONObject attributeFilterObject = jsonQuery(queryType);
				filterObject.put(ATTRIBUTE_KEY, attributeFilterObject);
			}
			if (StringUtils.isNotBlank(fullTextQuery)) {
				filterObject.put(FULL_TEXT_QUERY_KEY, fullTextQuery);
			}
			if (cqlQuery != null) {
				filterObject.put(CQL_KEY, cqlQuery);
			}
		} catch (JSONException ex) {

		}
		return filterObject;
	}

	private static JSONObject jsonQuery(final Query query) throws JSONException {
		final JSONObject jsonObject = new JSONObject();
		final Filter filter = query.getFilter();
		final FilterOperator filterOperator = query.getFilterOperator();
		if (filter != null) {
			final JSONArray values = new JSONArray();
			for (final String value : filter.getValue()) {
				values.put(value);
			}
			final JSONObject simple = new JSONObject();
			simple.put(ATTRIBUTE_KEY, filter.getName());
			simple.put(OPERATOR_KEY, filter.getOperator());
			simple.put(VALUE_KEY, values);
			jsonObject.put(SIMPLE_KEY, simple);
		} else if (filterOperator != null) {
			final String operator = LogicalOperatorMapper.of(filterOperator.getOperator()).getJson();
			final JSONArray jsonSubQueries = new JSONArray();
			for (final Query subQuery : filterOperator.getSubquery()) {
				jsonSubQueries.put(jsonQuery(subQuery));
			}
			jsonObject.put(operator, jsonSubQueries);
		}
		return jsonObject;
	}

	private static enum LogicalOperatorMapper {
		AND(AND_KEY), //
		OR(OR_KEY), //
		;

		private final String json;

		private LogicalOperatorMapper(final String json) {
			this.json = json;
		}

		public String getJson() {
			return json;
		}

		public static LogicalOperatorMapper of(final String s) {
			for (final LogicalOperatorMapper value : values()) {
				if (value.json.equalsIgnoreCase(s)) {
					return value;
				}
			}
			throw new IllegalArgumentException();
		}

	}

}

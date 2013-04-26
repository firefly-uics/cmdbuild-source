package org.cmdbuild.servlets.json.util;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.EQUAL;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.IN;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.AND_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.SIMPLE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logger.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class FlowStatusHelper {

	private static final Logger logger = Log.JSONRPC;
	private static final Marker marker = MarkerFactory.getMarker(FlowStatusHelper.class.getName());

	private final LookupStore lookupStore;

	public FlowStatusHelper(final LookupStore lookupStore) {
		this.lookupStore = lookupStore;
	}

	/**
	 * Merges the actual filter with the received flow-status.
	 * 
	 * @param filter
	 *            the actual value of filter as received.
	 * @param flowStatus
	 *            the value of the flow-status as expressed in the Code
	 *            attribute of the LookUp table.
	 * 
	 * @return the resulting filter with, eventually, the condition related to
	 *         the flow-status.
	 * 
	 * @throws JSONException
	 */
	public JSONObject merge(final JSONObject filter, final String flowStatus) throws JSONException {
		logger.info(marker, "adding flow status condition '{}' to actual filter '{}'", flowStatus, filter);
		if (isBlank(flowStatus)) {
			logger.debug(marker, "missing flow status");
			return filter;
		}
		final JSONObject filterWithFlowStatus = (filter == null) ? new JSONObject() : filter;
		final JSONObject attribute;
		if (filterWithFlowStatus.has(ATTRIBUTE_KEY)) {
			attribute = filter.getJSONObject(ATTRIBUTE_KEY);
		} else {
			logger.debug(marker, "filter has no element '{}' adding an empty one", ATTRIBUTE_KEY);
			attribute = new JSONObject();
			filterWithFlowStatus.put(ATTRIBUTE_KEY, attribute);
		}

		if (attribute.has(AND_KEY) || attribute.has(OR_KEY)) {
			logger.debug(marker, "attribute element has 'and' or 'or sub-elements");
			final JSONArray actual = attribute.has(AND_KEY) ? attribute.getJSONArray(AND_KEY) : attribute
					.getJSONArray(OR_KEY);
			final JSONArray arrayWithFlowStatus = new JSONArray();
			arrayWithFlowStatus.put(actual);
			arrayWithFlowStatus.put(simple(flowStatusFilterElement(flowStatus)));
			attribute.put(AND_KEY, arrayWithFlowStatus);
		} else if (attribute.has(SIMPLE_KEY)) {
			logger.debug(marker, "attribute element has 'simple' sub-element");
			final JSONObject actual = attribute.getJSONObject(SIMPLE_KEY);
			final JSONArray arrayWithFlowStatus = new JSONArray();
			arrayWithFlowStatus.put(simple(actual));
			arrayWithFlowStatus.put(simple(flowStatusFilterElement(flowStatus)));
			attribute.put(AND_KEY, arrayWithFlowStatus);
			attribute.remove(SIMPLE_KEY);
		} else {
			logger.debug(marker, "attribute element is empty");
			attribute.put(SIMPLE_KEY, flowStatusFilterElement(flowStatus));
		}

		logger.debug(marker, "resulting filter is '{}'", filterWithFlowStatus);

		return filterWithFlowStatus;
	}

	private JSONObject simple(final JSONObject jsonObject) throws JSONException {
		return new JSONObject() {
			{
				put(SIMPLE_KEY, jsonObject);
			}
		};
	}

	private JSONObject flowStatusFilterElement(final String flowStatus) throws JSONException {
		logger.debug(marker, "creating JSON flow status element for '{}'", flowStatus);
		final JSONArray singleValue = new JSONArray();
		final JSONArray allValues = new JSONArray();
		for (final Lookup element : lookupStore.listForType(LookupType.newInstance() //
				.withName("FlowStatus") //
				.build())) {
			if (element.code.equals(flowStatus)) {
				logger.debug(marker, "lookup found for flow status '{}'", flowStatus);
				singleValue.put(element.id);
			}
			allValues.put(element.id);
		}

		final JSONObject simple;
		simple = new JSONObject();
		simple.put(ATTRIBUTE_KEY, "FlowStatus");
		simple.put(OPERATOR_KEY, (singleValue.length() == 1) ? EQUAL : IN);
		simple.put(VALUE_KEY, (singleValue.length() == 1) ? singleValue : allValues);

		logger.debug(marker, "resulting element is '{}'", simple);

		return simple;
	}

}

package org.cmdbuild.servlets.json.serializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class JsonHistory {

	protected static class ValueAndDescription {
		private Object value;
		private String description;
		ValueAndDescription(Object value, String description) {
			this.value = value;
			this.description = description;
		}
		Object getValue() {
			return value;
		}
		String getDescription() {
			return description;
		}
	}

	protected interface HistoryItem {
		Object getId();
		DateTime getBeginDate();
		Map<String,ValueAndDescription> getAttributes();
		Map<String,Object> getExtraAttributes();
		boolean isInOutput();
	}

	private List<HistoryItem> list = new ArrayList<HistoryItem>(); // TODO

	public JSONArray toJson() throws JSONException {
		final JSONArray jsonArray = new JSONArray();
		addJsonHistoryItems(jsonArray);
		return jsonArray;
	}

	public void addJsonHistoryItems(final JSONArray jsonArray) throws JSONException {
		for (HistoryItem hi : list) {
			if (hi.isInOutput()) {
				jsonArray.put(historyItemToJson(hi));
			}
		}
	}

	protected final void addHistoryItem(HistoryItem hi) {
		list.add(hi);
	}

	protected final JSONObject historyItemToJson(HistoryItem hi) throws JSONException {
		JSONObject jsonHistoryItem = new JSONObject();
		for (Map.Entry<String,Object> entry : hi.getExtraAttributes().entrySet()) {
			jsonHistoryItem.put(entry.getKey(), entry.getValue());
		}
		jsonHistoryItem.put("Attr", historyItemAttributesToJson(hi));
		return jsonHistoryItem;
	}

	private JSONArray historyItemAttributesToJson(HistoryItem hi) throws JSONException {
		final JSONArray jsonAttr = new JSONArray();
		for (Map.Entry<String,ValueAndDescription> entry : hi.getAttributes().entrySet()) {
			final JSONObject jsonAttrValue = new JSONObject();
			final ValueAndDescription vad = entry.getValue();
			jsonAttrValue.put("d", vad.getDescription());
			jsonAttrValue.put("v", vad.getValue());
			//jsonAttrValue.put("c", TODO: CHANGED); // uses entry.getKey();
			jsonAttr.put(jsonAttrValue);
		}
		return jsonAttr;
	}
}

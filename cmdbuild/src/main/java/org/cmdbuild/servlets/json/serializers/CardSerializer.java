package org.cmdbuild.servlets.json.serializers;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CardSerializer {

	// TODO continue the implementation,
	// pay attention to lookup and references

	public static JSONObject toClient(CMCard card) throws JSONException {
		final JSONObject json = new JSONObject();

		// add the attributes
		for(Map.Entry<String, Object> entry : card.getValues()) {
			json.put(entry.getKey(), entry.getValue());
		}

		// add some required info
		json.put("Id", card.getId());
		json.put("IdClass", card.getType().getId());

		return json;
	}

	public static JSONObject toClient(List<CMCard> cards) throws JSONException {
		JSONObject json = new JSONObject();
		JSONArray jsonRows = new JSONArray();
		for (CMCard card : cards) {
			jsonRows.put(CardSerializer.toClient(card));
		}
		json.put("rows", jsonRows);
		return json;
	}
}

package org.cmdbuild.servlets.json.serializers;

import java.util.Map;

import static org.cmdbuild.servlets.json.ComunicationConstants.*;

import org.cmdbuild.model.data.Card;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CardSerializer {

	// TODO continue the implementation,
	// pay attention to lookup and references

	public static JSONObject toClient(final Card card, final String wrapperLabel) throws JSONException {
		final JSONObject json = new JSONObject();

		// add the attributes
		for (final Map.Entry<String, Object> entry : card.getAttributes().entrySet()) {
			json.put(entry.getKey(), entry.getValue());
		}

		// add some required info
		json.put(ID_CAPITAL, card.getId());
		json.put(CLASS_ID_CAPITAL, card.getClassId());

		// wrap in a JSON object if required
		if (wrapperLabel != null) {
			final JSONObject wrapper = new JSONObject();
			wrapper.put(wrapperLabel, json);
			return wrapper;
		} else {
			return json;
		}
	}

	public static JSONObject toClient(final Card card) throws JSONException {
		return toClient(card, null);
	}

	public static JSONObject toClient( //
			final Iterable<Card> cards, //
			final int totalSize //
		) throws JSONException {

		return toClient(cards, totalSize, ROWS);
	}

	public static JSONObject toClient( //
			final Iterable<Card> cards, //
			final int totalSize, //
			final String cardsLabel //
		) throws JSONException {

		final JSONObject json = new JSONObject();
		final JSONArray jsonRows = new JSONArray();
		for (final Card card : cards) {
			jsonRows.put(CardSerializer.toClient(card));
		}

		json.put(cardsLabel, jsonRows);
		json.put(RESULTS, totalSize);
		return json;
	}

}

package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.SOURCE_CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.SOURCE_FUNCTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.ComunicationConstants.VIEWS;

import java.util.List;

import org.cmdbuild.model.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewSerializer {

	public static JSONObject toClient(List<View> views) throws JSONException {
		final JSONObject out = new JSONObject();
		final JSONArray jsonViews = new JSONArray();

		for (View view: views) {
			jsonViews.put(toClient(view));
		}

		out.put(VIEWS, jsonViews);

		return out;
	}

	public static JSONObject toClient(View view) throws JSONException {
		final JSONObject jsonView = new JSONObject();
		jsonView.put(DESCRIPTION, view.getDescription());
		jsonView.put(FILTER, view.getFilter());
		jsonView.put(ID, view.getId());
		jsonView.put(NAME, view.getName());
		jsonView.put(SOURCE_CLASS_NAME, view.getSourceClassName());
		jsonView.put(SOURCE_FUNCTION, view.getSourceFunction());
		jsonView.put(TYPE, view.getType().toString());

		return jsonView;
	}
}

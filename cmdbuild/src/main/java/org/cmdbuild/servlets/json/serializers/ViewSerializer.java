package org.cmdbuild.servlets.json.serializers;

import java.util.List;

import org.cmdbuild.model.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static org.cmdbuild.servlets.json.ComunicationConstants.*;

public class ViewSerializer {

	public static JSONObject toClient(List<View> views) throws JSONException {
		final JSONObject out = new JSONObject();
		final JSONArray jsonViews = new JSONArray();

		for (View view: views) {
			jsonViews.put(toClient(view));
		}

		out.put(PARAMETER_VIEWS, jsonViews);

		return out;
	}

	public static JSONObject toClient(View view) throws JSONException {
		final JSONObject jsonView = new JSONObject();
		jsonView.put(PARAMETER_DESCRIPTION, view.getDescription());
		jsonView.put(PARAMETER_FILTER, view.getFilter());
		jsonView.put(PARAMETER_ID, view.getId());
		jsonView.put(PARAMETER_NAME, view.getName());
		jsonView.put(PARAMETER_SOURCE_CLASS_NAME, view.getSourceClassName());
		jsonView.put(PARAMETER_SOURCE_FUNCTION, view.getSourceFunction());
		jsonView.put(PARAMETER_TYPE, view.getType().toString());

		return jsonView;
	}
}

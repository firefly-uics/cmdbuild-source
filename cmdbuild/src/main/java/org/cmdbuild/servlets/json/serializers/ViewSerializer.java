package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.DESCRIPTION_FOR_CLIENT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE_CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE_FUNCTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.VIEWS;

import java.util.List;

import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.ViewTranslation;
import org.cmdbuild.model.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewSerializer {

	private final TranslationFacade translationFacade;

	public ViewSerializer(final TranslationFacade translationFacade) {
		this.translationFacade = translationFacade;
	}

	public JSONObject toClient(final List<View> views) throws JSONException {
		final JSONObject out = new JSONObject();
		final JSONArray jsonViews = new JSONArray();

		for (final View view : views) {
			jsonViews.put(toClient(view));
		}

		out.put(VIEWS, jsonViews);

		return out;
	}

	public JSONObject toClient(final View view) throws JSONException {
		final JSONObject jsonView = new JSONObject();
		final ViewTranslation translationObject = ViewTranslation.newInstance() //
				.withField(DESCRIPTION_FOR_CLIENT) //
				.withName(view.getName()) //
				.build();
		final String translatedDescription = translationFacade.read(translationObject);

		jsonView.put(DESCRIPTION, defaultIfNull(translatedDescription, view.getDescription()));
		jsonView.put(DEFAULT_DESCRIPTION, view.getDescription());
		jsonView.put(FILTER, view.getFilter());
		jsonView.put(ID, view.getId());
		jsonView.put(NAME, view.getName());
		jsonView.put(SOURCE_CLASS_NAME, view.getSourceClassName());
		jsonView.put(SOURCE_FUNCTION, view.getSourceFunction());
		jsonView.put(TYPE, view.getType().toString());

		return jsonView;
	}
}

package org.cmdbuild.servlets.json.administration.task;

import static org.cmdbuild.servlets.json.ComunicationConstants.CARD_ID;

import java.util.Map;

import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;

public class Mail extends JSONBaseWithSpringContext {
	private class AMail {
		private long id;
		private String type;
		private Boolean active;
		private String last;//date "dd/mm/yyyy hh:mm";
		private String next;//date "dd/mm/yyyy hh:mm";
	}
	@JSONExported
	public JsonResponse get( //
			@Parameter(value = CARD_ID) final Long cardId //
	) throws JSONException {

		return null;
	}
	@JSONExported
	public JsonResponse delete( //
			@Parameter(value = CARD_ID) final Long cardId //
	) throws JSONException {

		return null;
	}
	@JSONExported
	public JsonResponse post( //
			final Map<String, Object> attributes
	) throws JSONException {

		return null;
	}
	@JSONExported
	public JsonResponse put( //
			@Parameter(value = CARD_ID) final Long cardId, //
			final Map<String, Object> attributes
	) throws JSONException {

		return null;
	}

}

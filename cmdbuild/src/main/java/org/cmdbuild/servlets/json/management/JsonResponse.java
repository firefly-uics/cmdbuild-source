package org.cmdbuild.servlets.json.management;

import org.codehaus.jackson.map.ObjectMapper;

public class JsonResponse {

	private boolean success;
	private Object response;

	public boolean isSuccess() {
		return success;
	}

	public Object getResponse() {
		return response;
	}

	public static JsonResponse success(final Object response) {
		final JsonResponse responseObject = new JsonResponse();
		responseObject.success = true;
		responseObject.response = response;
		return responseObject;
	}

	/**
	 * Use only in the JsonDispatcher, remove when switching to Spring MVC.
	 */
	public String toString() {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			return "Error serializing the object!";
		}
	}
}
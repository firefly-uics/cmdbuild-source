package org.cmdbuild.servlets.json;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.Response;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.logic.auth.LoginDTO.LoginDTOBuilder;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Login extends JSONBase {

	@JSONExported
	@Unauthorized
	public JSONObject login(final JSONObject serializer, //
			@Parameter(value = "username", required = false) final String loginString, //
			@Parameter(value = "password", required = false) final String password, //
			@Parameter("role") final String groupName) throws JSONException {

		final AuthenticationLogic authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final LoginDTOBuilder builder = LoginDTO.newInstanceBuilder();
		final LoginDTO loginDTO = builder.withLoginString(loginString)//
				.withPassword(password)//
				.withGroupName(groupName)//
				.withUserStore(new SessionVars()).build();
		final Response response = authLogic.login(loginDTO);
		return serializeResponse(response, serializer);
	}

	private JSONObject serializeResponse(final Response response, final JSONObject serializer) {
		try {
			serializer.put("success", response.isSuccess());
			if (response.getReason() != null) {
				serializer.put("reason", response.getReason());
			}
			if (response.getGroups() != null) {
				serializer.put("groups", serializeGroupsForLogin(response.getGroups()));
			}
		} catch (final JSONException e) {
			Log.JSONRPC.error("Error serializing login response", e);
		}
		return serializer;
	}

	// Used by index.jsp
	private static JSONArray serializeGroupsForLogin(final Iterable<CMGroup> groups) throws JSONException {
		final JSONArray jsonGroups = new JSONArray();
		for (final CMGroup group : groups) {
			final JSONObject jsonGroup = new JSONObject();
			jsonGroup.put("name", group.getName());
			jsonGroup.put("description", group.getDescription());
			jsonGroups.put(jsonGroup);
		}
		return jsonGroups;
	}

}
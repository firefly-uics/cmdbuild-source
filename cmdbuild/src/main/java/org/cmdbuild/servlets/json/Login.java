package org.cmdbuild.servlets.json;

import java.util.Collection;

import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.AuthenticationFacade;
import org.cmdbuild.services.auth.Group;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Login extends JSONBase {

	@JSONExported
	@Unauthorized
	public JSONObject login(
			JSONObject serializer,
			UserContext userCtx,
			@Parameter(value="username", required=false) String username,
			@Parameter(value="password", required=false) String password,
			@Parameter("role") int groupId) throws JSONException {
//		if (userCtx == null) {
//			if (username == null || password == null) {
//				throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
//			}
//			userCtx = AuthenticationFacade.login(username, password);
//		}
//		if (groupId > 0) {
//			userCtx.setDefaultGroup(groupId);
//		}
//		try {
//			userCtx.assureNotNullDefaultGroup();
//			new SessionVars().setCurrentUserContext(userCtx);
//		} catch (AuthException e) {
//			serializer.put("success", false);
//			if (e.getExceptionType() == AuthException.AuthExceptionType.AUTH_MULTIPLE_GROUPS) {
//				serializer.put("reason", e.getExceptionTypeText());
//				serializer.put("groups", serializeGroupForLogin(userCtx.getGroups()));
//			} else {
//				throw e;
//			}
//		}
//		return serializer;
		throw new UnsupportedOperationException("Temporary disabled");
	}

	// Used by index.jsp
	public static JSONArray serializeGroupForLogin(Collection<Group> groups) throws JSONException {
		JSONArray jsonGroups = new JSONArray(); 
		for(Group group : groups) {
			JSONObject jsonGroup = new JSONObject();
			jsonGroup.put("name", group.getId());
			jsonGroup.put("description", group.getDescription());
			jsonGroups.put(jsonGroup);
		}
		return jsonGroups;
	}
}
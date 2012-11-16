package org.cmdbuild.servlets.json;

import java.util.Collection;

import org.cmdbuild.auth.AuthenticatedUser;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.AuthenticatedUserWrapper;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Login extends JSONBase {

	@JSONExported
	@Unauthorized
	public JSONObject login(JSONObject serializer, UserContext userCtx,
			@Parameter(value = "username", required = false) String username,
			@Parameter(value = "password", required = false) String password, @Parameter("role") int groupId)
			throws JSONException {

		final AuthenticationLogic authenticationFacade = applicationContext.getBean(AuthenticationLogic.class);
		AuthenticatedUser authenticatedUser = null;
		try {
			if (userCtx == null) {
				if (username == null || password == null) {
					throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
				}
				authenticatedUser = authenticationFacade.login(username, password);
				if (authenticatedUser.isAnonymous()) {
					throw AuthExceptionType.AUTH_WRONG_PASSWORD.createException();
				}
				userCtx = new AuthenticatedUserWrapper(authenticatedUser);
				/*
				 * FIXME: temporary solution.. if I don't do this, the next time
				 * the userCtx parameter will be null. The
				 * getCurrentUserContext() ---> OLD DAO calls should be deleted
				 * and changed with getUser() ---> NEW DAO (SessionVars class)
				 */
			}

			boolean userBelongsToMultipleGroups = groupId > 0;
			if (userBelongsToMultipleGroups) {
				String preferredGroupName = retrieveUserGroupNameFromGroupId(authenticatedUser, groupId);
				authenticatedUser.selectGroup(preferredGroupName);
			}

			new SessionVars().setUser(authenticatedUser);
		} catch (AuthException e) {
			serializer.put("success", false);
			if (e.getExceptionType() == AuthException.AuthExceptionType.AUTH_MULTIPLE_GROUPS) {
				serializer.put("reason", e.getExceptionTypeText());
				serializer.put("groups", serializeGroupForLogin(authenticatedUser.getGroups()));
			} else {
				throw e;
			}
		}
		return serializer;
	}

	private String retrieveUserGroupNameFromGroupId(AuthenticatedUser user, Integer groupId) {
		for (CMGroup group : user.getGroups()) {
			if (group.getId().equals(Long.valueOf(groupId))) {
				return group.getName();
			}
		}
		return null;
	}

	// Used by index.jsp
	private static JSONArray serializeGroupForLogin(Collection<CMGroup> groups) throws JSONException {
		JSONArray jsonGroups = new JSONArray();
		for (CMGroup group : groups) {
			JSONObject jsonGroup = new JSONObject();
			jsonGroup.put("name", group.getId());
			jsonGroup.put("description", group.getDescription());
			jsonGroups.put(jsonGroup);
		}
		return jsonGroups;
	}

}
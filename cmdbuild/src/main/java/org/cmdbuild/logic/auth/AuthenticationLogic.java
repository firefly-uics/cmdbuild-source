package org.cmdbuild.logic.auth;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;

/**
 * Facade class for all the authentication operations
 */
public class AuthenticationLogic {

	public static class Response {

		private boolean success = false;
		private String reason = null;
		private Iterable<CMGroup> groups = null;

		private Response(final boolean success, final String reason, final Iterable<CMGroup> groups) {
			this.success = success;
			this.reason = reason;
			this.groups = groups;
		}

		public static Response newInstance(final boolean success, final String reason, final Iterable<CMGroup> groups) {
			return new Response(success, reason, groups);
		}

		public boolean isSuccess() {
			return success;
		}

		public String getReason() {
			return reason;
		}

		public Iterable<CMGroup> getGroups() {
			return groups;
		}

	}

	public static final String USER_GROUP_DOMAIN_NAME = "UserRole";
	public static final String DEFAULT_GROUP_ATTRIBUTE = "DefaultGroup";
	private final AuthenticationService authService;

	/*
	 * Injected by Spring framework (injections defined in auth.xml)
	 */
	public AuthenticationLogic(final AuthenticationService authenticationService) {
		this.authService = authenticationService;
	}

	public Response login(final LoginDTO loginDTO) {
		final Login login = Login.newInstance(loginDTO.getLoginString());
		final AuthenticatedUser authUser = authService.authenticate(login, loginDTO.getPassword());

		final boolean userNotAuthenticated = authUser.isAnonymous();
		if (userNotAuthenticated) {
			return Response.newInstance(false, AuthExceptionType.AUTH_LOGIN_WRONG.createException().toString(), null);
		}

		final String groupName = loginDTO.getLoginGroupName();
		PrivilegeContext privilegeCtx = null;
		if (groupName == null) {
			final CMGroup guessedGroup = guessPreferredGroup(authUser);
			if (guessedGroup == null) {
				return Response.newInstance(false, AuthExceptionType.AUTH_MULTIPLE_GROUPS.createException().toString(),
						authUser.getGroups());
			} else if (authUser.getGroups().size() == 1) {
				privilegeCtx = buildPrivilegeContext(guessedGroup);
			} else { // the user has a default group
				final CMGroup[] groupsArray = (CMGroup[]) authUser.getGroups().toArray();
				privilegeCtx = buildPrivilegeContext(groupsArray);
			}
			final OperationUser operationUser = new OperationUser(authUser, privilegeCtx, guessedGroup);
			loginDTO.getUserStore().setUser(operationUser);
			return buildSuccessfulResponse();
		} else {
			final String selectedGroupName = groupName;
			final CMGroup selectedGroup = getGroup(authUser, selectedGroupName);
			privilegeCtx = buildPrivilegeContext(selectedGroup);
			final OperationUser operationUser = new OperationUser(authUser, privilegeCtx, selectedGroup);
			loginDTO.getUserStore().setUser(operationUser);
			return buildSuccessfulResponse();
		}
	}

	private CMGroup guessPreferredGroup(final CMUser user) {
		CMGroup guessedGroup = getDefaultGroup(user);
		if (guessedGroup == null) {
			guessedGroup = getTheFirstAndOnlyGroup(user);
		}
		return guessedGroup;
	}

	private CMGroup getDefaultGroup(final CMUser user) {
		return getGroup(user, user.getDefaultGroupName());
	}

	private CMGroup getGroup(final CMUser user, final String groupName) {
		for (final CMGroup g : user.getGroups()) {
			if (g.getName().equals(groupName)) {
				return g;
			}
		}
		return null;
	}

	private CMGroup getTheFirstAndOnlyGroup(final CMUser user) {
		CMGroup firstGroup = null;
		final Iterator<CMGroup> groups = user.getGroups().iterator();
		if (groups.hasNext()) {
			firstGroup = groups.next();
			if (groups.hasNext()) {
				firstGroup = null;
			}
		}
		return firstGroup;
	}

	private PrivilegeContext buildPrivilegeContext(final CMGroup... groups) {
		return TemporaryObjectsBeforeSpringDI.getPrivilegeContextFactory().buildPrivilegeContext(groups);
	}

	private Response buildSuccessfulResponse() {
		return Response.newInstance(true, null, null);
	}

	public List<CMUser> getUsersFromGroupId(final Long groupId) {
		return authService.fetchUsersByGroupId(groupId);
	}

	public Iterable<CMGroup> getGroupsFromUserId(final Long userId) {
		final CMUser user = authService.fetchUserById(userId);
		return user.getGroups();
	}

	public Iterable<CMGroup> getGroupsFromUsername(final String loginString) {
		final CMUser user = authService.fetchUserByUsername(loginString);
		return user.getGroups();
	}

	// private static Iterable<IRelation> getGroupRelationsForUser(final int
	// userId) {
	// final UserContext systemCtx = UserContext.systemContext();
	// final ICard userCard =
	// systemCtx.tables().get(UserCard.USER_CLASS_NAME).cards().get(userId);
	// final IDomain userRoleDomain =
	// systemCtx.domains().get(USER_GROUP_DOMAIN_NAME);
	// final Iterable<IRelation> userRoleRelations = systemCtx.relations().list(
	// DirectedDomain.create(userRoleDomain, DomainDirection.D), userCard);
	// return userRoleRelations;
	// }

	public static void setDefaultGroupForUser(final int userId, final int defaultGroupId) {
		// final Iterable<IRelation> groupRelations =
		// getGroupRelationsForUser(userId);
		// clearDefaultGroup(groupRelations);
		// setDefaultGroup(groupRelations, defaultGroupId);
		throw new UnsupportedOperationException("Working on it!");
	}

	// private static void clearDefaultGroup(final Iterable<IRelation>
	// groupRelations) {
	// changeDefaultGroup(groupRelations, INVALID_GROUP_ID);
	// }
	//
	// private static void setDefaultGroup(final Iterable<IRelation>
	// groupRelations, final int defaultGroupId) {
	// changeDefaultGroup(groupRelations, defaultGroupId);
	// }
	//
	// private static void changeDefaultGroup(final Iterable<IRelation>
	// groupRelations, final int defaultGroupId) {
	// for (final IRelation groupRel : groupRelations) {
	// final boolean wasDefaultGroup =
	// (groupRel.getAttributeValue(DEFAULT_GROUP_ATTRIBUTE).getBoolean() ==
	// Boolean.TRUE);
	// // Handles null values
	// final boolean willBeDefaulGroup = (groupRel.getCard2().getId() ==
	// defaultGroupId);
	// if (wasDefaultGroup != willBeDefaulGroup) {
	// groupRel.getAttributeValue(DEFAULT_GROUP_ATTRIBUTE).setValue(willBeDefaulGroup);
	// groupRel.save();
	// }
	// }
	// }

	public static boolean isLoggedIn(final HttpServletRequest request) throws RedirectException {

		// AuthenticatedUser authUser = new SessionVars().getUser();
		// if (authUser.isAnonymous() || )

		// TODO: see in the history what did headerAuth method do....

		// UserContext userCtx = new SessionVars().getCurrentUserContext();
		// if (userCtx == null) {
		// final AuthenticationService as = new DefaultAuthenticationService();
		// userCtx = as.headerAuth(request);
		// if (userCtx != null) {
		// new SessionVars().setCurrentUserContext(userCtx);
		// }
		// }
		// return (userCtx != null || doAutoLogin());
		// return new SessionVars().getUser().isValid(); // isAnonymous +
		// doAutoLogin
		return false;
	}

	public static void assureAdmin(final HttpServletRequest request, final AdminAccess adminAccess) {
		// final UserContext userCtx = new
		// SessionVars().getCurrentUserContext();
		// if (userCtx == null || !userCtx.privileges().isAdmin())
		// throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		// if (adminAccess == AdminAccess.DEMOSAFE) {
		// final String demoModeAdmin =
		// CmdbuildProperties.getInstance().getDemoModeAdmin().trim();
		// if (!demoModeAdmin.equals("") &&
		// !demoModeAdmin.equals(userCtx.getUsername()))
		// throw AuthExceptionType.AUTH_DEMO_MODE.createException();
		// }
		throw new UnsupportedOperationException("Working on it!");
	}

	private static boolean doAutoLogin() {
		// try {
		// final String username = AuthProperties.getInstance().getAutologin();
		// if (username != null && username.length() > 0) {
		// loginAs(username);
		// Log.OTHER.info("Autologin with user " + username);
		// return true;
		// }
		// } catch (final Exception e) {
		// Log.OTHER.warn("Autologin failed");
		// }
		return false;
	}

}

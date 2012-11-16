package org.cmdbuild.logic.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.auth.AuthenticatedUser;
import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;

public class AuthenticationLogic {

	public static final String USER_GROUP_DOMAIN_NAME = "UserRole";
	public static final String DEFAULT_GROUP_ATTRIBUTE = "DefaultGroup";
	private final AuthenticationService authenticationService;

	// private static final int INVALID_GROUP_ID = -1;

	/*
	 * Injected by Spring framework (injections defined in auth.xml)
	 */
	public AuthenticationLogic(final AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public AuthenticatedUser login(final String username, final String unencryptedPassword) {
		final Login login = Login.newInstance(username);
		return authenticationService.authenticate(login, unencryptedPassword);
	}

	public List<CMUser> getUsersFromGroupId(final Long groupId) {
		return authenticationService.fetchUsersByGroupId(groupId);
	}

	public Iterable<CMGroup> getGroupsFromUserId(final Long userId) {
		final CMUser user = authenticationService.fetchUserById(userId);
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

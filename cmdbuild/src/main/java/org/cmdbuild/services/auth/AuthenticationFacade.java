package org.cmdbuild.services.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;

public abstract class AuthenticationFacade {

	public static final String USER_GROUP_DOMAIN_NAME = "UserRole";
	public static final String DEFAULT_GROUP_ATTRIBUTE = "DefaultGroup";
//	private static final int INVALID_GROUP_ID = -1;

	public static UserContext login(final String username, final String unencryptedPassword) throws AuthException {
//		final AuthenticationService as = new AuthenticationService();
//		final UserContext userContext = as.jsonRpcAuth(username, unencryptedPassword);
//		if (userContext == null) {
//			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
//		}
//		return userContext;
		throw new UnsupportedOperationException("Working on it!");
	}

	public static List<UserCard> getUserList(final int groupId) {
//		final List<UserCard> list = new LinkedList<UserCard>();
//		try {
//			final UserContext systemCtx = UserContext.systemContext();
//			final IDomain userRoleDomain = systemCtx.domains().get(USER_GROUP_DOMAIN_NAME);
//			final GroupCard groupCard = GroupCard.get(groupId, systemCtx);
//			final Iterable<IRelation> userGroupRelations = systemCtx.relations().list(groupCard).domain(
//					DirectedDomain.create(userRoleDomain, DomainDirection.I), true);
//			for (final IRelation groupRel : userGroupRelations) {
//				list.add(new UserCard(groupRel.getCard1()));
//			}
//		} catch (final NotFoundException e) {
//			Log.OTHER.fatal("user_role domain does not exist", e);
//			throw AuthExceptionType.AUTH_UNKNOWN_GROUP.createException();
//		}
//		return list;
		throw new UnsupportedOperationException("Working on it!");
	}

	static public Iterable<Group> getGroupListForUser(final int userId) throws AuthException, ORMException {
//		final List<Group> list = new LinkedList<Group>();
//		try {
//			for (final IRelation groupRel : getGroupRelationsForUser(userId)) {
//				final GroupCard groupCard = new GroupCard(groupRel.getCard2());
//				final boolean isDefaultGroup = (Boolean.TRUE.equals(groupRel.getValue(DEFAULT_GROUP_ATTRIBUTE)));
//				list.add(groupCard.toGroup(isDefaultGroup));
//			}
//		} catch (final NotFoundException e) {
//			Log.PERSISTENCE.fatal("Cannot query groups for user", e);
//			throw AuthExceptionType.AUTH_UNKNOWN_GROUP.createException();
//		}
//		return list;
		throw new UnsupportedOperationException("Working on it!");
	}

//	private static Iterable<IRelation> getGroupRelationsForUser(final int userId) {
//		final UserContext systemCtx = UserContext.systemContext();
//		final ICard userCard = systemCtx.tables().get(UserCard.USER_CLASS_NAME).cards().get(userId);
//		final IDomain userRoleDomain = systemCtx.domains().get(USER_GROUP_DOMAIN_NAME);
//		final Iterable<IRelation> userRoleRelations = systemCtx.relations().list(
//				DirectedDomain.create(userRoleDomain, DomainDirection.D), userCard);
//		return userRoleRelations;
//	}

	public static void setDefaultGroupForUser(final int userId, final int defaultGroupId) {
//		final Iterable<IRelation> groupRelations = getGroupRelationsForUser(userId);
//		clearDefaultGroup(groupRelations);
//		setDefaultGroup(groupRelations, defaultGroupId);
		throw new UnsupportedOperationException("Working on it!");
	}

//	private static void clearDefaultGroup(final Iterable<IRelation> groupRelations) {
//		changeDefaultGroup(groupRelations, INVALID_GROUP_ID);
//	}
//
//	private static void setDefaultGroup(final Iterable<IRelation> groupRelations, final int defaultGroupId) {
//		changeDefaultGroup(groupRelations, defaultGroupId);
//	}
//
//	private static void changeDefaultGroup(final Iterable<IRelation> groupRelations, final int defaultGroupId) {
//		for (final IRelation groupRel : groupRelations) {
//			final boolean wasDefaultGroup = (groupRel.getAttributeValue(DEFAULT_GROUP_ATTRIBUTE).getBoolean() == Boolean.TRUE);
//			// Handles null values
//			final boolean willBeDefaulGroup = (groupRel.getCard2().getId() == defaultGroupId);
//			if (wasDefaultGroup != willBeDefaulGroup) {
//				groupRel.getAttributeValue(DEFAULT_GROUP_ATTRIBUTE).setValue(willBeDefaulGroup);
//				groupRel.save();
//			}
//		}
//	}

	static public boolean isLoggedIn(final HttpServletRequest request) throws RedirectException {
//		UserContext userCtx = new SessionVars().getCurrentUserContext();
//		if (userCtx == null) {
//			final AuthenticationService as = new AuthenticationService();
//			userCtx = as.headerAuth(request);
//			if (userCtx != null) {
//				new SessionVars().setCurrentUserContext(userCtx);
//			}
//		}
//		return (userCtx != null || doAutoLogin());
		return new SessionVars().getUser().isValid(); // isAnonymous + doAutoLogin
	}

	static public void assureAdmin(final HttpServletRequest request, final AdminAccess adminAccess) {
//		final UserContext userCtx = new SessionVars().getCurrentUserContext();
//		if (userCtx == null || !userCtx.privileges().isAdmin())
//			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
//		if (adminAccess == AdminAccess.DEMOSAFE) {
//			final String demoModeAdmin = CmdbuildProperties.getInstance().getDemoModeAdmin().trim();
//			if (!demoModeAdmin.equals("") && !demoModeAdmin.equals(userCtx.getUsername()))
//				throw AuthExceptionType.AUTH_DEMO_MODE.createException();
//		}
		throw new UnsupportedOperationException("Working on it!");
	}

	static private boolean doAutoLogin() {
//		try {
//			final String username = AuthProperties.getInstance().getAutologin();
//			if (username != null && username.length() > 0) {
//				loginAs(username);
//				Log.OTHER.info("Autologin with user " + username);
//				return true;
//			}
//		} catch (final Exception e) {
//			Log.OTHER.warn("Autologin failed");
//		}
		return false;
	}

}

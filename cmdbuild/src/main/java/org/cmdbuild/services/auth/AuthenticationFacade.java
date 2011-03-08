package org.cmdbuild.services.auth;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.elements.RelationImpl;
import org.cmdbuild.elements.filters.AbstractFilter;
import org.cmdbuild.elements.filters.AttributeFilter;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;

public abstract class AuthenticationFacade {

	public static final String USER_GROUP_DOMAIN_NAME = "UserRole";
	public static final String DEFAULT_GROUP_ATTRIBUTE = "DefaultGroup";
	private static final int INVALID_GROUP_ID = -1;

	public User addUser(final String username, final String password, final String description) throws AuthException {
		UserCard user = null;
		user = new UserCard();
		user.setUsername(username);
		user.setUnencryptedPassword(password);
		user.setDescription(description);
		user.save();
		return user;
	}

	public static UserContext login(final String username, final String unencryptedPassword) throws AuthException {
		final AuthenticationService as = new AuthenticationService();
		return as.jsonRpcAuth(username, unencryptedPassword);
	}

	static public List<UserCard> getUserList(final AbstractFilter filterCriteria) throws AuthException, ORMException {
		return getUserList(null, filterCriteria);
	}

	static public List<UserCard> getUserList(final AbstractFilter userFilterCriteria,
			final AbstractFilter roleFilterCriteria) throws AuthException, ORMException {
		List<IRelation> groupsRel = null;
		final List<UserCard> list = new LinkedList<UserCard>();
		try {
			groupsRel = RelationImpl.findAll(USER_GROUP_DOMAIN_NAME, userFilterCriteria, roleFilterCriteria);
			for (final IRelation groupRel : groupsRel) {
				list.add(new UserCard(groupRel.getCard1()));
			}
		} catch (final NotFoundException e) {
			Log.OTHER.fatal("user_role domain does not exist", e);
			throw AuthExceptionType.AUTH_UNKNOWN_GROUP.createException();
		}
		return list;
	}

	static public Iterable<Group> getGroupListForUser(final int userId) throws AuthException, ORMException {
		final List<Group> list = new LinkedList<Group>();
		try {
			final ICard user = UserContext.systemContext().tables().get(CardAttributes.User.toString()).cards().get(
					userId);
			final AbstractFilter userIdFilter = AttributeFilter.getEquals(user, "Id", String.valueOf(userId));
			for (final IRelation groupRel : RelationImpl.findAll(USER_GROUP_DOMAIN_NAME, userIdFilter, null)) {
				final GroupCard r = new GroupCard(groupRel.getCard2());
				final boolean isDefaultGroup = (Boolean.TRUE.equals(groupRel.getValue(DEFAULT_GROUP_ATTRIBUTE)));
				list.add(r.toGroup(isDefaultGroup));
			}
		} catch (final NotFoundException e) {
			Log.OTHER.fatal("user_role domain does not exist", e);
			throw AuthExceptionType.AUTH_UNKNOWN_GROUP.createException();
		}
		return list;
	}

	static public void setDefaultGroupForUser(final int userId, final int defaultGroupId) {
		final ICard user = UserContext.systemContext().tables().get(CardAttributes.User.toString()).cards().get(userId);
		final AbstractFilter userIdFilter = AttributeFilter.getEquals(user, "Id", String.valueOf(userId));
		final List<IRelation> groupRelations = RelationImpl.findAll(USER_GROUP_DOMAIN_NAME, userIdFilter, null);
		clearDefaultGroup(groupRelations);
		setDefaultGroup(groupRelations, defaultGroupId);
	}

	private static void clearDefaultGroup(List<IRelation> groupRelations) {
		changeDefaultGroup(groupRelations, INVALID_GROUP_ID);
	}

	private static void setDefaultGroup(final List<IRelation> groupRelations, final int defaultGroupId) {
		changeDefaultGroup(groupRelations, defaultGroupId);
	}

	private static void changeDefaultGroup(final List<IRelation> groupRelations, final int defaultGroupId) {
		for (final IRelation groupRel : groupRelations) {
			final boolean wasDefaultGroup = (groupRel.getAttributeValue(DEFAULT_GROUP_ATTRIBUTE).getBoolean() == Boolean.TRUE);
			// Handles null values
			final boolean willBeDefaulGroup = (groupRel.getCard2().getId() == defaultGroupId);
			if (wasDefaultGroup != willBeDefaulGroup) {
				groupRel.getAttributeValue(DEFAULT_GROUP_ATTRIBUTE).setValue(willBeDefaulGroup);
				groupRel.save();
			}
		}
	}

	static public boolean isLoggedIn(final HttpServletRequest request) throws RedirectException {
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		if (userCtx == null) {
			final AuthenticationService as = new AuthenticationService();
			userCtx = as.headerAuth(request);
			if (userCtx != null) {
				new SessionVars().setCurrentUserContext(userCtx);
			}
		}
		return (userCtx != null || doAutoLogin());
	}

	static public void assureAdmin(final HttpServletRequest request, final AdminAccess adminAccess) {
		final UserContext userCtx = new SessionVars().getCurrentUserContext();
		if (userCtx == null || !userCtx.privileges().isAdmin())
			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		if (adminAccess == AdminAccess.DEMOSAFE) {
			final String demoModeAdmin = CmdbuildProperties.getInstance().getDemoModeAdmin().trim();
			if (!demoModeAdmin.equals("") && !demoModeAdmin.equals(userCtx.getUsername()))
				throw AuthExceptionType.AUTH_DEMO_MODE.createException();
		}
	}

	static private boolean doAutoLogin() {
		try {
			final String username = AuthProperties.getInstance().getAutologin();
			if (username != null && username.length() > 0) {
				final UserContext userCtx = new AuthInfo(username).systemAuth();
				new SessionVars().setCurrentUserContext(userCtx);
				Log.OTHER.info("Autologin with user " + username);
				return true;
			}
		} catch (final Exception e) {
			Log.OTHER.warn("Autologin failed");
		}
		return false;
	}
}

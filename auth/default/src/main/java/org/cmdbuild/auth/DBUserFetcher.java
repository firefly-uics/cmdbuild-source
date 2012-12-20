package org.cmdbuild.auth;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.jcip.annotations.GuardedBy;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.UserImpl;
import org.cmdbuild.auth.user.UserImpl.UserImplBuilder;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Implements user, group and privilege management on top of the DAO layer
 */
public abstract class DBUserFetcher implements UserFetcher {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final CMDataView view;

	@GuardedBy("allGroupsCacheLock")
	private static volatile Map<Long, CMGroup> allGroupsCache = null;
	private static final Object allGroupsCacheLock = new Object();

	protected DBUserFetcher(final CMDataView view) {
		Validate.notNull(view);
		this.view = view;
	}

	@Override
	public CMUser fetchUser(final Login login) {
		final CMCard userCard = fetchUserCard(login);
		return buildUserFromCard(userCard);
	}

	@Override
	public CMUser fetchUserById(final Long userId) {
		final CMQueryRow row = view.select(anyAttribute(userClass())) //
				.from(userClass()) //
				.where(condition(attribute(userClass(), userClass().getKeyAttributeName()), eq(userId))) //
				.run() //
				.getOnlyRow();
		return buildUserFromCard(row.getCard(userClass()));
	}

	@Override
	public List<CMUser> fetchUsersFromGroupId(final Long groupId) {
		final CMQueryResult result = view.select(anyAttribute(userClass())) //
				.from(userClass()) //
				.join(roleClass(), over(userGroupDomain())) //
				.where(condition(attribute(roleClass(), roleClass().getKeyAttributeName()), eq(groupId))) //
				.run();

		final List<CMUser> usersForSpecifiedGroup = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard userCard = row.getCard(userClass());
			final CMUser user = buildUserFromCard(userCard);
			usersForSpecifiedGroup.add(user);
		}
		return usersForSpecifiedGroup;

	}

	@Override
	public List<Long> fetchUserIdsFromGroupId(final Long groupId) {
		final CMQueryResult result = view.select(anyAttribute(userClass())) //
				.from(userClass()) //
				.join(roleClass(), over(userGroupDomain())) //
				.where(condition(attribute(roleClass(), roleClass().getKeyAttributeName()), eq(groupId))) //
				.run();

		final List<Long> userIdsForSpecifiedGroup = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard userCard = row.getCard(userClass());
			userIdsForSpecifiedGroup.add(userCard.getId());
		}
		return userIdsForSpecifiedGroup;

	}

	private CMUser buildUserFromCard(final CMCard userCard) {
		// FIXME: improve performances...
		final Long userId = userCard.getId();
		final String username = userCard.get(userNameAttribute()).toString();
		final Object userDescription = userCard.get(userDescriptionAttribute());
		final String defaultGroupName = fetchDefaultGroupNameForUser(username);
		final UserImplBuilder userBuilder = UserImpl.newInstanceBuilder() //
				.withId(userId) //
				.withName(username) //
				.withDescription(userDescription != null ? userDescription.toString() : "") //
				.withDefaultGroupName(defaultGroupName); //

		final List<String> userGroups = fetchGroupNamesForUser(userId);
		for (final String groupName : userGroups) {
			userBuilder.withGroupName(groupName);
		}
		userBuilder.setActive(true);
		return userBuilder.build();
	}

	private String fetchDefaultGroupNameForUser(final String username) {
		final CMQueryResult result = view
				.select(attribute(userClass(), "Username"), attribute(userGroupDomain(), "DefaultGroup"),
						attribute(roleClass(), roleClass().getCodeAttributeName())) //
				.from(userClass()) //
				.join(roleClass(), over(userGroupDomain())) //
				.where(condition(attribute(userClass(), "Username"), eq(username))) //
				.run();

		String defaultGroupName = null;
		for (final CMQueryRow row : result) {
			final CMCard group = row.getCard(roleClass());
			final CMRelation relation = row.getRelation(userGroupDomain()).getRelation();
			final String groupName = (String) group.getCode();
			final Object isDefaultGroup = relation.get("DefaultGroup");
			if (isDefaultGroup != null) {
				if ((Boolean) isDefaultGroup) {
					defaultGroupName = groupName;
				}
			}
		}
		return defaultGroupName;
	}

	protected final CMCard fetchUserCard(final Login login) throws NoSuchElementException {
		final Alias userClassAlias = Alias.canonicalAlias(userClass());
		final CMQueryRow userRow = view
				.select(
				// FIXME: anyAttribute()
				attribute(userClassAlias, userNameAttribute()), attribute(userClassAlias, userDescriptionAttribute()),
						attribute(userClassAlias, userPasswordAttribute())) //
				.from(userClass(), as(userClassAlias)) //
				.where(condition(attribute(userClassAlias, loginAttributeName(login)), eq(login.getValue()))) //
				.run().getOnlyRow();
		final CMCard userCard = userRow.getCard(userClassAlias);
		return userCard;
	}

	// private List<String> getAllGroupNames() {
	// TODO why cache?
	// if (allGroupsCache == null) {
	// synchronized (allGroupsCacheLock) {
	// if (allGroupsCache == null) {
	// allGroupsCache = fetchAllGroups();
	// }
	// }
	// }
	// return allGroupsCache;
	// return groupFetcher.fetchAllGroupIdToGroup();
	// }

	private List<String> fetchGroupNamesForUser(final Long userId) {
		final List<String> groupNames = new ArrayList<String>();
		final Alias groupClassAlias = Alias.canonicalAlias(roleClass());
		final Alias userClassAlias = Alias.canonicalAlias(userClass());
		final CMQueryResult userGroupsRows = view.select(attribute(groupClassAlias, "Code")).from(roleClass())
				.join(userClass(), as(userClassAlias), over(userGroupDomain()))
				.where(condition(attribute(userClass(), userIdAttribute()), eq(userId))) //
				.run();
		for (final CMQueryRow row : userGroupsRows) {
			final CMCard groupCard = row.getCard(groupClassAlias);
			groupNames.add((String) groupCard.getCode());
		}
		return groupNames;
	}

	@Override
	public List<CMUser> fetchAllUsers() {
		final CMQueryResult result = view.select(anyAttribute(userClass())) //
				.from(userClass()) //
				.run();
		final List<CMUser> allUsers = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard userCard = row.getCard(userClass());
			final CMUser user = buildUserFromCard(userCard);
			allUsers.add(user);
		}
		return allUsers;
	}

	/*
	 * Methods to shade class and attribute names. They should be detected by
	 * metadatas, but for now we stick to what the DBA has decided.
	 */

	protected abstract CMClass userClass();

	protected abstract CMClass roleClass();

	protected abstract String userEmailAttribute();

	protected abstract String userNameAttribute();

	protected abstract String userDescriptionAttribute();

	protected abstract String userPasswordAttribute();

	protected abstract String userIdAttribute();

	protected abstract String activeAttribute();

	protected abstract CMDomain userGroupDomain();

	protected final String loginAttributeName(final Login login) {
		switch (login.getType()) {
		case USERNAME:
			return userNameAttribute();
		case EMAIL:
			return userEmailAttribute();
		default:
			throw new IllegalArgumentException("Unsupported login type");
		}
	}

}

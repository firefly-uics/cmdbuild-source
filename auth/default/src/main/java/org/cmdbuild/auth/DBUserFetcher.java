package org.cmdbuild.auth;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;

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
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause.Operator;
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
	private final GroupFetcher groupFetcher;

	@GuardedBy("allGroupsCacheLock")
	private static volatile Map<Long, CMGroup> allGroupsCache = null;
	private static final Object allGroupsCacheLock = new Object();

	protected DBUserFetcher(final CMDataView view) {
		Validate.notNull(view);
		this.view = view;
		groupFetcher = new DBGroupFetcher(view);
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
				.where(attribute(userClass(), userClass().getKeyAttributeName()), Operator.EQUALS, userId) //
				.run() //
				.getOnlyRow();
		return buildUserFromCard(row.getCard(userClass()));
	}

	@Override
	public List<CMUser> fetchUsersFromGroupId(final Long groupId) {
		final CMQueryResult result = view.select(anyAttribute(userClass())) //
				.from(userClass()) //
				.join(roleClass(), over(userGroupDomain())) //
				.where(attribute(roleClass(), roleClass().getKeyAttributeName()), Operator.EQUALS, groupId) //
				.run();

		final List<CMUser> usersForSpecifiedGroup = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard userCard = row.getCard(userClass());
			final CMUser user = buildUserFromCard(userCard);
			usersForSpecifiedGroup.add(user);
		}
		return usersForSpecifiedGroup;

	}

	private CMUser buildUserFromCard(final CMCard userCard) {
		final Long userId = userCard.getId();
		final String username = userCard.get(userNameAttribute()).toString();
		final Object userDescription = userCard.get(userDescriptionAttribute());
//		final Object email = userCard.get("Email");
		final String defaultGroupName = fetchDefaultGroupNameForUser(username);
		final UserImplBuilder userBuilder = UserImpl.newInstanceBuilder() //
				.withId(userId) //
				.withName(username) //
				.withDescription(userDescription != null ? userDescription.toString() : "") //
				.withDefaultGroupName(defaultGroupName); //
//				.withEmail(email != null ? email.toString() : "");

		final Map<Long, CMGroup> allGroups = getAllGroups();
		for (final Object groupId : fetchGroupIdsForUser(userCard.getId())) {
			final CMGroup group = allGroups.get(groupId);
			userBuilder.withGroup(group);
		}
		userBuilder.setActive(true); //FIXME: when get method of DBEntry is fixed
		return userBuilder.build();
	}

	private String fetchDefaultGroupNameForUser(final String username) {
		final CMQueryResult result = view
				.select(attribute(userClass(), "Username"), attribute(userGroupDomain(), "DefaultGroup"),
						attribute(roleClass(), roleClass().getCodeAttributeName())) //
				.from(userClass()) //
				.join(roleClass(), over(userGroupDomain())) //
				.where(attribute(userClass(), "Username"), Operator.EQUALS, username) //
				.run();

		String defaultGroupName = null;
		for (final CMQueryRow row : result) {
			final CMCard group = row.getCard(roleClass());
			final CMRelation relation = row.getRelation(userGroupDomain()).getRelation();
			final String groupName = (String) group.getCode();
			final Object isDefaultGroup = relation.get("DefaultGroup");
			if (isDefaultGroup != null)
				if ((Boolean) isDefaultGroup) {
					defaultGroupName = groupName;
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
						attribute(userClassAlias, userPasswordAttribute()))
				//
				.from(userClass(), as(userClassAlias))
				//
				.where(attribute(userClassAlias, loginAttributeName(login)), Operator.EQUALS, login.getValue()).run()
				.getOnlyRow();
		final CMCard userCard = userRow.getCard(userClassAlias);
		return userCard;
	}

	private Map<Long, CMGroup> getAllGroups() {
		// TODO why cache?
		// if (allGroupsCache == null) {
		// synchronized (allGroupsCacheLock) {
		// if (allGroupsCache == null) {
		// allGroupsCache = fetchAllGroups();
		// }
		// }
		// }
		// return allGroupsCache;
		return groupFetcher.fetchAllGroupIdToGroup();
	}

	private List<Object> fetchGroupIdsForUser(final Object userId) {
		final List<Object> groupIds = new ArrayList<Object>();
		final Alias groupClassAlias = Alias.canonicalAlias(groupClass());
		final Alias userClassAlias = Alias.canonicalAlias(userClass());
		final CMQueryResult userGroupsRows = view.select(anyAttribute(groupClassAlias)).from(groupClass())
				.join(userClass(), as(userClassAlias), over(userGroupDomain()))
				.where(attribute(userClass(), userIdAttribute()), Operator.EQUALS, userId).run();
		for (final CMQueryRow row : userGroupsRows) {
			final CMCard groupCard = row.getCard(groupClassAlias);
			groupIds.add(groupCard.getId());
		}
		return groupIds;
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

	private CMClass groupClass() {
		return view.findClassByName("Role");
	}

}

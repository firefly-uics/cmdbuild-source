package org.cmdbuild.auth;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.jcip.annotations.GuardedBy;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.GroupImpl;
import org.cmdbuild.auth.acl.GroupImpl.GroupImplBuilder;
import org.cmdbuild.auth.acl.PrivilegeSet.PrivilegePair;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.UserImpl;
import org.cmdbuild.auth.user.UserImpl.UserImplBuilder;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause.Operator;
import org.cmdbuild.dao.reference.EntryTypeReference;
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
	private static final String DB_READ_PRIVILEGE = "r";
	private static final String DB_WRITE_PRIVILEGE = "w";

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
		final String userName = userCard.get(userNameAttribute()).toString();
		final Object userDescription = userCard.get(userDescriptionAttribute());
		final UserImplBuilder userBuilder = UserImpl.newInstanceBuilder() //
				.withId(userId) //
				.withName(userName) //
				.withDescription(userDescription != null ? userDescription.toString() : "");

		final Map<Long, CMGroup> allGroups = getAllGroups();
		for (final Object groupId : fetchGroupIdsForUser(userCard.getId())) {
			final CMGroup group = allGroups.get(groupId);
			userBuilder.withGroup(group);
		}

		return userBuilder.build();
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
		return fetchAllGroups();
	}

	private Map<Long, CMGroup> fetchAllGroups() {
		final Map<Long, CMGroup> groupCards = new HashMap<Long, CMGroup>();
		final Map<Object, List<PrivilegePair>> allPrivileges = fetchAllPrivileges();
		final Alias groupClassAlias = Alias.canonicalAlias(groupClass());
		final CMQueryResult groupRows = view
				.select(
				// FIXME: anyAttribute()
				attribute(groupClassAlias, groupNameAttribute()),
						attribute(groupClassAlias, groupDescriptionAttribute()),
						attribute(groupClassAlias, groupIsGodAttribute()),
						attribute(groupClassAlias, groupDisabledModulesAttribute()),
						attribute(groupClassAlias, groupStartingClassAttribute()))
				.from(groupClass(), as(groupClassAlias)).run();
		for (final CMQueryRow row : groupRows) {
			final CMCard groupCard = row.getCard(groupClassAlias);
			final Long groupId = groupCard.getId();
			final Object groupDescription = groupCard.get(groupDescriptionAttribute());
			final GroupImplBuilder groupBuilder = GroupImpl.newInstanceBuilder().withId(groupId)
					.withName(groupCard.get(groupNameAttribute()).toString())
					.withDescription(groupDescription != null ? groupDescription.toString() : null);

			final boolean groupIsGod = Boolean.TRUE.equals(groupCard.get(groupIsGodAttribute()));
			if (groupIsGod) {
				groupBuilder.withPrivilege(new PrivilegePair(DefaultPrivileges.GOD));
			} else if (allPrivileges.containsKey(groupId)) {
				groupBuilder.withPrivileges(allPrivileges.get(groupId));
				for (final String moduleName : getDisabledModules(groupCard)) {
					groupBuilder.withoutModule(moduleName);
				}
			}

			final EntryTypeReference classReference = (EntryTypeReference) groupCard.get(groupStartingClassAttribute());
			groupBuilder.withStartingClassId(classReference.getId());

			groupCards.put(groupId, groupBuilder.build());
		}
		return groupCards;
	}

	private String[] getDisabledModules(final CMCard groupCard) {
		final String groupDisabledModules = (String) groupCard.get(groupDisabledModulesAttribute());
		if (groupDisabledModules != null) {
			final int start = groupDisabledModules.lastIndexOf("{"), end = groupDisabledModules.indexOf("{");
			if (start == 0 && end == (groupDisabledModules.length() - 1)) {
				return groupDisabledModules.substring(start, end).split(",");
			}
		}
		return new String[0];
	}

	/*
	 * TODO Add report privileges
	 */
	private Map<Object, List<PrivilegePair>> fetchAllPrivileges() {
		final Map<Object, List<PrivilegePair>> allPrivileges = new HashMap<Object, List<PrivilegePair>>();
		final Alias privilegeClassAlias = Alias.canonicalAlias(privilegeClass());
		final CMQueryResult privilegeRows = view.select(anyAttribute(privilegeClassAlias))
				.from(privilegeClass(), as(privilegeClassAlias)).run();
		for (final CMQueryRow row : privilegeRows) {
			final CMCard privilegeCard = row.getCard(privilegeClassAlias);
			final CMPrivilegedObject privObject = extractPrivilegeId(privilegeCard);
			final CMPrivilege privilege = extractPrivilegeType(privilegeCard);
			final Object groupId = privilegeCard.get(privilegeGroupIdAttribute());
			if (privObject == null || privilege == null || groupId == null) {
				logger.warn(
						"Skipping privilege pair (%s,%s) for group %s",
						new Object[] { privilegeCard.get(privilegeClassIdAttribute()),
								privilegeCard.get(privilegeTypeAttribute()), groupId });
			} else {
				List<PrivilegePair> privList = allPrivileges.get(groupId);
				if (privList == null) {
					privList = new ArrayList<PrivilegePair>();
					allPrivileges.put(groupId, privList);
				}
				privList.add(new PrivilegePair(privObject, privilege));
			}
		}
		return allPrivileges;
	}

	private CMPrivilegedObject extractPrivilegeId(final CMCard privilegeCard) {
		final EntryTypeReference etr = (EntryTypeReference) privilegeCard.get(privilegeClassIdAttribute());
		return view.findClassById(etr.getId());
	}

	private CMPrivilege extractPrivilegeType(final CMCard privilegeCard) {
		final Object type = privilegeCard.get(privilegeTypeAttribute());
		if (DB_READ_PRIVILEGE.equals(type)) {
			return DefaultPrivileges.READ;
		} else if (DB_WRITE_PRIVILEGE.equals(type)) {
			return DefaultPrivileges.WRITE;
		}
		return null;
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

	private String groupNameAttribute() {
		return "Code";
	}

	private String groupDescriptionAttribute() {
		return "Description";
	}

	private String groupIsGodAttribute() {
		return "Administrator";
	}

	private String groupDisabledModulesAttribute() {
		return "DisabledModules";
	}

	private String groupStartingClassAttribute() {
		return "startingClass";
	}

	private CMClass privilegeClass() {
		return view.findClassByName("Grant");
	}

	private String privilegeGroupIdAttribute() {
		return "IdRole";
	}

	private String privilegeClassIdAttribute() {
		return "IdGrantedClass";
	}

	private String privilegeTypeAttribute() {
		return "Mode";
	}
}

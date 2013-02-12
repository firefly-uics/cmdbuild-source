package org.cmdbuild.auth;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Utils.as;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.GroupImpl;
import org.cmdbuild.auth.acl.GroupImpl.GroupImplBuilder;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.dao.CardStatus;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.reference.EntryTypeReference;
import org.cmdbuild.dao.view.CMDataView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DBGroupFetcher implements GroupFetcher {

	private final CMDataView view;
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String DB_READ_PRIVILEGE = "r";
	private static final String DB_WRITE_PRIVILEGE = "w";

	public DBGroupFetcher(final CMDataView view) {
		Validate.notNull(view);
		this.view = view;
	}

	@Override
	public Map<Long, CMGroup> fetchAllGroupIdToGroup() {
		final CMClass roleClass = view.findClass("Role");
		final Map<Long, CMGroup> groupCards = new HashMap<Long, CMGroup>();
		final Alias groupClassAlias = EntryTypeAlias.canonicalAlias(roleClass);
		final CMQueryResult groupRows = view.select(anyAttribute(roleClass)) //
				.from(roleClass, as(groupClassAlias)).run();
		for (final CMQueryRow row : groupRows) {
			final CMCard groupCard = row.getCard(groupClassAlias);
			final CMGroup group = buildCMGroupFromGroupCard(groupCard);
			groupCards.put(groupCard.getId(), group);
		}
		return groupCards;
	}

	private String[] getDisabledModules(final CMCard groupCard) {
		final Object disabledModules = groupCard.get(groupDisabledModulesAttribute());
		if (disabledModules != null) {
			return (String[])  disabledModules;
		}

		return new String[0];
	}

	/*
	 * TODO Add report privileges
	 */
	private List<PrivilegePair> fetchAllPrivilegesForGroup(final Long groupId) {
		final CMClass privilegeClass = view.findClass("Grant");
		final List<PrivilegePair> allPrivileges = Lists.newArrayList();
		final Alias privilegeClassAlias = EntryTypeAlias.canonicalAlias(privilegeClass);
		final CMQueryResult groupPrivileges = view.select(anyAttribute(privilegeClassAlias)) //
				.from(privilegeClass, as(privilegeClassAlias)) //
				.where(condition(attribute(privilegeClassAlias, "IdRole"), eq(groupId))) //
				.run();
		for (final CMQueryRow row : groupPrivileges) {
			final CMCard privilegeCard = row.getCard(privilegeClassAlias);
			final CMPrivilegedObject privObject = extractPrivilegedObject(privilegeCard);
			final CMPrivilege privilege = extractPrivilegeType(privilegeCard);
			if (privObject == null || privilege == null) {
				logger.warn(
						"Skipping privilege pair (%s,%s) for group %s",
						new Object[] { privilegeCard.get(privilegeClassIdAttribute()),
								privilegeCard.get(privilegeTypeAttribute()), groupId });
			} else {
				allPrivileges.add(new PrivilegePair(privObject, privilege));
			}
		}
		return allPrivileges;
	}

	private CMPrivilegedObject extractPrivilegedObject(final CMCard privilegeCard) {
		final EntryTypeReference etr = (EntryTypeReference) privilegeCard.get(privilegeClassIdAttribute());
		return view.findClass(etr.getId());
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

	@Override
	public Iterable<CMGroup> fetchAllGroups() {
		final Map<Long, CMGroup> groupIdToGroup = fetchAllGroupIdToGroup();
		return groupIdToGroup.values();
	}

	@Override
	public CMGroup fetchGroupWithId(final Long groupId) {
		try {
			final CMCard groupCard = fetchGroupCardFromId(groupId);
			return buildCMGroupFromGroupCard(groupCard);
		} catch (final NoSuchElementException ex) {
			return new NullGroup(groupId);
		}
	}

	@Override
	public CMGroup fetchGroupWithName(final String groupName) {
		try {
			final CMCard groupCard = fetchGroupCardFromName(groupName);
			return buildCMGroupFromGroupCard(groupCard);
		} catch (final NoSuchElementException ex) {
			return new NullGroup();
		}
	}

	@Override
	public CMGroup changeGroupStatusTo(final Long groupId, final boolean isActive) {
		final CMCard groupCard = fetchGroupCardFromId(groupId);
		final CMCardDefinition modifiableCard = view.update(groupCard);
		if (isActive) {
			modifiableCard.set("Active", CardStatus.ACTIVE.value());
		} else {
			modifiableCard.set("Active", CardStatus.INACTIVE.value());
		}
		final CMCard modifiedGroupCard = modifiableCard.save();
		return buildCMGroupFromGroupCard(modifiedGroupCard);
	}

	private CMCard fetchGroupCardFromId(final Long groupId) {
		final CMClass roleClass = view.findClass("Role");
		final Alias groupClassAlias = EntryTypeAlias.canonicalAlias(roleClass);
		final CMQueryRow row = view.select(anyAttribute(roleClass)) //
				.from(roleClass, as(groupClassAlias)) //
				.where(condition(attribute(roleClass, "Id"), eq(groupId))) //
				.run().getOnlyRow();
		final CMCard groupCard = row.getCard(groupClassAlias);
		return groupCard;
	}

	private CMCard fetchGroupCardFromName(final String groupName) {
		final CMClass roleClass = view.findClass("Role");
		final Alias groupClassAlias = EntryTypeAlias.canonicalAlias(roleClass);
		final CMQueryRow row = view.select(anyAttribute(roleClass)) //
				.from(roleClass, as(groupClassAlias)) //
				.where(condition(attribute(roleClass, "Code"), eq(groupName))) //
				.run().getOnlyRow();
		final CMCard groupCard = row.getCard(groupClassAlias);
		return groupCard;
	}

	private CMGroup buildCMGroupFromGroupCard(final CMCard groupCard) {
		final Long groupId = groupCard.getId();
		final List<PrivilegePair> allPrivileges = fetchAllPrivilegesForGroup(groupId);
		final Object groupDescription = groupCard.get(groupDescriptionAttribute());
		final GroupImplBuilder groupBuilder = GroupImpl.newInstance().withId(groupId)
				.withName(groupCard.get(groupNameAttribute()).toString())
				.withDescription(groupDescription != null ? groupDescription.toString() : null);
		final boolean groupIsGod = Boolean.TRUE.equals(groupCard.get(groupIsGodAttribute()));
		if (groupIsGod) {
			groupBuilder.withPrivilege(new PrivilegePair(DefaultPrivileges.GOD));
		} else {
			groupBuilder.withPrivileges(allPrivileges);
			for (final String moduleName : getDisabledModules(groupCard)) {
				groupBuilder.withoutModule(moduleName);
			}
		}
		final EntryTypeReference classReference = (EntryTypeReference) groupCard.get(groupStartingClassAttribute());
		if (classReference != null) {
			groupBuilder.withStartingClassId(classReference.getId());
		}
		final Object emailAddress = groupCard.get(groupEmailAttribute());
		groupBuilder.withEmail(emailAddress != null ? emailAddress.toString() : null);
		groupBuilder.active(true);
		groupBuilder.administrator(groupIsGod);
		return groupBuilder.build();
	}

	private String groupNameAttribute() {
		return "Code";
	}

	private String groupEmailAttribute() {
		return "Email";
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

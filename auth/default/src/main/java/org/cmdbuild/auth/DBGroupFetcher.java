package org.cmdbuild.auth;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;

import java.util.ArrayList;
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
import static org.cmdbuild.dao.query.clause.AnyAttribute.*;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause.Operator;
import org.cmdbuild.dao.reference.EntryTypeReference;
import org.cmdbuild.dao.view.CMDataView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBGroupFetcher implements GroupFetcher {

	private final CMDataView view;
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String DB_READ_PRIVILEGE = "r";
	private static final String DB_WRITE_PRIVILEGE = "w";

	public DBGroupFetcher(final CMDataView view) {
		Validate.notNull(view);
		this.view = view;
	}

	public Map<Long, CMGroup> fetchAllGroupIdToGroup() {
		final Map<Long, CMGroup> groupCards = new HashMap<Long, CMGroup>();
		final Alias groupClassAlias = Alias.canonicalAlias(groupClass());
		final CMQueryResult groupRows = view.select(anyAttribute(groupClass())) //
				.from(groupClass(), as(groupClassAlias)).run();
		for (final CMQueryRow row : groupRows) {
			final CMCard groupCard = row.getCard(groupClassAlias);
			CMGroup group = buildCMGroupFromGroupCard(groupCard);
			groupCards.put(groupCard.getId(), group);
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

	@Override
	public Iterable<CMGroup> fetchAllGroups() {
		Map<Long, CMGroup> groupIdToGroup = fetchAllGroupIdToGroup();
		return groupIdToGroup.values();
	}

	@Override
	public CMGroup fetchGroupWithId(Long groupId) {
		try {
			CMCard groupCard = fetchGroupCardFromId(groupId);
			return buildCMGroupFromGroupCard(groupCard);
		} catch (NoSuchElementException ex) {
			return new NullGroup(groupId);
		}
	}

	// FIXME: modify this method when the update card feature will be available
	@Override
	public CMGroup changeGroupStatusTo(Long groupId, boolean isActive) {
		CMCard groupCard = fetchGroupCardFromId(groupId);
		CMCardDefinition modifiableCard = view.modifyCard(groupCard);
		if (isActive) {
			modifiableCard.set("Active", CardStatus.ACTIVE.value());
		} else {
			modifiableCard.set("Active", CardStatus.INACTIVE.value());
		}
		CMCard modifiedGroupCard = modifiableCard.save();
		return buildCMGroupFromGroupCard(modifiedGroupCard);
	}

	private CMCard fetchGroupCardFromId(Long groupId) {
		final Alias groupClassAlias = Alias.canonicalAlias(groupClass());
		CMQueryRow row = view.select(anyAttribute(groupClass())) //
				.from(groupClass(), as(groupClassAlias)) //
				.where(attribute(groupClass(), "Id"), Operator.EQUALS, groupId) //
				.run().getOnlyRow();
		CMCard groupCard = row.getCard(groupClassAlias);
		return groupCard;
	}

	private CMGroup buildCMGroupFromGroupCard(CMCard groupCard) {
		final Map<Object, List<PrivilegePair>> allPrivileges = fetchAllPrivileges();
		final Long groupId = groupCard.getId();
		final Object groupDescription = groupCard.get(groupDescriptionAttribute());
		final GroupImplBuilder groupBuilder = GroupImpl.newInstance().withId(groupId)
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
		if (classReference != null) {
			groupBuilder.withStartingClassId(classReference.getId());
		}
		Object emailAddress = groupCard.get(groupEmailAttribute());
		groupBuilder.withEmail(emailAddress != null ? emailAddress.toString() : null);
		groupBuilder.active(true);
		groupBuilder.administrator(groupIsGod);
		return groupBuilder.build();
	}

	private CMClass privilegeClass() {
		return view.findClassByName("Grant");
	}

	private CMClass groupClass() {
		return view.findClassByName("Role");
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

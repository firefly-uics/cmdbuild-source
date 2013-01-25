package org.cmdbuild.logic.privileges;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;

import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.AnyAttribute;
import org.cmdbuild.dao.reference.EntryTypeReference;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.Logic;

import com.google.common.collect.Lists;

public class SecurityLogic implements Logic {

	public static class PrivilegeInfo {

		private final Long groupId;
		public final String mode;
		public final CMPrivilegedObject privilegedObject;

		public PrivilegeInfo(final Long groupId, final CMPrivilegedObject privilegedObject, final String mode) {
			this.groupId = groupId;
			this.mode = mode;
			this.privilegedObject = privilegedObject;
		}

		public String getMode() {
			return mode;
		}

		public Long getPrivilegeObjectId() {
			if (privilegedObject instanceof CMClass) {
				return ((CMClass) privilegedObject).getId();
			}
			// TODO: manage domain, report, function
			return null;
		}

		public String getPrivilegedObjectName() {
			if (privilegedObject instanceof CMClass) {
				return ((CMClass) privilegedObject).getName();
			}
			// TODO: manage domain, report, function
			return null;
		}

		public Long getGroupId() {
			return groupId;
		}

		public String getPrivilegeId() {
			return privilegedObject.getPrivilegeId();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
			result = prime * result + ((mode == null) ? 0 : mode.hashCode());
			result = prime * result + ((privilegedObject == null) ? 0 : privilegedObject.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final PrivilegeInfo other = (PrivilegeInfo) obj;
			if (this.mode.equals(other.mode) //
					&& this.groupId.equals(other.getGroupId()) //
					&& this.getPrivilegeObjectId().equals(other.getPrivilegeObjectId())) {
				return true;
			}
			return false;
		}

	}

	private final CMDataView view;
	private final CMClass grantClass;

	public SecurityLogic(final CMDataView view) {
		this.view = view;
		this.grantClass = view.findClassByName("Grant");
	}

	public List<PrivilegeInfo> getPrivilegesForGroup(final Long groupId) {
		final List<PrivilegeInfo> fetchedPrivileges = fetchStoredPrivilegesForGroup(groupId);
		final Iterable<CMClass> nonReservedActiveClasses = filterNonReservedAndNonBaseClasses();
		for (final CMClass clazz : nonReservedActiveClasses) {
			final Long classId = clazz.getId();
			if (!isPrivilegeStoredForClass(classId, fetchedPrivileges)) {
				final PrivilegeInfo pi = new PrivilegeInfo(groupId, clazz, "-");
				fetchedPrivileges.add(pi);
			}
		}
		return fetchedPrivileges;
	}

	/**
	 * Fetches the privileges for specified group. NOTE that the group has no
	 * privilege if it is retrieved and fetched as 'none' or if it is not stored
	 * in the database
	 */
	private List<PrivilegeInfo> fetchStoredPrivilegesForGroup(final Long groupId) {
		logger.debug("Retrieving privileges for group with id {}", groupId);
		final List<PrivilegeInfo> fetchedPrivileges = Lists.newArrayList();
		final CMQueryResult result = view.select(attribute(grantClass, "IdRole"), //
				attribute(grantClass, "IdGrantedClass"), //
				attribute(grantClass, "Mode")) //
				.from(grantClass) //
				.where(condition(attribute(grantClass, "IdRole"), eq(groupId))) //
				.run();
		for (final CMQueryRow row : result) {
			final CMCard grantCard = row.getCard(grantClass);
			final EntryTypeReference entryTypeReference = (EntryTypeReference) grantCard.get("IdGrantedClass");
			final String mode = (String) grantCard.get("Mode");
			final CMClass clazz = view.findClassById(entryTypeReference.getId());
			final PrivilegeInfo pi = new PrivilegeInfo(groupId, clazz, mode);
			fetchedPrivileges.add(pi);
		}
		return fetchedPrivileges;
	}

	@SuppressWarnings("unchecked")
	private Iterable<CMClass> filterNonReservedAndNonBaseClasses() {
		final Iterable<CMClass> classes = (Iterable<CMClass>) view.findClasses();
		final List<CMClass> nonReservedClasses = Lists.newArrayList();
		for (final CMClass clazz : classes) {
			if (!clazz.isSystem() && !clazz.isBaseClass()) {
				nonReservedClasses.add(clazz);
			}
		}
		return nonReservedClasses;
	}

	private boolean isPrivilegeStoredForClass(final Long classId, final List<PrivilegeInfo> fetchedPrivileges) {
		for (final PrivilegeInfo privilegeInfo : fetchedPrivileges) {
			if (privilegeInfo.getPrivilegeObjectId() != null && privilegeInfo.getPrivilegeObjectId().equals(classId)) {
				return true;
			}
		}
		return false;
	}

	public void savePrivilege(final PrivilegeInfo privilegeInfo) {
		final CMQueryResult result = view.select(AnyAttribute.anyAttribute(grantClass)) //
				.from(grantClass) //
				.where(condition(attribute(grantClass, "IdRole"), eq(privilegeInfo.getGroupId()))) //
				.run();

		for (final CMQueryRow row : result) {
			final CMCard grantCard = row.getCard(grantClass);
			final EntryTypeReference etr = (EntryTypeReference) grantCard.get("IdGrantedClass");
			if (etr.getId().equals(privilegeInfo.getPrivilegeObjectId())) {
				updateModeForGrantCard(grantCard, privilegeInfo.getMode());
				return;
			}
		}
		createGrantCard(privilegeInfo);
	}

	private void updateModeForGrantCard(final CMCard grantCard, final String mode) {
		final CMCardDefinition modifiableGrant = view.update(grantCard);
		modifiableGrant.set("Mode", mode).save();
	}

	private void createGrantCard(final PrivilegeInfo privilegeInfo) {
		final CMCardDefinition grantCardToBeCreated = view.createCardFor(grantClass);
		grantCardToBeCreated.set("IdRole", privilegeInfo.getGroupId()) //
				.set("IdGrantedClass", privilegeInfo.getPrivilegeObjectId()) //
				.set("Mode", privilegeInfo.getMode()) //
				.save();
	}

}

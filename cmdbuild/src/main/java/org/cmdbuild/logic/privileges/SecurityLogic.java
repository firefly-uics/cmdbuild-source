package org.cmdbuild.logic.privileges;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.util.List;

import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause.Operator;
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
	}

	private final CMDataView view;

	public SecurityLogic(final CMDataView view) {
		this.view = view;
	}

	public List<PrivilegeInfo> getPrivilegesForGroup(final Long groupId) {
		final CMClass grantClass = view.findClassByName("Grant");
		logger.debug("Retrieving privileges for group with id {}", groupId);
		final List<PrivilegeInfo> privileges = Lists.newArrayList();
		final CMQueryResult result = view.select(attribute(grantClass, "IdRole"), //
				attribute(grantClass, "IdGrantedClass"), //
				attribute(grantClass, "Mode")) //
				.from(grantClass) //
				.where(attribute(grantClass, "IdRole"), Operator.EQUALS, groupId).run();
		for (final CMQueryRow row : result) {
			final CMCard grantCard = row.getCard(grantClass);
			final EntryTypeReference entryTypeReference = (EntryTypeReference) grantCard.get("IdGrantedClass");
			final String mode = (String) grantCard.get("Mode");
			final CMClass clazz = view.findClassById(entryTypeReference.getId());
			final PrivilegeInfo pi = new PrivilegeInfo(groupId, clazz, mode);
			privileges.add(pi);
		}
		return privileges;
	}

}

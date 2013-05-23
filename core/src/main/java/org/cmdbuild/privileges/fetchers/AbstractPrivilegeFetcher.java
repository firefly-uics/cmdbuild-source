package org.cmdbuild.privileges.fetchers;

import static org.cmdbuild.auth.privileges.constants.GrantConstants.DISABLED_ATTRIBUTES_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.GRANT_CLASS_NAME;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.GROUP_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.MODE_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_CLASS_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGE_FILTER_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.TYPE_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.auth.privileges.constants.PrivilegedObjectType;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.DBDataView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public abstract class AbstractPrivilegeFetcher implements PrivilegeFetcher {

	private final DBDataView view;
	private final Long groupId;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected AbstractPrivilegeFetcher(final DBDataView view, final Long groupId) {
		this.view = view;
		this.groupId = groupId;
	}

	/**
	 * Template method that uses methods that will be defined in subclasses
	 */
	@Override
	public Iterable<PrivilegePair> fetch() {
		final CMClass privilegeClass = view.findClass(GRANT_CLASS_NAME);
		final CMQueryResult result = view
				.select(anyAttribute(privilegeClass))
				.from(privilegeClass)
				.where(and(condition(attribute(privilegeClass, GROUP_ID_ATTRIBUTE), eq(groupId)),
						condition(attribute(privilegeClass, TYPE_ATTRIBUTE), eq(getPrivilegedObjectType().getValue()))))
				.run();

		final List<PrivilegePair> privilegesForDefinedType = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard privilegeCard = row.getCard(privilegeClass);
			final SerializablePrivilege privObject = extractPrivilegedObject(privilegeCard);
			final CMPrivilege privilege = extractPrivilegeMode(privilegeCard);
			if (privObject == null) {
				logger.warn(
						"Skipping privilege pair (%s,%s) of type (%s) for group %s",
						new Object[] { privilegeCard.get(PRIVILEGED_CLASS_ID_ATTRIBUTE),
								privilegeCard.get(MODE_ATTRIBUTE), getPrivilegedObjectType().getValue(), groupId });
			} else {
				final PrivilegePair privilegePair = new PrivilegePair(privObject, getPrivilegedObjectType().getValue(),
						privilege);
				privilegePair.privilegeFilter = extractPrivilegeFilter(privilegeCard);
				privilegePair.disabledAttributes = extractDisabledAttributes(privilegeCard);
				privilegesForDefinedType.add(privilegePair);
			}
		}
		return privilegesForDefinedType;
	}

	private String extractPrivilegeFilter(final CMCard privilegeCard) {
		if (getPrivilegedObjectType().getValue().equals(PrivilegedObjectType.CLASS.getValue())) {
			final Object privilegeFilter = privilegeCard.get(PRIVILEGE_FILTER_ATTRIBUTE);
			if (privilegeFilter != null) {
				return (String) privilegeFilter;
			}
		}
		return null;
	}

	private String[] extractDisabledAttributes(final CMCard privilegeCard) {
		String[] disabledAttributes = new String[0];
		if (!getPrivilegedObjectType().getValue().equals(PrivilegedObjectType.CLASS.getValue())) {
			return disabledAttributes;
		}
		if (hasNonePrivilege(privilegeCard)) {
			return getAllAttributesForClass(privilegeCard);
		}
		final Object disabledAttributesObject = privilegeCard.get(DISABLED_ATTRIBUTES_ATTRIBUTE);
		if (disabledAttributesObject != null) {
			disabledAttributes = (String[]) privilegeCard.get(DISABLED_ATTRIBUTES_ATTRIBUTE);
		}

		return disabledAttributes;
	}

	private boolean hasNonePrivilege(final CMCard privilegeCard) {
		final Object type = privilegeCard.get(MODE_ATTRIBUTE);
		return PrivilegeMode.NONE.getValue().equals(type);
	}

	private String[] getAllAttributesForClass(final CMCard privilegeCard) {
		final List<String> classAttributes = Lists.newArrayList();
		final CMClass cmClass = (CMClass) extractPrivilegedObject(privilegeCard);
		for (final CMAttribute attribute : cmClass.getAttributes()) {
			classAttributes.add(attribute.getName());
		}
		return classAttributes.toArray(new String[classAttributes.size()]);
	}

	/*****************************************************************************
	 * The following methods must be defined by all classes that extend this
	 * class
	 *****************************************************************************/

	protected abstract PrivilegedObjectType getPrivilegedObjectType();

	protected abstract SerializablePrivilege extractPrivilegedObject(final CMCard privilegeCard);

	protected abstract CMPrivilege extractPrivilegeMode(final CMCard privilegeCard);

}

package org.cmdbuild.logic.privileges;

import static org.cmdbuild.auth.privileges.constants.GrantConstants.*;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.*;

import java.util.List;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.auth.privileges.constants.PrivilegedObjectType;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.reference.EntryTypeReference;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.model.View;
import org.cmdbuild.model.profile.UIConfiguration;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.factories.CMClassPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.PrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.ViewPrivilegeFetcherFactory;
import org.cmdbuild.services.store.DataViewStore;

import com.google.common.collect.Lists;

public class SecurityLogic implements Logic {

	public static final String GROUP_ATTRIBUTE_DISABLEDMODULES = "DisabledModules";
	public static final String GROUP_ATTRIBUTE_DISABLEDCARDTABS = "DisabledCardTabs";
	public static final String GROUP_ATTRIBUTE_DISABLEDPROCESSTABS = "DisabledProcessTabs";
	public static final String GROUP_ATTRIBUTE_HIDESIDEPANEL = "HideSidePanel";
	public static final String GROUP_ATTRIBUTE_FULLSCREEN = "FullScreenMode";
	public static final String GROUP_ATTRIBUTE_SIMPLE_HISTORY_CARD = "SimpleHistoryModeForCard";
	public static final String GROUP_ATTRIBUTE_SIMPLE_HISTORY_PROCESS = "SimpleHistoryModeForProcess";
	public static final String GROUP_ATTRIBUTE_PROCESS_WIDGET_ALWAYS_ENABLED = "ProcessWidgetAlwaysEnabled";

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

		/**
		 * TODO: add getId(), getName() and getDescription() methods to
		 * CMPrivilegedObject interface?
		 */

		public Long getPrivilegedObjectId() {
			if (privilegedObject instanceof CMClass) {
				return ((CMClass) privilegedObject).getId();
			} else if (privilegedObject instanceof View) {
				return ((View) privilegedObject).getId();
			}
			// TODO: manage domain, report, function
			return null;
		}

		public String getPrivilegedObjectName() {
			if (privilegedObject instanceof CMClass) {
				return ((CMClass) privilegedObject).getIdentifier().getLocalName();
			} else if (privilegedObject instanceof View) {
				return ((View) privilegedObject).getName();
			}
			// TODO: manage domain, report, function
			return null;
		}

		public String getPrivilegedObjectDescription() {
			if (privilegedObject instanceof CMClass) {
				return ((CMClass) privilegedObject).getDescription();
			} else if (privilegedObject instanceof View) {
				return ((View) privilegedObject).getDescription();
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
					&& this.getPrivilegedObjectId().equals(other.getPrivilegedObjectId())) {
				return true;
			}
			return false;
		}

	}

	private final CMDataView view;
	private final CMClass grantClass;

	public SecurityLogic(final CMDataView view) {
		this.view = view;
		this.grantClass = view.findClass(GRANT_CLASS_NAME);
	}

	public List<PrivilegeInfo> fetchClassPrivilegesForGroup(final Long groupId) {
		final List<PrivilegeInfo> fetchedClassPrivileges = fetchStoredPrivilegesForGroup(groupId,
				PrivilegedObjectType.CLASS);
		final Iterable<CMClass> nonReservedActiveClasses = filterNonReservedAndNonBaseClasses();
		for (final CMClass clazz : nonReservedActiveClasses) {
			final Long classId = clazz.getId();
			if (!isPrivilegeAlreadyStored(classId, fetchedClassPrivileges)) {
				final PrivilegeInfo pi = new PrivilegeInfo(groupId, clazz, PrivilegeMode.NONE.getValue());
				fetchedClassPrivileges.add(pi);
			}
		}
		return fetchedClassPrivileges;
	}

	public List<PrivilegeInfo> fetchViewPrivilegesForGroup(final Long groupId) {
		final List<PrivilegeInfo> fetchedViewPrivileges = fetchStoredPrivilegesForGroup(groupId,
				PrivilegedObjectType.VIEW);
		final Iterable<View> allViews = fetchAllViews();
		for (final View view : allViews) {
			final Long viewId = view.getId();
			if (!isPrivilegeAlreadyStored(viewId, fetchedViewPrivileges)) {
				final PrivilegeInfo pi = new PrivilegeInfo(groupId, view, PrivilegeMode.NONE.getValue());
				fetchedViewPrivileges.add(pi);
			}
		}
		return fetchedViewPrivileges;
	}

	private Iterable<View> fetchAllViews() {
		final CMDataView view = TemporaryObjectsBeforeSpringDI.getSystemView();
		final DataViewStore<View> viewStore = new DataViewStore<View>(view, new ViewConverter());
		return viewStore.list();
	}

	/**
	 * Fetches the privileges for specified group. NOTE that the group has no
	 * privilege if it is retrieved and fetched as 'none' or if it is not stored
	 * in the database
	 */
	private List<PrivilegeInfo> fetchStoredPrivilegesForGroup(final Long groupId, final PrivilegedObjectType type) {
		final PrivilegeFetcherFactory privilegeFetcherFactory = getPrivilegeFetcherFactoryForType(type);
		privilegeFetcherFactory.setGroupId(groupId);
		final PrivilegeFetcher privilegeFetcher = privilegeFetcherFactory.create();
		final Iterable<PrivilegePair> privilegePairs = privilegeFetcher.fetch();
		return fromPrivilegePairToPrivilegeInfo(privilegePairs, groupId);
	}

	private PrivilegeFetcherFactory getPrivilegeFetcherFactoryForType(final PrivilegedObjectType type) {
		final DBDataView view = (DBDataView) TemporaryObjectsBeforeSpringDI.getSystemView();
		switch (type) {
		case VIEW:
			return new ViewPrivilegeFetcherFactory(view);
		case CLASS:
			return new CMClassPrivilegeFetcherFactory(view);
		default:
			return null;
		}
	}

	private List<PrivilegeInfo> fromPrivilegePairToPrivilegeInfo(Iterable<PrivilegePair> privilegePairs, Long groupId) {
		List<PrivilegeInfo> list = Lists.newArrayList();
		for (PrivilegePair privilegePair : privilegePairs) {
			CMPrivilegedObject privilegedObject = privilegePair.privilegedObject;
			CMPrivilege privilege = privilegePair.privilege;
			PrivilegeInfo privilegeInfo;
			if (privilege.implies(DefaultPrivileges.WRITE)) {
				privilegeInfo = new PrivilegeInfo(groupId, privilegedObject, PrivilegeMode.WRITE.getValue());
			} else if (privilege.implies(DefaultPrivileges.READ)) {
				privilegeInfo = new PrivilegeInfo(groupId, privilegedObject, PrivilegeMode.READ.getValue());
			} else {
				privilegeInfo = new PrivilegeInfo(groupId, privilegedObject, PrivilegeMode.NONE.getValue());
			}
			list.add(privilegeInfo);
		}
		return list;
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

	private boolean isPrivilegeAlreadyStored(final Long classId, final List<PrivilegeInfo> fetchedPrivileges) {
		for (final PrivilegeInfo privilegeInfo : fetchedPrivileges) {
			if (privilegeInfo.getPrivilegedObjectId() != null && privilegeInfo.getPrivilegedObjectId().equals(classId)) {
				return true;
			}
		}
		return false;
	}

	public void saveClassPrivilege(final Long groupId, final Long classId, final PrivilegeMode mode) {
		final CMQueryResult result = view
				.select(anyAttribute(grantClass))
				.from(grantClass)
				.where(and(condition(attribute(grantClass, GROUP_ID_ATTRIBUTE), eq(groupId)),
						condition(attribute(grantClass, TYPE_ATTRIBUTE), eq(PrivilegedObjectType.CLASS.getValue())))) //
				.run();

		for (final CMQueryRow row : result) {
			final CMCard grantCard = row.getCard(grantClass);
			final EntryTypeReference etr = (EntryTypeReference) grantCard.get(PRIVILEGED_CLASS_ID_ATTRIBUTE);
			if (etr.getId().equals(classId)) {
				updateModeForGrantCard(grantCard, mode);
				return;
			}
		}
		createClassGrantCard(groupId, classId, mode);
	}

	/**
	 * TODO: modify it!!!!
	 * 
	 * @param privilegeInfo
	 */
	public void saveViewPrivilege(final Long groupId, final Long viewId, final PrivilegeMode mode) {
		final CMQueryResult result = view
				.select(anyAttribute(grantClass))
				.from(grantClass)
				.where(and(condition(attribute(grantClass, GROUP_ID_ATTRIBUTE), eq(groupId)),
						condition(attribute(grantClass, TYPE_ATTRIBUTE), eq(PrivilegedObjectType.VIEW.getValue())))) //
				.run();

		for (final CMQueryRow row : result) {
			final CMCard grantCard = row.getCard(grantClass);
			final Long storedViewId = (Long) grantCard.get(PRIVILEGED_OBJECT_ID_ATTRIBUTE);
			if (storedViewId.equals(viewId)) {
				updateModeForGrantCard(grantCard, mode);
				return;
			}
		}
		createViewGrantCard(groupId, viewId, mode);
	}

	private void updateModeForGrantCard(final CMCard grantCard, final PrivilegeMode mode) {
		final CMCardDefinition mutableGrantCard = view.update(grantCard);
		mutableGrantCard.set(MODE_ATTRIBUTE, mode.getValue()).save();
	}

	private void createClassGrantCard(final Long groupId, final Long classId, final PrivilegeMode mode) {
		final CMCardDefinition grantCardToBeCreated = view.createCardFor(grantClass);
		grantCardToBeCreated.set(GROUP_ID_ATTRIBUTE, groupId) //
				.set(PRIVILEGED_CLASS_ID_ATTRIBUTE, classId) //
				.set(MODE_ATTRIBUTE, mode.getValue()) //
				.set(TYPE_ATTRIBUTE, PrivilegedObjectType.CLASS.getValue()) //
				.save();
	}

	private void createViewGrantCard(final Long groupId, final Long viewId, final PrivilegeMode mode) {
		final CMCardDefinition grantCardToBeCreated = view.createCardFor(grantClass);
		grantCardToBeCreated.set(GROUP_ID_ATTRIBUTE, groupId) //
				.set(PRIVILEGED_OBJECT_ID_ATTRIBUTE, viewId) //
				.set(MODE_ATTRIBUTE, mode.getValue()) //
				.set(TYPE_ATTRIBUTE, PrivilegedObjectType.VIEW.getValue()) //
				.save();
	}

	public UIConfiguration fetchGroupUIConfiguration(final Long groupId) {
		final CMClass roleClass = view.findClass("Role");
		final CMQueryRow row = view.select(anyAttribute(roleClass)) //
				.from(roleClass) //
				.where(condition(attribute(roleClass, "Id"), eq(groupId))) //
				.run().getOnlyRow();
		final CMCard roleCard = row.getCard(roleClass);
		final UIConfiguration uiConfiguration = new UIConfiguration();
		uiConfiguration.setDisabledModules((roleCard.get(GROUP_ATTRIBUTE_DISABLEDMODULES) == null) ? null
				: (String[]) roleCard.get(GROUP_ATTRIBUTE_DISABLEDMODULES));
		uiConfiguration.setDisabledCardTabs((roleCard.get(GROUP_ATTRIBUTE_DISABLEDCARDTABS) == null) ? null
				: (String[]) roleCard.get(GROUP_ATTRIBUTE_DISABLEDCARDTABS));
		uiConfiguration.setDisabledProcessTabs((roleCard.get(GROUP_ATTRIBUTE_DISABLEDPROCESSTABS) == null) ? null
				: (String[]) roleCard.get(GROUP_ATTRIBUTE_DISABLEDPROCESSTABS));
		uiConfiguration.setHideSidePanel((Boolean) roleCard.get(GROUP_ATTRIBUTE_HIDESIDEPANEL));
		uiConfiguration.setFullScreenMode((Boolean) roleCard.get(GROUP_ATTRIBUTE_FULLSCREEN));
		uiConfiguration.setSimpleHistoryModeForCard((Boolean) roleCard.get(GROUP_ATTRIBUTE_SIMPLE_HISTORY_CARD));
		uiConfiguration.setSimpleHistoryModeForProcess((Boolean) roleCard.get(GROUP_ATTRIBUTE_SIMPLE_HISTORY_PROCESS));
		uiConfiguration.setProcessWidgetAlwaysEnabled((Boolean) roleCard
				.get(GROUP_ATTRIBUTE_PROCESS_WIDGET_ALWAYS_ENABLED));
		// FIXME: manage cloud admin
		// uiConfiguration.setCloudAdmin(this.isCloudAdmin());
		return uiConfiguration;
	}

	public void saveGroupUIConfiguration(final Long groupId, final UIConfiguration configuration) {
		final CMClass roleClass = view.findClass("Role");
		final CMQueryRow row = view.select(anyAttribute(roleClass)) //
				.from(roleClass) //
				.where(condition(attribute(roleClass, "Id"), eq(groupId))) //
				.run().getOnlyRow();
		final CMCard roleCard = row.getCard(roleClass);
		final CMCardDefinition cardDefinition = view.update(roleCard);
		cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDMODULES, configuration.getDisabledModules());
		cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDCARDTABS, configuration.getDisabledCardTabs());
		cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDPROCESSTABS, configuration.getDisabledProcessTabs());
		cardDefinition.set(GROUP_ATTRIBUTE_HIDESIDEPANEL, configuration.isHideSidePanel());
		cardDefinition.set(GROUP_ATTRIBUTE_FULLSCREEN, configuration.isFullScreenMode());
		cardDefinition.set(GROUP_ATTRIBUTE_SIMPLE_HISTORY_CARD, configuration.isSimpleHistoryModeForCard());
		cardDefinition.set(GROUP_ATTRIBUTE_SIMPLE_HISTORY_PROCESS, configuration.isSimpleHistoryModeForProcess());
		cardDefinition.set(GROUP_ATTRIBUTE_PROCESS_WIDGET_ALWAYS_ENABLED, configuration.isProcessWidgetAlwaysEnabled());
		// FIXME: manage cloud admin
		cardDefinition.save();
	}

}

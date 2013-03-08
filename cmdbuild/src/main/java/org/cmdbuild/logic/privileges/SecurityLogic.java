package org.cmdbuild.logic.privileges;

import static org.cmdbuild.auth.privileges.constants.GrantConstants.GRANT_CLASS_NAME;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.GROUP_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.MODE_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_CLASS_ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;

import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.reference.EntryTypeReference;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.profile.UIConfiguration;

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

		public Long getPrivilegeObjectId() {
			if (privilegedObject instanceof CMClass) {
				return ((CMClass) privilegedObject).getId();
			}
			// TODO: manage domain, report, function, views
			return null;
		}

		public String getPrivilegedObjectName() {
			if (privilegedObject instanceof CMClass) {
				return ((CMClass) privilegedObject).getIdentifier().getLocalName();
			}
			// TODO: manage domain, report, function, views
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
		this.grantClass = view.findClass(GRANT_CLASS_NAME);
	}

	public List<PrivilegeInfo> getPrivilegesForGroup(final Long groupId) {
		final List<PrivilegeInfo> fetchedPrivileges = fetchStoredPrivilegesForGroup(groupId);
		final Iterable<CMClass> nonReservedActiveClasses = filterNonReservedAndNonBaseClasses();
		for (final CMClass clazz : nonReservedActiveClasses) {
			final Long classId = clazz.getId();
			if (!isPrivilegeStoredForClass(classId, fetchedPrivileges)) {
				final PrivilegeInfo pi = new PrivilegeInfo(groupId, clazz, PrivilegeMode.NONE.getValue());
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
		//TODO: use here the privilege fetchers...
		logger.debug("Retrieving privileges for group with id {}", groupId);
		final List<PrivilegeInfo> fetchedPrivileges = Lists.newArrayList();
		final CMQueryResult result = view.select(attribute(grantClass, GROUP_ID_ATTRIBUTE), //
				attribute(grantClass, PRIVILEGED_CLASS_ID_ATTRIBUTE), //
				attribute(grantClass, MODE_ATTRIBUTE)) //
				.from(grantClass) //
				.where(condition(attribute(grantClass, GROUP_ID_ATTRIBUTE), eq(groupId))) //
				.run();
		for (final CMQueryRow row : result) {
			final CMCard grantCard = row.getCard(grantClass);
			final EntryTypeReference entryTypeReference = (EntryTypeReference) grantCard
					.get(PRIVILEGED_CLASS_ID_ATTRIBUTE);
			final String mode = (String) grantCard.get(MODE_ATTRIBUTE);
			final CMClass clazz = view.findClass(entryTypeReference.getId());
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
		final CMQueryResult result = view.select(anyAttribute(grantClass)) //
				.from(grantClass) //
				.where(condition(attribute(grantClass, GROUP_ID_ATTRIBUTE), eq(privilegeInfo.getGroupId()))) //
				.run();

		for (final CMQueryRow row : result) {
			final CMCard grantCard = row.getCard(grantClass);
			final EntryTypeReference etr = (EntryTypeReference) grantCard.get(PRIVILEGED_CLASS_ID_ATTRIBUTE);
			if (etr.getId().equals(privilegeInfo.getPrivilegeObjectId())) {
				updateModeForGrantCard(grantCard, privilegeInfo.getMode());
				return;
			}
		}
		createGrantCard(privilegeInfo);
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

	private void updateModeForGrantCard(final CMCard grantCard, final String mode) {
		final CMCardDefinition modifiableGrant = view.update(grantCard);
		modifiableGrant.set(MODE_ATTRIBUTE, mode).save();
	}

	private void createGrantCard(final PrivilegeInfo privilegeInfo) {
		final CMCardDefinition grantCardToBeCreated = view.createCardFor(grantClass);
		grantCardToBeCreated.set(GROUP_ID_ATTRIBUTE, privilegeInfo.getGroupId()) //
				.set(PRIVILEGED_CLASS_ID_ATTRIBUTE, privilegeInfo.getPrivilegeObjectId()) //
				.set(MODE_ATTRIBUTE, privilegeInfo.getMode()) //
				.save();
	}

}

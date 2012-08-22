package org.cmdbuild.elements.wrappers;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.dao.type.StringArray;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.model.profile.UIConfiguration;
import org.cmdbuild.services.auth.Group;
import org.cmdbuild.services.auth.GroupImpl;
import org.cmdbuild.services.auth.UserContext;

public class GroupCard extends LazyCard {

	private static final long serialVersionUID = 1L;

	public static final String GROUP_CLASS_NAME = "Role";
	public static final String GROUP_NAME_ATTRIBUTE = ICard.CardAttributes.Code.toString();
	public static final String GROUP_DESCRIPTION_ATTRIBUTE = ICard.CardAttributes.Description.toString();

	public static final String GROUP_ATTRIBUTE_EMAIL = "Email";
	public static final String GROUP_ATTRIBUTE_ISADMIN = "Administrator";
	public static final String GROUP_ATTRIBUTE_STARTINGCLASS = "startingClass";

	// UIConfiguration
	public static final String GROUP_ATTRIBUTE_DISABLEDMODULES = "DisabledModules";
	public static final String GROUP_ATTRIBUTE_DISABLEDCARDTABS = "DisabledCardTabs";
	public static final String GROUP_ATTRIBUTE_DISABLEDPROCESSTABS = "DisabledProcessTabs";
	public static final String GROUP_ATTRIBUTE_HIDESIDEPANEL = "HideSidePanel";
	public static final String GROUP_ATTRIBUTE_FULLSCREEN = "FullScreenMode";
	public static final String GROUP_ATTRIBUTE_SIMPLE_HISTORY_CARD = "SimpleHistoryModeForCard";
	public static final String GROUP_ATTRIBUTE_SIMPLE_HISTORY_PROCESS = "SimpleHistoryModeForProcess";
	public static final String GROUP_ATTRIBUTE_PROCESS_WIDGET_ALWAYS_ENABLED = "ProcessWidgetAlwaysEnabled";

	private static final ITable roleClass = UserContext.systemContext().tables().get(GROUP_CLASS_NAME);

	public GroupCard() throws NotFoundException {
		super(roleClass.cards().create());
	}

	// Should not be public but this class moved where it is used
	public GroupCard(final ICard card) throws NotFoundException {
		super(card);
	}

	public Group toGroup(final boolean defaultGroup) {
		return new GroupImpl(this.getId(), this.getName(), this.getDescription(), this.isAdmin(),
				this.getStartingClassOrDefault(), defaultGroup, this.getUIConfiguration());
	}

	public String getName() {
		return getCode();
	}

	public void setName(final String name) {
		if (isNew()) {
			setCode(name);
		} else {
			throw ORMExceptionType.ORM_ERROR_CARD_UPDATE.createException();
		}
	}

	public String getEmail() {
		return getAttributeValue(GROUP_ATTRIBUTE_EMAIL).getString();
	}

	public void setEmail(final String email) {
		getAttributeValue(GROUP_ATTRIBUTE_EMAIL).setValue(email);
	}

	public UIConfiguration getUIConfiguration() {
		final UIConfiguration uiConfiguration = new UIConfiguration();

		uiConfiguration.setDisabledModules(getStringArrayValue(GROUP_ATTRIBUTE_DISABLEDMODULES));
		uiConfiguration.setDisabledCardTabs(getStringArrayValue(GROUP_ATTRIBUTE_DISABLEDCARDTABS));
		uiConfiguration.setDisabledProcessTabs(getStringArrayValue(GROUP_ATTRIBUTE_DISABLEDPROCESSTABS));
		uiConfiguration.setHideSidePanel(getBooleanValue(GROUP_ATTRIBUTE_HIDESIDEPANEL));
		uiConfiguration.setFullScreenMode(getBooleanValue(GROUP_ATTRIBUTE_FULLSCREEN));
		uiConfiguration.setSimpleHistoryModeForCard(getBooleanValue(GROUP_ATTRIBUTE_SIMPLE_HISTORY_CARD));
		uiConfiguration.setSimpleHistoryModeForProcess(getBooleanValue(GROUP_ATTRIBUTE_SIMPLE_HISTORY_PROCESS));
		uiConfiguration.setProcessWidgetAlwaysEnabled(getBooleanValue(GROUP_ATTRIBUTE_PROCESS_WIDGET_ALWAYS_ENABLED));

		return uiConfiguration;
	}

	private String[] getStringArrayValue(final String attributeName) {
		final StringArray sa = (StringArray) getValue(attributeName);
		if (sa != null) {
			return sa.getValue();
		} else {
			return new String[0];
		}
	}

	private boolean getBooleanValue(final String attributeName) {
		final Boolean b = (Boolean) getValue(attributeName);
		if (b != null) {
			return b.booleanValue();
		} else {
			return false;
		}
	}

	public void setUIConfiguration(UIConfiguration uiConfiguration) {
		if (uiConfiguration == null) {
			uiConfiguration = new UIConfiguration();
		}

		setValue(GROUP_ATTRIBUTE_DISABLEDMODULES, uiConfiguration.getDisabledModules());
		setValue(GROUP_ATTRIBUTE_DISABLEDCARDTABS, uiConfiguration.getDisabledCardTabs());
		setValue(GROUP_ATTRIBUTE_DISABLEDPROCESSTABS, uiConfiguration.getDisabledProcessTabs());
		setValue(GROUP_ATTRIBUTE_HIDESIDEPANEL, uiConfiguration.isHideSidePanel());
		setValue(GROUP_ATTRIBUTE_FULLSCREEN, uiConfiguration.isFullScreenMode());
		setValue(GROUP_ATTRIBUTE_SIMPLE_HISTORY_CARD, uiConfiguration.isSimpleHistoryModeForCard());
		setValue(GROUP_ATTRIBUTE_SIMPLE_HISTORY_PROCESS, uiConfiguration.isSimpleHistoryModeForProcess());
		setValue(GROUP_ATTRIBUTE_PROCESS_WIDGET_ALWAYS_ENABLED, uiConfiguration.isProcessWidgetAlwaysEnabled());
	}

	public boolean isAdmin() {
		return getAttributeValue(GROUP_ATTRIBUTE_ISADMIN).getBoolean();
	}

	/*
	 * returns null on no class set
	 */
	public ITable getStartingClassOrDefault() {
		final AttributeValue startingClassValue = getAttributeValue(GROUP_ATTRIBUTE_STARTINGCLASS);
		try {
			if (startingClassValue.isNull() || startingClassValue.getInt() <= 0) {
				final String startingClassName = CmdbuildProperties.getInstance().getStartingClassName();
				if (startingClassName != null)
					return UserContext.systemContext().tables().get(startingClassName);
			} else {
				final Integer startingClassId = this.getStartingClassId();
				if (startingClassId != null)
					return UserContext.systemContext().tables().get(startingClassId);
			}
		} catch (final Exception e) {
		}
		return null;
	}

	public Integer getStartingClassId() {
		if (!getAttributeValue(GROUP_ATTRIBUTE_STARTINGCLASS).isNull())
			return getAttributeValue(GROUP_ATTRIBUTE_STARTINGCLASS).getInt(); // TODO
		return null;
	}

	public void setStartingClass(final Integer startingClass) {
		getAttributeValue(GROUP_ATTRIBUTE_STARTINGCLASS).setValue(startingClass);
	}

	public void setIsAdmin(final Boolean isAdmin) {
		getAttributeValue(GROUP_ATTRIBUTE_ISADMIN).setValue(isAdmin);
	}

	public static Iterable<GroupCard> allActive() throws NotFoundException, ORMException {
		final List<GroupCard> list = new LinkedList<GroupCard>();
		for (final ICard card : roleClass.cards().list())
			list.add(new GroupCard(card));
		return list;
	}

	public static Iterable<GroupCard> all() throws NotFoundException, ORMException {
		final List<GroupCard> list = new LinkedList<GroupCard>();
		for (final ICard card : roleClass.cards().list().ignoreStatus())
			list.add(new GroupCard(card));
		return list;
	}

	public static GroupCard getOrCreate(final int groupId) {
		GroupCard groupCard;
		if (groupId <= 0) {
			final ICard card = UserContext.systemContext().tables().get(GroupCard.GROUP_CLASS_NAME).cards().create();
			groupCard = new GroupCard(card);
		} else {
			groupCard = getOrDie(groupId);
		}
		return groupCard;
	}

	public static GroupCard getOrDie(final int groupId) {
		final ICard card = UserContext.systemContext().tables().get(GroupCard.GROUP_CLASS_NAME).cards().list()
				.ignoreStatus().id(groupId).get();
		return new GroupCard(card);
	}

	public static GroupCard getOrNull(final String groupName) {
		GroupCard groupCard = null;
		if (isNotEmpty(groupName)) {
			final CardQuery groupQuery = UserContext.systemContext().tables().get(GroupCard.GROUP_CLASS_NAME).cards()
					.list().ignoreStatus().filter(GROUP_NAME_ATTRIBUTE, AttributeFilterType.EQUALS, groupName);
			final Iterator<ICard> it = groupQuery.iterator();
			if (it.hasNext()) {
				groupCard = new GroupCard(it.next());
			}
		}
		return groupCard;
	}
}

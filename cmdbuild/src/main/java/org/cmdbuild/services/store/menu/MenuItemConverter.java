package org.cmdbuild.services.store.menu;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.elements.wrappers.MenuCard;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.store.DBDashboardStore;
import org.cmdbuild.services.store.menu.MenuStore.MenuItem;
import org.cmdbuild.services.store.menu.MenuStore.MenuItemType;
import org.cmdbuild.services.store.menu.MenuStore.ReportExtension;

public class MenuItemConverter {

	private static final String NO_GROUP_NAME = "";
	private static final int NO_INDEX = 0;
	private static final Long NO_REFERENCED_ELEMENT_ID = new Long(0);

	/**
	 * 
	 * @param groupName
	 * @param menuItem
	 * @return a menuCard for the given menuItem to assign to the given group
	 *         name
	 */
	public static MenuCard toMenuCard(final String groupName, final MenuItem menuItem) {
		final MenuItemType type = menuItem.getType();
		final MenuItemConverter s;
		if (MenuItemType.REPORT_CSV.equals(type) || MenuItemType.REPORT_PDF.equals(type)) {
			s = new ReportConverterStrategy();
		} else if (MenuItemType.FOLDER.equals(type)) {
			s = new FolderConverterStrategy();
		} else if (MenuItemType.DASHBOARD.equals(type)) {
			s = new DashboardConverterStrategy();
		} else {
			s = new EntryTypeConverterStrategy();
		}

		return s.fromMenuItemToMenuCard(groupName, menuItem);
	}

	/**
	 * 
	 * @param menuCardList
	 * @return a MenuItem starting to a list of MenuCard
	 */
	public static MenuItem fromMenuCard(final Iterable<MenuCard> menuCardList) {
		final MenuItem root = new MenuItemDTO();
		root.setType(MenuItemType.ROOT);
		final Map<Integer, ConvertingItem> items = new HashMap<Integer, ConvertingItem>();

		for (final MenuCard menuCard : menuCardList) {
			final Integer id = menuCard.getId();
			items.put(id, convertMenuCardToMenuItemBuilder(menuCard));
		}

		for (final ConvertingItem item : items.values()) {
			final Integer parentId = item.menuCard.getParentId();
			if (parentId > 0) {
				final ConvertingItem parent = items.get(parentId);
				parent.menuItem.addChild(item.menuItem);
			} else {
				root.addChild(item.menuItem);
			}
		}

		return root;
	}

	/**
	 * 
	 * @param cmClass
	 * @return a MenuItem that is the menu representation of a CMClass
	 */
	public static MenuItem fromCMClass(final CMClass cmClass) {
		final MenuItem menuItem = new MenuItemDTO();
		menuItem.setType(MenuItemType.CLASS);
		menuItem.setReferedClassName(cmClass.getName());
		menuItem.setReferencedElementId(NO_REFERENCED_ELEMENT_ID);
		menuItem.setDescription(cmClass.getDescription());
		menuItem.setGroupName(NO_GROUP_NAME);
		menuItem.setIndex(NO_INDEX);
		return menuItem;
	}

	// FIXME when implement new ReportCard use it as parameter
	public static MenuItem fromCMReport(final CMCard report, final ReportExtension extension) {
		final MenuItem menuItem = new MenuItemDTO();
		if (ReportExtension.CSV.equals(extension)) {
			menuItem.setType(MenuItemType.REPORT_CSV);
		} else if (ReportExtension.PDF.equals(extension)) {
			menuItem.setType(MenuItemType.REPORT_PDF);
		}

		menuItem.setReferedClassName(report.getType().getName());
		menuItem.setReferencedElementId(report.getId());
		menuItem.setDescription((String) report.getDescription());
		menuItem.setGroupName(NO_GROUP_NAME);
		menuItem.setIndex(NO_INDEX);
		return menuItem;
	}

	public static MenuItem fromDashboard(final DashboardDefinition dashboardDefinition, final Long id) {
		final MenuItem menuItem = new MenuItemDTO();

		menuItem.setType(MenuItemType.DASHBOARD);
		menuItem.setReferedClassName("_Dashboard"); // FIXME retrieve the name
													// from a constant
		menuItem.setReferencedElementId(id);
		menuItem.setDescription(dashboardDefinition.getDescription());
		menuItem.setGroupName(NO_GROUP_NAME);
		menuItem.setIndex(NO_INDEX);
		return menuItem;
	}

	private static ConvertingItem convertMenuCardToMenuItemBuilder(final MenuCard menuCard) {
		final MenuItem menuItem = new MenuItemDTO();
		menuItem.setId(new Long(menuCard.getId()));
		menuItem.setType(MenuItemType.getType(menuCard.getCode()));
		menuItem.setDescription(menuCard.getDescription());
		menuItem.setParentId(new Long(menuCard.getParentId()));
		menuItem.setIndex(menuCard.getNumber());
		if (!MenuItemType.FOLDER.equals(menuItem.getType())) {
			final String className = UserOperations.from(UserContext.systemContext()).tables()
					.get(menuCard.getElementClassId()).getName();
			menuItem.setReferedClassName(className);
			menuItem.setReferencedElementId(new Long(menuCard.getElementObjId()));
		}
		menuItem.setGroupName(menuCard.getGroupName());

		return new ConvertingItem(menuCard, menuItem);
	}

	MenuCard fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
		final MenuCard menuCard = new MenuCard();

		final String typeAsString = menuItem.getType().getValue();
		// In the menu card, the code stores the node type.
		// The column Type store an info that could be used
		// for the report, but there are four years that is not implemented,
		// so does not consider this column!!
		menuCard.setCode(typeAsString);
		menuCard.setType("");

		menuCard.setDescription(menuItem.getDescription());
		menuCard.setNumber(menuItem.getIndex());
		menuCard.setGroupName(groupName);

		return menuCard;
	}

	private static class FolderConverterStrategy extends MenuItemConverter {
		@Override
		MenuCard fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			return super.fromMenuItemToMenuCard(groupName, menuItem);
		}
	}

	private static class EntryTypeConverterStrategy extends MenuItemConverter {
		@Override
		MenuCard fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			final MenuCard menuCard = super.fromMenuItemToMenuCard(groupName, menuItem);

			menuCard.setElementObjId(menuItem.getReferencedElementId());
			menuCard.setElementClassId(UserOperations.from(UserContext.systemContext()).tables()
					.get(menuItem.getReferedClassName()).getId());

			return menuCard;
		}
	}

	private static class ReportConverterStrategy extends MenuItemConverter {
		@Override
		MenuCard fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			final MenuCard menuCard = super.fromMenuItemToMenuCard(groupName, menuItem);
			menuCard.setElementObjId(menuItem.getReferencedElementId());
			menuCard.setElementClassId(UserOperations.from(UserContext.systemContext()).tables()
					.get(ReportCard.REPORT_CLASS_NAME).getId());
			return menuCard;
		}
	}

	private static class DashboardConverterStrategy extends MenuItemConverter {
		@Override
		MenuCard fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			final MenuCard menuCard = super.fromMenuItemToMenuCard(groupName, menuItem);
			menuCard.setElementObjId(menuItem.getReferencedElementId());
			menuCard.setElementClassId(UserOperations.from(UserContext.systemContext()).tables()
					.get(DBDashboardStore.DASHBOARD_TABLE).getId());
			return menuCard;
		}
	}

	private static class ConvertingItem {
		public final MenuCard menuCard;
		public final MenuItem menuItem;

		public ConvertingItem(final MenuCard menuCard, final MenuItem menuItemBuilder) {
			this.menuCard = menuCard;
			this.menuItem = menuItemBuilder;
		}
	}
}

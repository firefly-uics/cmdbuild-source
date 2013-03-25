package org.cmdbuild.services.store.menu;

import java.util.List;

import org.cmdbuild.elements.report.ReportFactory;
import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

public interface MenuStore {

	Logger logger = Log.CMDBUILD;

	public static enum ReportExtension {
		PDF(ReportFactory.ReportExtension.PDF.toString().toLowerCase()), //
		CSV(ReportFactory.ReportExtension.CSV.toString().toLowerCase());
		private final String extension;

		ReportExtension(final String extension) {
			this.extension = extension;
		}

		public String getExtension() {
			return this.extension;
		}
	}

	public static enum MenuItemType {
		CLASS("class"), DASHBOARD("dashboard"), PROCESS("processclass"), FOLDER("folder"), REPORT_CSV("reportcsv"), REPORT_PDF(
				"reportpdf"), ROOT("root");

		private final String value;

		MenuItemType(final String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static MenuItemType getType(final String type) {
			for (final MenuItemType itemType : values()) {
				if (itemType.getValue().equals(type)) {
					return itemType;
				}
			}

			return null;
		}
	}

	interface MenuItem {
		/**
		 * 
		 * @return the Id of the menu item
		 */
		Long getId();

		/**
		 * 
		 * @return the type of the menu item
		 */
		MenuItemType getType();

		/**
		 * 
		 * @return the Label to use to describe the menu item
		 */
		String getDescription();

		/**
		 * 
		 * @return the id of the menu item that is the parent of this item
		 */
		Integer getParentId();

		/**
		 * 
		 * @return the name of the class to open, if the menu is associated to a
		 *         class/process. Otherwise, this is the name of a class and you
		 *         have to use also the {@link getElementId}
		 */
		String getReferedClassName();

		/**
		 * 
		 * @return the id of the element that the menu item point to. Currently
		 *         is used for the report. Must be used also for dashboards and
		 *         views
		 */
		Integer getReferencedElementId();

		/**
		 * 
		 * @return a number to sort the menu items
		 */
		int getIndex();

		/**
		 * 
		 * @return the name of the group that owns this menu
		 */
		String getGroupName();

		/**
		 * 
		 * @return the children menu item of this item
		 */
		List<MenuItem> getChildren();

		void setId(Long id);

		void setType(MenuItemType type);

		void setDescription(String description);

		void setParentId(Integer parentId);

		void setReferedClassName(String referencedClassName);

		void setReferencedElementId(Integer referencedElementId);

		void setIndex(int index);

		void setGroupName(String groupName);

		void addChild(MenuItem child);
	}

	/**
	 * @param groupName
	 * @return the menu defined for the given group
	 */
	MenuItem read(String groupName);

	/**
	 * @param groupName
	 *            the group for this menu
	 * @param menuItem
	 *            the menu to save
	 */
	void save(String groupName, MenuItem menuItem);

	/**
	 * 
	 * @param groupName
	 *            the group for which delete the menu
	 */
	void delete(String groupName);

	/**
	 * 
	 * @param groupName
	 * @return a menu that has as top level children the categories of menuItems
	 *         (Classes, Activities, Report, Dashboards and View) The children
	 *         of these nodes are the respective nodes that are not yet added to
	 *         the current menu
	 */
	MenuItem getAvailableItems(String groupName);

	/**
	 * 
	 * @param groupName
	 * @return the menu configured for the group with the given name or the
	 *         default menu if there is not one. In any case, for this menu must
	 *         remove the items for which the group has not the read privileges
	 */
	MenuItem getMenuToUseForGroup(String groupName);
}

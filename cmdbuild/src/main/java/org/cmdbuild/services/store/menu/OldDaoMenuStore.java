package org.cmdbuild.services.store.menu;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.elements.wrappers.MenuCard;
import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.DataAccessLogic.FetchCardListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.model.dashboard.DashboardDefinition;

public class OldDaoMenuStore implements MenuStore {

	private static final String DEFAULT_GROUP = "";

	@Override
	public MenuItem read(final String groupName) {
		final Iterable<MenuCard> menuList = MenuCard.loadListForGroup(groupName);
		return MenuItemConverter.fromMenuCard(menuList);
	}

	@Override
	public void delete(final String groupName) {
		MenuCard.deleteTree(groupName);
	}

	@Override
	public void save(final String groupName, final MenuItem menuItem) {
		delete(groupName);
		saveNode(groupName, menuItem, null);
	}

	@Override
	public MenuItem getAvailableItems(final String groupName) {
		final Iterable<MenuCard> menuList = MenuCard.loadListForGroup(groupName);
		final MenuItem root = new MenuItemDTO();
		root.setType(MenuItemType.ROOT);
		root.addChild(getAvailableClasses(menuList));
		root.addChild(getAvailableProcesses(menuList));
		root.addChild(getAvailableReports(menuList));
		root.addChild(getAvailableDashboards(menuList));

		return root;
	}

	@Override
	public MenuItem getMenuToUseForGroup(final String groupName) {
		// TODO check privileges
		Iterable<MenuCard> menuList = MenuCard.loadListForGroup(groupName);
		if (!menuList.iterator().hasNext()) {

			// is empty, look for the default menu
			menuList = MenuCard.loadListForGroup(DEFAULT_GROUP);
		}

		return MenuItemConverter.fromMenuCard(menuList);
	}

	private void saveNode(final String groupName, final MenuItem menuItem, final Integer parentId) {
		Integer savedNodeId = null;

		// The root node is not useful, and is not saved on DB
		if (!menuItem.getType().equals(MenuItemType.ROOT)) {
			final MenuCard menuCard = MenuItemConverter.toMenuCard(groupName, menuItem);

			if (parentId == null) {
				menuCard.setParentId(0);
			} else {
				menuCard.setParentId(parentId);
			}

			menuCard.save();
			savedNodeId = menuCard.getId();
		}

		// save the children (comment not useful but funny)
		for (final MenuItem child : menuItem.getChildren()) {
			saveNode(groupName, child, savedNodeId);
		}
	}

	private MenuItem getAvailableClasses(final Iterable<MenuCard> menuList) {
		final MenuItem classesFolder = new MenuItemDTO();
		classesFolder.setType(MenuItemType.FOLDER);
		classesFolder.setDescription("class");
		classesFolder.setIndex(0);

		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final CMDataView systemDataView = TemporaryObjectsBeforeSpringDI.getSystemView();

		for (final CMClass cmClass : systemDataView.findClasses()) {
			if (cmClass.isSystem() || cmClass.isBaseClass() || isInTheMenuList(cmClass, menuList)
					|| dataAccessLogic.isProcess(cmClass)) {
				continue;
			}

			classesFolder.addChild(MenuItemConverter.fromCMClass(cmClass));
		}

		return classesFolder;
	}

	private MenuItem getAvailableProcesses(final Iterable<MenuCard> menuList) {

		final MenuItem processesFolder = new MenuItemDTO();
		processesFolder.setType(MenuItemType.FOLDER);
		processesFolder.setDescription("processclass");
		processesFolder.setIndex(1);

		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final CMDataView systemDataView = TemporaryObjectsBeforeSpringDI.getSystemView();

		for (final CMClass cmClass : systemDataView.findClasses()) {
			if (cmClass.isSystem() || isInTheMenuList(cmClass, menuList) || !dataAccessLogic.isProcess(cmClass)) {
				continue;
			}

			processesFolder.addChild(MenuItemConverter.fromCMClass(cmClass));
		}

		return processesFolder;
	}

	private MenuItem getAvailableReports(final Iterable<MenuCard> menuList) {
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final CMDataView systemDataView = TemporaryObjectsBeforeSpringDI.getSystemView();
		final CMClass reportTable = systemDataView.getReportClass();

		final MenuItem reportFolder = new MenuItemDTO();
		reportFolder.setType(MenuItemType.FOLDER);
		reportFolder.setDescription("report");
		reportFolder.setIndex(2);

		final FetchCardListResponse reports = dataAccessLogic.fetchCards(reportTable.getName(), QueryOptions
				.newQueryOption().build());

		for (final CMCard report : reports) {
			for (final ReportExtension extension : ReportExtension.values()) {
				if (thereIsNotAlreadyInTheMenu(report, extension, menuList)) {
					reportFolder.addChild(MenuItemConverter.fromCMReport(report, extension));
				}
			}
		}

		return reportFolder;
	}

	private MenuItem getAvailableDashboards(final Iterable<MenuCard> menuList) {

		final MenuItem dashboardFolder = new MenuItemDTO();
		dashboardFolder.setType(MenuItemType.FOLDER);
		dashboardFolder.setDescription("dashboard");
		dashboardFolder.setIndex(3);

		final DashboardLogic dl = TemporaryObjectsBeforeSpringDI.getDashboardLogic();
		final Map<Long, DashboardDefinition> dashboards = dl.fullListDashboards();
		for (final Long id : dashboards.keySet()) {

			if (thereIsNotAlreadyInTheMenu(id, menuList)) {
				dashboardFolder.addChild(MenuItemConverter.fromDashboard(dashboards.get(id), id));
			}
		}

		return dashboardFolder;
	}

	private boolean thereIsNotAlreadyInTheMenu(final CMCard report, final ReportExtension extension,
			final Iterable<MenuCard> menuList) {
		for (final MenuCard menuItem : menuList) {
			final String suffix = extension.getExtension();
			if (menuItem.getElementObjId() == report.getId() && menuItem.getCode().endsWith((suffix))) {
				return false;
			}
		}

		return true;
	}

	private boolean thereIsNotAlreadyInTheMenu(final Long id, final Iterable<MenuCard> menuList) {
		for (final MenuCard menuItem : menuList) {
			if (menuItem.getElementObjId() == id) {
				return false;
			}
		}

		return true;
	}

	private boolean isInTheMenuList(final CMClass cmClass, final Iterable<MenuCard> menuList) {
		if (menuList == null) {
			return false;
		}

		for (final MenuCard menuItem : menuList) {
			if (menuItem.getElementClassId() == cmClass.getId()) {
				return true;
			}
		}

		return false;
	}
}

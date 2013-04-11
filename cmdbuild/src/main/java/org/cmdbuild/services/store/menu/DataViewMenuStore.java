package org.cmdbuild.services.store.menu;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_CLASS_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_OBJECT_ID_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.GROUP_NAME_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.MENU_CLASS_NAME;
import static org.cmdbuild.services.store.menu.MenuConstants.PARENT_ID_ATTRIBUTE;

import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.DataViewCardFetcher;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.model.View;
import org.cmdbuild.model.dashboard.DashboardDefinition;

import com.google.common.collect.Lists;

public class DataViewMenuStore implements MenuStore {

	private static final String DEFAULT_MENU_GROUP_NAME = "*";
	private final CMDataView view;
	private final AuthenticationLogic authLogic;
	private final DashboardLogic dashboardLogic;
	private final DataAccessLogic dataAccessLogic;
	private final PrivilegeContextFactory privilegeContextFactory;

	public DataViewMenuStore(final CMDataView view, final AuthenticationLogic authLogic,
			final DashboardLogic dashboardLogic, final DataAccessLogic dataAccessLogic,
			final PrivilegeContextFactory privilegeContextFactory) {
		this.view = view;
		this.authLogic = authLogic;
		this.dashboardLogic = dashboardLogic;
		this.dataAccessLogic = dataAccessLogic;
		this.privilegeContextFactory = privilegeContextFactory;
	}

	@Override
	public MenuItem read(String groupName) {
		groupName = groupNameOrDefaultMenuGroupName(groupName);

		final Iterable<CMCard> menuCards = fetchMenuCardsForGroup(groupName);
		return MenuItemConverter.fromMenuCard(menuCards);
	}

	private String groupNameOrDefaultMenuGroupName(String groupName) {
		if ("".equals(groupName) || groupName == null) {
			groupName = DEFAULT_MENU_GROUP_NAME;
		}
		return groupName;
	}

	@Override
	public void delete(String groupName) {
		groupName = groupNameOrDefaultMenuGroupName(groupName);

		final Iterable<CMCard> cardsToDelete = fetchMenuCardsForGroup(groupName);
		for (final CMCard cardToDelete : cardsToDelete) {
			view.delete(cardToDelete);
		}
	}

	@Override
	public void save(String groupName, final MenuItem menuItem) {
		groupName = groupNameOrDefaultMenuGroupName(groupName);

		delete(groupName);
		saveNode(groupName, menuItem, null);
	}

	@Override
	public MenuItem getAvailableItems(final String groupName) {
		final Iterable<CMCard> menuCards = fetchMenuCardsForGroup(groupNameOrDefaultMenuGroupName(groupName));
		final MenuItem root = new MenuItemDTO();
		root.setType(MenuItemType.ROOT);
		root.addChild(getAvailableClasses(menuCards));
		root.addChild(getAvailableProcesses(menuCards));
		root.addChild(getAvailableReports(menuCards));
		root.addChild(getAvailableDashboards(menuCards));
		root.addChild(getAvailableViews(menuCards));
		return root;
	}

	private Iterable<CMCard> fetchMenuCardsForGroup(final String groupName) {
		final List<CMCard> menuCards = Lists.newArrayList();
		final CMClass menuClass = view.findClass(MENU_CLASS_NAME);
		final CMQueryResult result = view.select(anyAttribute(menuClass)) //
				.from(menuClass) //
				.where(condition(attribute(menuClass, GROUP_NAME_ATTRIBUTE), eq(groupName))) //
				.run();
		for (final CMQueryRow row : result) {
			menuCards.add(row.getCard(menuClass));
		}
		return menuCards;
	}

	@Override
	public MenuItem getMenuToUseForGroup(final String groupName) {
		Iterable<CMCard> menuCards = fetchMenuCardsForGroup(groupName);
		final boolean isThereAMenuForCurrentGroup = menuCards.iterator().hasNext();
		final CMGroup group = authLogic.getGroupWithName(groupName);
		final MenuCardFilter menuCardFilter = new MenuCardFilter(view, group, privilegeContextFactory);
		Iterable<CMCard> readableMenuCards;
		if (isThereAMenuForCurrentGroup) {
			readableMenuCards = menuCardFilter.filterReadableMenuCards(menuCards);
		} else {
			menuCards = fetchMenuCardsForGroup(DEFAULT_MENU_GROUP_NAME);
			readableMenuCards = menuCardFilter.filterReadableMenuCards(menuCards);
		}

		return MenuItemConverter.fromMenuCard(readableMenuCards);
	}

	private void saveNode(final String groupName, final MenuItem menuItem, final Long parentId) {
		Long savedNodeId = null;
		// The root node is not useful, and is not saved on DB
		if (!menuItem.getType().equals(MenuItemType.ROOT)) {
			final CMCardDefinition mutableMenuCard = MenuItemConverter.toMenuCard(groupName, menuItem);
			if (parentId == null) {
				mutableMenuCard.set(PARENT_ID_ATTRIBUTE, 0);
			} else {
				mutableMenuCard.set(PARENT_ID_ATTRIBUTE, parentId);
			}
			final CMCard savedCard = mutableMenuCard.save();
			savedNodeId = savedCard.getId();
		}
		// save the children (comment not useful but funny)
		for (final MenuItem child : menuItem.getChildren()) {
			saveNode(groupName, child, savedNodeId);
		}
	}

	private MenuItem getAvailableClasses(final Iterable<CMCard> menuCards) {
		final MenuItem classesFolder = new MenuItemDTO();
		classesFolder.setType(MenuItemType.FOLDER);
		classesFolder.setDescription("class");
		classesFolder.setIndex(0);
		for (final CMClass cmClass : view.findClasses()) {
			if (cmClass.isSystem() || cmClass.isBaseClass() || isInTheMenuList(cmClass, menuCards)
					|| dataAccessLogic.isProcess(cmClass)) {
				continue;
			}
			classesFolder.addChild(MenuItemConverter.fromCMClass(cmClass));
		}
		return classesFolder;
	}

	private MenuItem getAvailableProcesses(final Iterable<CMCard> menuCards) {

		final MenuItem processesFolder = new MenuItemDTO();
		processesFolder.setType(MenuItemType.FOLDER);
		processesFolder.setDescription("processclass");
		processesFolder.setIndex(1);

		for (final CMClass cmClass : view.findClasses()) {
			if (cmClass.isSystem() || isInTheMenuList(cmClass, menuCards) || !dataAccessLogic.isProcess(cmClass)) {
				continue;
			}

			processesFolder.addChild(MenuItemConverter.fromCMClass(cmClass));
		}

		return processesFolder;
	}

	private MenuItem getAvailableReports(final Iterable<CMCard> menuCards) {
		final CMClass reportTable = view.getReportClass();

		final MenuItem reportFolder = new MenuItemDTO();
		reportFolder.setType(MenuItemType.FOLDER);
		reportFolder.setDescription("report");
		reportFolder.setIndex(2);

		final PagedElements<CMCard> reports = DataViewCardFetcher.newInstance() //
				.withDataView(view) //
				.withClassName(reportTable.getIdentifier().getLocalName()) //
				.withQueryOptions(QueryOptions.newQueryOption().build()) //
				.build() //
				.fetch();

		for (final CMCard report : reports) {
			for (final ReportExtension extension : ReportExtension.values()) {
				if (thereIsNotAlreadyInTheMenu(report, extension, menuCards)) {
					reportFolder.addChild(MenuItemConverter.fromCMReport(report, extension));
				}
			}
		}

		return reportFolder;
	}

	private MenuItem getAvailableDashboards(final Iterable<CMCard> menuCards) {
		final MenuItem dashboardFolder = new MenuItemDTO();
		dashboardFolder.setType(MenuItemType.FOLDER);
		dashboardFolder.setDescription("dashboard");
		dashboardFolder.setIndex(3);
		final Map<Integer, DashboardDefinition> dashboards = dashboardLogic.fullListDashboards();
		for (final Integer id : dashboards.keySet()) {
			if (!isInTheMenuList(id, menuCards)) {
				dashboardFolder.addChild(MenuItemConverter.fromDashboard(dashboards.get(id), id));
			}
		}

		return dashboardFolder;
	}

	private MenuItem getAvailableViews(final Iterable<CMCard> menuCards) {
		final MenuItem viewsFolder = new MenuItemDTO();
		viewsFolder.setType(MenuItemType.FOLDER);
		viewsFolder.setDescription("view");
		viewsFolder.setIndex(4);

		final ViewLogic viewLogic = new ViewLogic();
		final List<View> definedViews = viewLogic.fetchViewsOfAllTypes();

		for (final View view : definedViews) {
			final Integer id = new Integer(view.getId().intValue());
			if (!isInTheMenuList(id, menuCards)) {
				viewsFolder.addChild(MenuItemConverter.fromView(view));
			}
		}

		return viewsFolder;
	}

	private boolean thereIsNotAlreadyInTheMenu(final CMCard report, final ReportExtension extension,
			final Iterable<CMCard> menuCards) {
		for (final CMCard menuCard : menuCards) {
			final String suffix = extension.getExtension();
			if (menuCard.get(ELEMENT_OBJECT_ID_ATTRIBUTE) == report.getId()
					&& ((String) menuCard.getCode()).endsWith((suffix))) {
				return false;
			}
		}

		return true;
	}

	private boolean isInTheMenuList(final Integer id, final Iterable<CMCard> menuCards) {
		for (final CMCard menuCard : menuCards) {
			final Object elementObjectId = menuCard.get(ELEMENT_OBJECT_ID_ATTRIBUTE);
			if (elementObjectId != null) {
				if (((Integer) elementObjectId).equals(id)) {

					return true;
				}
			}
		}
		return false;
	}

	private boolean isInTheMenuList(final CMClass cmClass, final Iterable<CMCard> menuCards) {
		if (menuCards == null) {
			return false;
		}
		for (final CMCard menuCard : menuCards) {
			final Long elementClassId = menuCard.get(ELEMENT_CLASS_ATTRIBUTE, Long.class);
			if (elementClassId != null && !menuCard.get("Type").equals(MenuItemType.FOLDER.getValue())) {
				if (elementClassId.equals(cmClass.getId())) {
					return true;
				}
			}
		}
		return false;
	}
}

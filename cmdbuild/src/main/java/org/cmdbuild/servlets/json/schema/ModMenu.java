package org.cmdbuild.servlets.json.schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.wrappers.MenuCard;
import org.cmdbuild.elements.wrappers.MenuCard.AllowedReportExtension;
import org.cmdbuild.elements.wrappers.MenuCard.MenuCodeType;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.ClassSerializer;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModMenu extends JSONBase {

	private final String DASHBOARD = "dashboard";

	@Admin
	@JSONExported
	public JSONArray getMenu( //
			@Parameter(PARAMETER_GROUP) final String groupName //
	) throws JSONException, AuthException, NotFoundException, ORMException {
		JSONArray out = new JSONArray();
		if (groupName != null) {
			final Iterable<MenuCard> menuList = MenuCard.loadListForGroup(groupName);
			out = Serializer.serializeMenuList(menuList, UserContext.systemContext(), null);
		}

		return out;
	}

	@Admin
	@JSONExported
	public JSONArray getAvailableItemsMenu( //
			final JSONObject serializer, //
			final ITableFactory tf, //
			final UserContext userCtx, //
			@Parameter("group") final String groupName //
	) throws JSONException {
		final Iterable<MenuCard> menuList = MenuCard.loadListForGroup(groupName);
		final JSONArray jsonAvailableItems = Serializer.buildJsonAvaiableMenuItems();

		addAvailableTables(tf, menuList, jsonAvailableItems);
		addAvailableReports(userCtx, menuList, jsonAvailableItems);
		addAvailableDashboards(userCtx, jsonAvailableItems);

		return jsonAvailableItems;
	}

	@Admin
	@JSONExported
	public JSONObject saveMenu( //
			final JSONObject serializer, //
			@Parameter("group") final String groupName, //
			@Parameter("menuItems") final JSONArray jsonMenuItems //
	) throws Exception {
		MenuCard.deleteTree(groupName);
		final Map<Object, Integer> idMap = new HashMap<Object, Integer>();

		int len = jsonMenuItems.length();
		while (len > 0) {
			final int startLength = len;

			for (int i = 0; i < jsonMenuItems.length(); ++i) {
				final JSONObject jsonItem = jsonMenuItems.getJSONObject(i);

				if (idMap.keySet().contains(jsonItem.get("id"))) {
					continue;
				}

				MenuCard menuItem = new MenuCard();
				menuItem = getMenuCardFromJSONObject(groupName, jsonItem);

				if (jsonItem.has("parent") && idMap.keySet().contains(jsonItem.get("parent"))) {
					menuItem.setParentId(idMap.get(jsonItem.get("parent")));
				}

				menuItem.save();
				final Object givenId = jsonItem.get("id");
				idMap.put(givenId, menuItem.getId());
				--len;
			}

			if (len == startLength) {
				throw new Exception();
			}
		}

		return serializer;
	}

	private MenuCard getMenuCardFromJSONObject(final String groupName, final JSONObject jsonItem) throws JSONException {
		final MenuCard menuItem = new MenuCard();

		final String type = jsonItem.getString("type");
		if (MenuCodeType.FOLDER.getCodeType().equals(type)) {
			menuItem.setCode(MenuCodeType.FOLDER.getCodeType());
			menuItem.setType(type);
		} else {
			menuItem.setCode(MenuCard.getCodeValueOf(type).getCodeType());
			if (menuItem.isReport()) {
				menuItem.setElementClassId(UserOperations.from(UserContext.systemContext()).tables()
						.get(ReportCard.REPORT_CLASS_NAME).getId());
				menuItem.setElementObjId(jsonItem.getInt("objid"));
				final String subtype = jsonItem.getString("subtype");
				if (subtype != null && !subtype.equals("")) {
					menuItem.setType(MenuCard.getTypeValueOf(subtype).getType());
				}
			} else {
				menuItem.setElementClassId(jsonItem.getInt("id"));
				menuItem.setType(type);
			}
		}
		menuItem.setNumber(jsonItem.getInt("cmIndex"));
		menuItem.setDescription(jsonItem.getString("text"));
		menuItem.setGroupName(groupName);
		return menuItem;
	}

	@Admin
	@JSONExported
	public JSONObject deleteMenu( //
			final JSONObject serializer, //
			@Parameter("group") final String groupName //
	) throws JSONException {
		MenuCard.deleteTree(groupName);
		return serializer;
	}

	@JSONExported
	public JSONArray getGroupMenu( //
			final UserContext userCtx //
	) throws JSONException, AuthException, NotFoundException, ORMException {
		JSONArray response = null;
		// get the menu associated to the user's group
		final String defaultGroupName = userCtx.getDefaultGroup().getName();
		Iterable<MenuCard> menuList = MenuCard.loadListForGroup(defaultGroupName);
		// if there isn't any menu associated to the user's group, get the
		// default menu
		if (menuList != null && !menuList.iterator().hasNext()) {
			menuList = MenuCard.loadListForGroup(MenuCard.DEFAULT_GROUP);
		}

		if (menuList != null && menuList.iterator().hasNext()) {
			try {
				response = Serializer.serializeMenuList(menuList, userCtx, getAvailableReportId(userCtx));
			} catch (final NullPointerException e) {
				// Empty tree if something goes wrong... AKA workflow problems
			}
		}

		if (response == null) {
			response = new JSONArray();
		}

		return response;
	}

	private Set<Integer> getAvailableReportId(final UserContext userCtx) {
		final Set<Integer> availableReports = new HashSet<Integer>();
		for (final ReportCard report : ReportCard.findAll()) {
			if (report.isUserAllowed(userCtx)) {
				availableReports.add(report.getId());
			}
		}
		return availableReports;
	}

	private void addAvailableTables(final ITableFactory tf, final Iterable<MenuCard> menuList,
			final JSONArray jsonAvaiableItems) throws JSONException {
		final Iterable<ITable> allTables = tf.list();
		for (final ITable table : allTables) {
			if (!table.isAllowedOnTrees() || isInTheMenuList(table, menuList)) {
				continue;
			} else {
				final JSONObject jsonTable = ClassSerializer.toClient(table);
				if (table.isActivity()) {
					jsonTable.put("parent", Serializer.AVAILABLE_PROCESS_CLASS);
				} else {
					jsonTable.put("parent", Serializer.AVAILABLE_CLASS);
				}
				// to deny the possibility to append a node
				jsonTable.put("leaf", true);
				jsonAvaiableItems.put(jsonTable);
			}
		}
	}

	private void addAvailableReports(final UserContext userCtx, final Iterable<MenuCard> menuList,
			final JSONArray jsonAvaiableItems) throws JSONException {
		final Iterable<ReportCard> allReports = ReportCard.findAll();
		for (final ReportCard report : allReports) {
			if (!report.isUserAllowed(userCtx)) {
				continue;
			} else {
				for (final AllowedReportExtension reportExtension : MenuCard.AllowedReportExtension.values()) {
					final String type = "report" + reportExtension.getExtension();
					if (isInTheMenuList(report, type, menuList)) {
						continue;
					}
					final JSONObject jsonReport = Serializer.serializeReportForMenu(report, type);
					jsonAvaiableItems.put(jsonReport);
				}
			}
		}
	}

	private void addAvailableDashboards(final UserContext userCtx, final JSONArray jsonAvailableItems)
			throws JSONException {
		final DashboardLogic dl = TemporaryObjectsBeforeSpringDI.getDashboardLogic(userCtx);
		final Map<Long, DashboardDefinition> dashboards = dl.fullListDashboards();
		for (final Long id : dashboards.keySet()) {
			final DashboardDefinition df = dashboards.get(id);
			final JSONObject jsonDashboard = new JSONObject();
			jsonDashboard.put("id", id);
			jsonDashboard.put("text", df.getDescription());
			jsonDashboard.put("leaf", true);
			jsonDashboard.put("cmName", DASHBOARD);
			jsonDashboard.put("iconCls", DASHBOARD);
			jsonDashboard.put("type", DASHBOARD);
			jsonDashboard.put("parent", Serializer.AVAILABLE_DASHBOARDS);

			jsonAvailableItems.put(jsonDashboard);
		}
	}

	private boolean isInTheMenuList(final ReportCard report, final String type, final Iterable<MenuCard> menuList) {
		for (final MenuCard menuItem : menuList) {
			if (menuItem.getElementObjId() == report.getId() && menuItem.getCode().equals(type)) {
				return true;
			}
		}
		return false;
	}

	private boolean isInTheMenuList(final ITable table, final Iterable<MenuCard> menuList) {
		for (final MenuCard menuItem : menuList) {
			if (menuItem.getElementClassId() == table.getId()) {
				return true;
			}
		}
		return false;
	}

}

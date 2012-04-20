package org.cmdbuild.servlets.json.schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.wrappers.MenuCard;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.elements.wrappers.MenuCard.AllowedReportExtension;
import org.cmdbuild.elements.wrappers.MenuCard.MenuCodeType;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModMenu extends JSONBase {

	@Admin
	@JSONExported
	public JSONArray getMenu(
			JSONObject serializer,
			@Parameter("group") String groupName) throws JSONException {
		if (groupName != null) {
			Iterable<MenuCard> menuList = MenuCard.loadListForGroup(groupName);
			return Serializer.serializeMenuList(menuList, UserContext.systemContext(), null);
		}
		return new JSONArray();
	}

	@Admin
	@JSONExported
	public JSONArray getAvailableItemsMenu(
			JSONObject serializer,
			ITableFactory tf,
			UserContext userCtx,
			@Parameter("group") String groupName) throws JSONException {
		Iterable<MenuCard> menuList = MenuCard.loadListForGroup(groupName);
		JSONArray jsonAvailableItems = Serializer.buildJsonAvaiableMenuItems();

		addAvailableTables(tf, menuList, jsonAvailableItems);
		addAvailableReports(userCtx, menuList, jsonAvailableItems);
		return jsonAvailableItems;
	}

	@Admin
	@JSONExported
	public JSONObject saveMenu(
			JSONObject serializer,
			@Parameter("group") String groupName,
			@Parameter("menuItems") JSONArray jsonMenuItems
	) throws Exception {
		MenuCard.deleteTree(groupName);
		Map<Object, Integer> idMap = new HashMap<Object, Integer>();

		int len=jsonMenuItems.length();
		while (len > 0) {
			int startLength = len;

			for (int i=0; i<jsonMenuItems.length(); ++i) {
				JSONObject jsonItem = jsonMenuItems.getJSONObject(i);

				if (idMap.keySet().contains(jsonItem.get("id"))) {
					continue;
				}

				MenuCard menuItem = new MenuCard();
				menuItem = getMenuCardFromJSONObject(groupName, jsonItem);

				if (jsonItem.has("parent") &&
						idMap.keySet().contains(jsonItem.get("parent"))) {
					menuItem.setParentId(idMap.get(jsonItem.get("parent")));
				}

				menuItem.save();
				Object givenId = jsonItem.get("id");
				idMap.put(givenId, menuItem.getId());
				--len;
			}

			if (len == startLength) {
				throw new Exception();
			}
		}

		return serializer;
	}

	private MenuCard getMenuCardFromJSONObject(String groupName, JSONObject jsonItem) throws JSONException {
		MenuCard menuItem = new MenuCard();

		String type = jsonItem.getString("type");
		if (MenuCodeType.FOLDER.getCodeType().equals(type)) {
			menuItem.setCode(MenuCodeType.FOLDER.getCodeType());
			menuItem.setType(type);
		} else {
			menuItem.setCode(MenuCard.getCodeValueOf(type).getCodeType());
			if (menuItem.isReport()) {
				menuItem.setElementClassId(UserContext.systemContext().tables().get(ReportCard.REPORT_CLASS_NAME).getId());
				menuItem.setElementObjId(jsonItem.getInt("objid"));
				String subtype = jsonItem.getString("subtype");
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
	public JSONObject deleteMenu(
			JSONObject serializer,
			@Parameter("group") String groupName
		) throws JSONException {
		MenuCard.deleteTree(groupName);
		return serializer;
	}

	@JSONExported
	public JSONArray getGroupMenu(
			UserContext userCtx
		) throws JSONException, AuthException, NotFoundException, ORMException {
		JSONArray response = null;
		//get the menu associated to the user's group
		Iterable<MenuCard> menuList = MenuCard.loadListForGroup(userCtx.getDefaultGroup().getName());
		//if there isn't any menu associated to the user's group, get the default menu
		if (!menuList.iterator().hasNext()) {
			menuList = MenuCard.loadListForGroup(MenuCard.DEFAULT_GROUP);
		}

		if (menuList.iterator().hasNext()) {
			try {
				response = Serializer.serializeMenuList(menuList, userCtx, getAvailableReportId(userCtx));
			} catch (NullPointerException e) {
				// Empty tree if something goes wrong... AKA workflow problems
			}
		}

		if (response == null) {
			response = new JSONArray();
		}

		return response;
	}

	private Set<Integer> getAvailableReportId(UserContext userCtx) {
		Set<Integer> availableReports = new HashSet<Integer>();
		for (ReportCard report : ReportCard.findAll()) {
			if (report.isUserAllowed(userCtx)) {
				availableReports.add(report.getId());
			}
		}
		return availableReports;
	}

	private void addAvailableTables(ITableFactory tf,
			Iterable<MenuCard> menuList, JSONArray jsonAvaiableItems)
			throws JSONException {
		Iterable<ITable> allTables = tf.list();
		for (ITable table: allTables) {
			if (!table.isAllowedOnTrees() || isInTheMenuList(table, menuList)) {
				continue;
			} else {
				JSONObject jsonTable = Serializer.serializeTable(table);
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

	private void addAvailableReports(UserContext userCtx,
			Iterable<MenuCard> menuList, JSONArray jsonAvaiableItems)
			throws JSONException {
		Iterable<ReportCard> allReports = ReportCard.findAll();
		for (ReportCard report: allReports) {
			if (!report.isUserAllowed(userCtx)) {
				continue;
			} else {
				for (AllowedReportExtension reportExtension: MenuCard.AllowedReportExtension.values()) {
					String type = "report"+reportExtension.getExtension();
					if (isInTheMenuList(report, type, menuList)) {
						continue;
					}
					JSONObject jsonReport = Serializer.serializeReportForMenu(report, type);
					jsonAvaiableItems.put(jsonReport);
				}
			}
		}
	}

	private boolean isInTheMenuList(ReportCard report, String type, Iterable<MenuCard> menuList) {
		for (MenuCard menuItem: menuList) {
			if (menuItem.getElementObjId() == report.getId() && menuItem.getCode().equals(type)) {
				return true;
			}
		}
		return false;
	}

	private boolean isInTheMenuList(ITable table, Iterable<MenuCard> menuList) {
		for (MenuCard menuItem: menuList) {
			if (menuItem.getElementClassId() == table.getId()) {
				return true;
			}
		}
		return false;
	}


}

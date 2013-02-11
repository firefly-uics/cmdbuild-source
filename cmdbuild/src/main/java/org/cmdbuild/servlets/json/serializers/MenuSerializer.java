package org.cmdbuild.servlets.json.serializers;

import java.util.Set;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.wrappers.MenuCard;
import org.cmdbuild.elements.wrappers.MenuCard.MenuCodeType;
import org.cmdbuild.elements.wrappers.MenuCard.MenuType;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.store.menu.MenuItemDTO;
import org.cmdbuild.services.store.menu.MenuStore;
import org.cmdbuild.services.store.menu.MenuStore.MenuItem;
import org.cmdbuild.services.store.menu.MenuStore.MenuItemType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MenuSerializer {

	public static final String
		MENU = "menu",
		CHILDREN = "children",
		CLASS_NAME = "referencedClassName",
		DESCRIPTION = "description",
		ELEMENT_ID = "referencedElementId",
		INDEX = "index",
		TYPE = "type";

	public static JSONObject toClient(final MenuItem menu, final boolean withWrapper) throws JSONException {
		final JSONObject out = singleToClient(menu);
		if (menu.getChildren().size() > 0) {
			JSONArray children = new JSONArray();
			for (MenuItem child: menu.getChildren()) {
				children.put(toClient(child, false));
			}

			out.put(CHILDREN, children);
		}

		if (withWrapper) {
			return new JSONObject() {{
				put(MENU, out);
			}};
		} else {
			return out;
		}
	}

	public static JSONObject singleToClient(final MenuItem menu) throws JSONException {
		final JSONObject out = new JSONObject();
		out.put(TYPE, menu.getType().getValue());
		out.put(INDEX, menu.getIndex());
		out.put(ELEMENT_ID, menu.getReferencedElementId());
		out.put(DESCRIPTION, menu.getDescription());
		out.put(CLASS_NAME, menu.getReferedClassName());
		return out;
	}

	public static JSONArray toClient(final Iterable<MenuCard> menuList, final UserContext userCtx,
			final Set<Integer> availableReports) throws JSONException {
		final JSONArray jsonMenuList = new JSONArray();

		if (menuList == null) {
			return jsonMenuList;
		}

		for (final MenuCard menu : menuList) {
			boolean isFolder = true;
			final JSONObject jsonMenu = new JSONObject();

			if (menu.getCode() != null) {
				isFolder = menu.getCode().equals(MenuCodeType.FOLDER.getCodeType());
				if (menu.isReport()) {
					if (availableReports != null && !availableReports.contains(menu.getElementObjId())) {
						continue;
					}
				} else {
					try { // Ugly but I can't fix every design mistake right now
						final ITable menuEntryClass = UserOperations.from(UserContext.systemContext()).tables()
								.get(menu.getElementClassId());
						final PrivilegeType privileges = userCtx.privileges().getPrivilege(menuEntryClass);
						if (PrivilegeType.NONE.equals(privileges))
							continue; // Exits for the outer loop
						final MenuType menuType = menu.getTypeEnum();
						final boolean writePriv = PrivilegeType.WRITE.equals(privileges);
						jsonMenu.put("priv_write", writePriv);
						jsonMenu.put("priv_create", writePriv && !MenuType.SUPERCLASS.equals(menuType));
						jsonMenu.put("superclass", menuEntryClass.isSuperClass());
					} catch (final Exception e) {
						// Who cares if it fails
					}
				}
				jsonMenu.put("type", menu.getCode().toLowerCase());
				jsonMenu.put("subtype", menu.getType().toLowerCase());
				jsonMenu.put("text", menu.getDescription());
			} else {
				jsonMenu.put("type", MenuCodeType.CLASS.getCodeType());
				jsonMenu.put("subtype", MenuCodeType.CLASS.getCodeType());
				jsonMenu.put("text", menu.getDescription());
			}
			if (menu.isReport()) {
				jsonMenu.put("objid", menu.getElementObjId());
			}

			if (menu.getElementClassId() != 0) {
				if (menu.isReport()) {
					/**
					 * must be unique - and for report ElementClassId is always
					 * "Report" and there are two ElementObjId for each report
					 */
					jsonMenu.put("id", menu.getElementObjId() + menu.getCode());
				} else {
					jsonMenu.put("id", menu.getElementClassId());
				}
			}
			if (!jsonMenu.has("id")) { // this should be for folders
				jsonMenu.put("id", menu.getId());
			}

			if (menu.getParentId() > 0) {
				jsonMenu.put("parent", menu.getParentId());
			}

			jsonMenu.put("cmIndex", menu.getNumber());
			jsonMenu.put("leaf", !isFolder);
			jsonMenu.put("selectable", !isFolder);
			jsonMenuList.put(jsonMenu);
		}

		return jsonMenuList;
	}

	public static MenuItem toServer(JSONObject jsonMenu) throws JSONException {
		final MenuItem item = singleToServer(jsonMenu);
		if (jsonMenu.has(CHILDREN)) {
			JSONArray children = jsonMenu.getJSONArray(CHILDREN);
			for (int i=0; i<children.length(); ++i) {
				JSONObject child = (JSONObject) children.get(i);
				item.addChild(toServer(child));
			}
		}

		return item;
	}

	private static MenuItem singleToServer(JSONObject jsonMenu) throws JSONException {
		final MenuItem item = new MenuItemDTO();
		final MenuItemType type = MenuStore.MenuItemType.getType(jsonMenu.getString(TYPE));
		item.setType(type);

		if (!MenuItemType.ROOT.equals(type)) {
			item.setDescription(jsonMenu.getString(DESCRIPTION));
			item.setIndex(jsonMenu.getInt(INDEX));
			item.setReferedClassName(jsonMenu.getString(CLASS_NAME));
			item.setReferencedElementId(getElementId(jsonMenu));
		}

		return item;
	}

	private static Long getElementId(JSONObject jsonMenu) throws JSONException {
		Long elementId = null;
		if (jsonMenu.has(ELEMENT_ID)) {
			String stringElementId = (String) jsonMenu.get(ELEMENT_ID);
			if (notEmpty(stringElementId)) {
				elementId = Long.valueOf(stringElementId);
			}
		}

		return elementId;
	}

	private static boolean notEmpty(String s) {
		return !"".equals(s);
	}
}

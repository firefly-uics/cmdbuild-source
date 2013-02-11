package org.cmdbuild.servlets.json.schema;

import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.wrappers.MenuCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.store.menu.MenuStore;
import org.cmdbuild.services.store.menu.MenuStore.MenuItem;
import org.cmdbuild.services.store.menu.OldDaoMenuStore;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.MenuSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModMenu extends JSONBase {

	/**
	 * 
	 * @param groupName
	 * @return The full menu configuration. All the MenuItems configured
	 * 		for the given group name.
	 * @throws JSONException
	 * @throws AuthException
	 * @throws NotFoundException
	 * @throws ORMException
	 */
	@Admin
	@JSONExported
	public JSONObject getMenuConfiguration( //
			@Parameter(PARAMETER_GROUP_NAME) final String groupName //
			) throws JSONException, AuthException, NotFoundException, ORMException {

		final MenuStore store = getStore();
		final MenuItem menu = store.read(groupName);
		final boolean withWrapper = true;
		return MenuSerializer.toClient(menu, withWrapper);
	}

	/**
	 * 
	 * @param groupName
	 * 		The group for which we want the items that could be added to the menu.
	 * 		This items are Classes, Processes, Reports and Dashboards
	 *
	 * @return the list of available items grouped by type
	 * @throws JSONException
	 */
	@Admin
	@JSONExported
	public JSONObject getAvailableMenuItems( //
			final ITableFactory tf, //
			@Parameter(PARAMETER_GROUP_NAME) final String groupName //
			) throws JSONException {

		final MenuStore store = getStore();
		final MenuItem availableMenu = store.getAvailableItems(groupName);
		final boolean withWrapper = true;
		return MenuSerializer.toClient(availableMenu, withWrapper);
	}

	/**
	 * 
	 * @param groupName the group name for which we want save the menu
	 * @param jsonMenuItems the list of menu items
	 * @throws Exception
	 */
	@Admin
	@JSONExported
	public void saveMenu( //
			@Parameter(PARAMETER_GROUP_NAME) final String groupName, //
			@Parameter(PARAMETER_MENU) final JSONObject jsonMenu //
			) throws Exception {

		final MenuStore store = getStore();
		final MenuItem menu = MenuSerializer.toServer(jsonMenu);
		store.save(groupName, menu);
	}

	/**
	 * 
	 * @param groupName the name of the group for which we want delete the menu
	 * @return
	 * @throws JSONException
	 */
	@Admin
	@JSONExported
	public void deleteMenu( //
			@Parameter(PARAMETER_GROUP_NAME) final String groupName //
			) throws JSONException {

		final MenuStore store = getStore();
		store.delete(groupName);
	}

	/**
	 * 
	 * @param groupName
	 * @return the menu defined for the given group. If there are no menu
	 * 		for this group, it returns the DefaultMenu (if exists).
	 * 		Note that this method has to remove, eventually, the nodes
	 * 		that point to something that the user has not the privileges to manage
	 * @throws JSONException
	 * @throws AuthException
	 * @throws NotFoundException
	 * @throws ORMException
	 */
	@JSONExported
	public JSONObject getAssignedMenu( //
			@Parameter(PARAMETER_GROUP_NAME) final String groupName //
			) throws JSONException, AuthException, NotFoundException, ORMException {

		final MenuStore store = getStore();
		final MenuItem menu = store.getMenuToUseForGroup(groupName);
		final boolean withWrapper = true;
		return MenuSerializer.toClient(menu, withWrapper);

	}

	private MenuStore getStore() {
		return new OldDaoMenuStore();
	}
}
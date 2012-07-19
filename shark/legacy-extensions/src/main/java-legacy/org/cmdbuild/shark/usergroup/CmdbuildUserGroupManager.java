package org.cmdbuild.shark.usergroup;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.usergroup.UserGroupManager;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class CmdbuildUserGroupManager implements UserGroupManager {

	private static final String SHARK_ADMIN_USER = "admin";
	private static final String SHARK_ADMIN_PASSWORD = "enhydra";

	private static final String[] SYSTEM_ALL_USERS = { SHARK_ADMIN_USER };

	private static final String[] EMPTY_LIST = {};
	private static final String[] UNKNOWN_LIST = null;
	private static final String UNKNOWN = null;

	public void configure(CallbackUtilities cus) throws Exception {
	}

	// no subgroups
	public boolean doesGroupBelongToGroup(WMSessionHandle shandle, String groupName, String subgroupName)
			throws Exception {
		return false;
	}

	// every group is accepted
	public boolean doesGroupExist(WMSessionHandle shandle, String groupName) throws Exception {
		return true;
	}

	// every user belongs to every group
	public boolean doesUserBelongToGroup(WMSessionHandle shandle, String groupName, String username) throws Exception {
		return doesUserExist(shandle, username);
	}

	// system and admin users exist
	public boolean doesUserExist(WMSessionHandle shandle, String username) throws Exception {
		return SHARK_ADMIN_USER.equals(username);
	}

	// ???
	public String[] getAllGroupnames(WMSessionHandle shandle) throws Exception {
		return null;
	}

	public String[] getAllGroupnamesForUser(WMSessionHandle shandle, String userName) throws Exception {
		return UNKNOWN_LIST;
	}

	public String[] getAllImmediateSubgroupsForGroup(WMSessionHandle shandle, String groupName) throws Exception {
		return EMPTY_LIST;
	}

	public String[] getAllImmediateUsersForGroup(WMSessionHandle shandle, String groupName) throws Exception {
		return EMPTY_LIST;
	}

	public String[] getAllSubgroupsForGroups(WMSessionHandle shandle, String[] groupNames) throws Exception {
		return EMPTY_LIST;
	}

	public String[] getAllUsers(WMSessionHandle shandle) throws Exception {
		return SYSTEM_ALL_USERS;
	}

	public String[] getAllUsersForGroups(WMSessionHandle shandle, String[] groupNames) throws Exception {
		return SYSTEM_ALL_USERS;
	}

	// ???
	public String getGroupAttribute(WMSessionHandle shandle, String groupName, String attName) throws Exception {
		return UNKNOWN;
	}

	public String getGroupDescription(WMSessionHandle shandle, String groupName) throws Exception {
		return UNKNOWN;
	}

	public String[] getGroups(WMSessionHandle sessionHandle, String expression) throws Exception {
		return UNKNOWN_LIST;
	}

	public String[] getObjects(WMSessionHandle sessionHandle, String expression) throws Exception {
		return UNKNOWN_LIST;
	}

	public String getUserAttribute(WMSessionHandle shandle, String username, String attName) throws Exception {
		return UNKNOWN;
	}

	public String getUserEMailAddress(WMSessionHandle shandle, String username) throws Exception {
		return UNKNOWN;
	}

	public String getUserFirstName(WMSessionHandle shandle, String username) throws Exception {
		return UNKNOWN;
	}

	public String getUserLastName(WMSessionHandle shandle, String username) throws Exception {
		return UNKNOWN;
	}

	public String getUserPassword(WMSessionHandle shandle, String username) throws Exception {
		return UNKNOWN;
	}

	public String getUserRealName(WMSessionHandle shandle, String username) throws Exception {
		return UNKNOWN;
	}

	public boolean validateUser(String username, String pwd) throws Exception {
		if (username.equals(SHARK_ADMIN_USER) && pwd.equals(SHARK_ADMIN_PASSWORD)) {
			return true;
		} else {
			return false;
		}
	}

}

package org.cmdbuild.logic.auth;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.logic.Logic;

/**
 * Facade class for all the authentication operations
 */
public interface AuthenticationLogic extends Logic {

	interface Response {

		boolean isSuccess();

		String getReason();

		Collection<GroupInfo> getGroupsInfo();

	}

	/**
	 * A simple bean that contains informations for login menu (group list)
	 */
	interface GroupInfo {

		String getName();

		String getDescription();

		Long getId();

	}

	Response login(LoginDTO loginDTO);

	GroupInfo getGroupInfoForGroup(String groupName);

	List<CMUser> getUsersForGroupWithId(Long groupId);

	List<Long> getUserIdsForGroupWithId(Long groupId);

	Iterable<String> getGroupNamesForUserWithId(Long userId);

	Iterable<String> getGroupNamesForUserWithUsername(String loginString);

	CMUser getUserWithId(Long userId);

	CMUser createUser(UserDTO userDTO);

	CMUser updateUser(UserDTO userDTO);

	CMGroup getGroupWithId(Long groupId);

	CMGroup getGroupWithName(String groupName);

	CMGroup changeGroupStatusTo(Long groupId, boolean isActive);

	Iterable<CMGroup> getAllGroups();

	List<CMUser> getAllUsers();

	CMUser enableUserWithId(Long userId);

	CMUser disableUserWithId(Long userId);

	CMGroup createGroup(GroupDTO groupDTO);

	CMGroup updateGroup(GroupDTO groupDTO);

	CMGroup setGroupActive(Long groupId, boolean active);

	void addUserToGroup(Long userId, Long groupId);

	void removeUserFromGroup(Long userId, Long groupId);

}

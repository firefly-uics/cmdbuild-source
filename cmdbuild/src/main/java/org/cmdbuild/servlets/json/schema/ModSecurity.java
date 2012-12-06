package org.cmdbuild.servlets.json.schema;

import java.io.IOException;
import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.elements.wrappers.PrivilegeCard;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.GroupDTO;
import org.cmdbuild.logic.auth.GroupDTO.GroupDTOBuilder;
import org.cmdbuild.logic.auth.UserDTO;
import org.cmdbuild.logic.auth.UserDTO.UserDTOBuilder;
import org.cmdbuild.logic.privileges.PrivilegesLogic;
import org.cmdbuild.model.profile.UIConfiguration;
import org.cmdbuild.model.profile.UIConfigurationObjectMapper;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class ModSecurity extends JSONBase {

	private static final ObjectMapper mapper = new UIConfigurationObjectMapper();
	private AuthenticationLogic authLogic;
	private PrivilegesLogic privilegesLogic;

	@JSONExported
	public String getGroupList(final JSONObject serializer) throws JSONException, AuthException, ORMException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final Iterable<CMGroup> allGroups = authLogic.getAllGroups();
		final JSONArray groups = new JSONArray();
		for (final CMGroup group : allGroups) {
			final JSONObject jsonGroup = Serializer.serializeCMGroup(group);
			groups.put(jsonGroup);
		}
		serializer.put("groups", groups);
		return serializer.toString();
	}

	@JSONExported
	public JsonResponse getUIConfiguration(final UserContext userCtx) throws JSONException, AuthException, ORMException {
		return JsonResponse.success(userCtx.getDefaultGroup().getUIConfiguration());
	}

	@Admin
	@JSONExported
	public JsonResponse getGroupUIConfiguration(@Parameter("id") final int groupId, final UserContext userCtx)
			throws JSONException, AuthException, ORMException {

		final GroupCard group = GroupCard.getOrDie(groupId);
		return JsonResponse.success(group.getUIConfiguration());
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveGroupUIConfiguration(@Parameter("id") final int groupId,
			@Parameter("uiConfiguration") final String jsonUIConfiguration, final UserContext userCtx)
			throws JSONException, AuthException, JsonParseException, JsonMappingException, IOException {

		final GroupCard group = GroupCard.getOrDie(groupId);
		final UIConfiguration uiConfiguration = mapper.readValue(jsonUIConfiguration, UIConfiguration.class);

		group.setUIConfiguration(uiConfiguration);
		group.save();
	}

	@JSONExported
	public JSONObject getPrivilegeList(final JSONObject serializer, @Parameter("groupId") final Long groupId)
			throws JSONException, AuthException {
		// TODO: use new dao
		// privilegesLogic = applicationContext.getBean(PrivilegesLogic.class);
		// //TODO: modify the spring xml file
		// List<PrivilegePair> privilegeList = PrivilegeCard.forGroup(groupId);
		// serializer.put("rows",
		// Serializer.serializePrivilegeList(privilegeList, tf));
		final int i = 0;
		return serializer;
	}

	@JSONExported
	public JSONObject getUserList(final JSONObject serializer) throws JSONException, AuthException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final List<CMUser> usersList = authLogic.getAllUsers();
		serializer.put("rows", Serializer.serializeCMUserList(usersList));
		return serializer;
	}

	@JSONExported
	public JSONObject getUserGroupList(final JSONObject serializer, @Parameter(value = "userid") final Long userId)
			throws JSONException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final CMUser user = authLogic.getUserWithId(userId);
		final JSONArray jsonGroupList = Serializer.serializeGroupsForUser(user);
		serializer.put("result", jsonGroupList);
		return serializer;
	}

	@Admin
	@JSONExported
	public JSONObject getGroupUserList(@Parameter("groupId") final Long groupId,
			@Parameter("alreadyAssociated") final boolean associated, final JSONObject serializer) throws JSONException {
		/**
		 * TODO: improve performances: CMUser method getGroups must return a
		 * list of strings of group names
		 */
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final List<CMUser> associatedUsers = authLogic.getUsersForGroupWithId(groupId);
		if (!associated) {
			final List<CMUser> allUsers = authLogic.getAllUsers();
			final List<CMUser> notAssociatedUsers = Lists.newArrayList();
			for (final CMUser user : allUsers) {
				if (!associatedUsers.contains(user)) {
					notAssociatedUsers.add(user);
				}
			}
			return serializer.put("users", Serializer.serializeCMUserList(notAssociatedUsers));
		}
		return serializer.put("users", Serializer.serializeCMUserList(associatedUsers));
	}

	@JSONExported
	public void changePassword(final UserContext userCtx, @Parameter("newpassword") final String newPassword,
			@Parameter("oldpassword") final String oldPassword) {
		userCtx.changePassword(oldPassword, newPassword);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void savePrivilege(final JSONObject serializer, @Parameter("groupId") final int groupId,
			@Parameter("classid") final int grantedClassId, @Parameter("privilege_mode") final String privilegeMode,
			final ITableFactory tf) throws JSONException, AuthException {

		final PrivilegeCard privilege = PrivilegeCard.get(groupId, grantedClassId);

		if (privilegeMode.equals("write_privilege"))
			privilege.setMode(PrivilegeType.WRITE);
		else if (privilegeMode.equals("read_privilege"))
			privilege.setMode(PrivilegeType.READ);
		else
			privilege.setMode(PrivilegeType.NONE);

		privilege.save();
	}

	@Admin(AdminAccess.DEMOSAFE)
	@Transacted
	@JSONExported
	public JSONObject saveUser(final JSONObject serializer, @Parameter("userid") final Long userId,
			@Parameter(value = "description", required = false) final String description,
			@Parameter(value = "username", required = false) final String username,
			@Parameter(value = "password", required = false) final String password,
			@Parameter(value = "email", required = false) final String email,
			@Parameter("isactive") final boolean isActive, @Parameter("defaultgroup") final Long defaultGroupId)
			throws JSONException, AuthException {

		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final boolean newUser = userId <= -1;
		CMUser createdOrUpdatedUser = null;
		final UserDTOBuilder userDTOBuilder = UserDTO.newInstance() //
				.withDescription(description) //
				.withUsername(username) //
				.withPassword(password) //
				.withEmail(email) //
				.withDefaultGroupId(defaultGroupId) //
				.setActive(isActive);
		if (newUser) {
			final UserDTO userDTO = userDTOBuilder.build();
			createdOrUpdatedUser = authLogic.createUser(userDTO);
		} else {
			final UserDTO userDTO = userDTOBuilder.withUserId(userId).build();
			createdOrUpdatedUser = authLogic.updateUser(userDTO);
		}
		serializer.put("rows", Serializer.serializeCMUser(createdOrUpdatedUser));
		return serializer;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject disableUser(final JSONObject serializer, @Parameter("userid") final Long userId,
			@Parameter("disable") final boolean disable) throws JSONException, AuthException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		CMUser user;
		if (disable) {
			user = authLogic.disableUserWithId(userId);
		} else {
			user = authLogic.enableUserWithId(userId);
		}
		serializer.put("rows", Serializer.serializeCMUser(user));
		return serializer;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject saveGroup(final JSONObject serializer, @Parameter("id") final Long groupId,
			@Parameter(value = "name", required = false) final String name,
			@Parameter("description") final String description, @Parameter("email") final String email,
			@Parameter("startingClass") final Long startingClass, @Parameter("isActive") final boolean isActive,
			@Parameter("isAdministrator") final boolean isAdministrator,
			@Parameter(value = "users", required = false) final String users) throws JSONException, AuthException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final boolean newGroup = groupId <= -1;
		CMGroup createdOrUpdatedGroup = null;
		final GroupDTOBuilder builder = GroupDTO.newInstance() //
				.withName(name) //
				.withDescription(description) //
				.withAdminFlag(isAdministrator) //
				.withEmail(email) //
				.withStartingClassId(startingClass) //
				.setActive(isActive); // FIXME: when users (last parameter) is
		// not null?
		if (newGroup) {
			final GroupDTO groupDTO = builder.build();
			createdOrUpdatedGroup = authLogic.createGroup(groupDTO);
		} else {
			final GroupDTO groupDTO = builder.withGroupId(groupId).build();
			createdOrUpdatedGroup = authLogic.updateGroup(groupDTO);
		}
		serializer.put("group", Serializer.serializeCMGroup(createdOrUpdatedGroup));
		return serializer;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveGroupUserList(@Parameter(value = "users", required = false) final String users,
			@Parameter("groupId") final int groupId, final UserContext userCtx) {
		// final GroupCard group = GroupCard.getOrCreate(groupId);
		// final IDomain userGroupDomain =
		// UserOperations.from(UserContext.systemContext()).domains().get(authLogic.USER_GROUP_DOMAIN_NAME);
		//
		// final List<UserCard> oldUserList = authLogic.getUserList(groupId);
		// final List<String> newUserIdList = new ArrayList<String>();
		// if (users != null && !users.equals("")) {
		// StringTokenizer tokenizer = new StringTokenizer(users, ",");
		// while (tokenizer.hasMoreTokens()) {
		// newUserIdList.add(tokenizer.nextToken());
		// }
		// }
		//
		// for (UserCard user : oldUserList) {
		// String userId = ((Integer) user.getId()).toString();
		// if (newUserIdList.contains(userId)) {
		// newUserIdList.remove(userId);
		// } else {
		// IRelation relation =
		// UserOperations.from(UserContext.systemContext()).relations().get(userGroupDomain,
		// user, group);
		// relation.delete();
		// }
		// }
		//
		// //newUserIdList contains only the IDs of new group's users
		// for (String userId : newUserIdList) {
		// ICard userCard =
		// UserOperations.from(UserContext.systemContext()).tables().get(UserCard.USER_CLASS_NAME).cards().list().ignoreStatus().id(Integer.parseInt(userId)).get();
		// UserCard user = new UserCard(userCard);
		// IRelation relation =
		// UserOperations.from(UserContext.systemContext()).relations().create(userGroupDomain,
		// user, group);
		// relation.save();
		// }
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject enableDisableGroup(final JSONObject serializer, @Parameter("isActive") final boolean isActive,
			@Parameter("groupId") final int groupId) throws JSONException, AuthException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final CMGroup group = authLogic.changeGroupStatusTo(Long.valueOf(groupId), isActive);
		serializer.put("group", Serializer.serializeCMGroup(group));
		return serializer;
	}
}

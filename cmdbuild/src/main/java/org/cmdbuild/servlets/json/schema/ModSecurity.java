package org.cmdbuild.servlets.json.schema;

import java.io.IOException;
import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logic.DataAccessLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.GroupInfo;
import org.cmdbuild.logic.auth.GroupDTO;
import org.cmdbuild.logic.auth.GroupDTO.GroupDTOBuilder;
import org.cmdbuild.logic.auth.UserDTO;
import org.cmdbuild.logic.auth.UserDTO.UserDTOBuilder;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.logic.privileges.SecurityLogic.PrivilegeInfo;
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
	private SecurityLogic securityLogic;

	@JSONExported
	public String getGroupList(final JSONObject serializer) throws JSONException, AuthException, ORMException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final Iterable<CMGroup> allGroups = authLogic.getAllGroups();
		final JSONArray groups = new JSONArray();
		for (final CMGroup group : allGroups) {
			final JSONObject jsonGroup = Serializer.serialize(group);
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
	public JsonResponse getGroupUIConfiguration(@Parameter("id") final int groupId) throws JSONException,
			AuthException, ORMException {

		final GroupCard group = GroupCard.getOrDie(groupId);
		return JsonResponse.success(group.getUIConfiguration());
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveGroupUIConfiguration(@Parameter("id") final int groupId,
			@Parameter("uiConfiguration") final String jsonUIConfiguration) throws JSONException, AuthException,
			JsonParseException, JsonMappingException, IOException {

		final GroupCard group = GroupCard.getOrDie(groupId);
		final UIConfiguration uiConfiguration = mapper.readValue(jsonUIConfiguration, UIConfiguration.class);

		group.setUIConfiguration(uiConfiguration);
		group.save();
	}

	@JSONExported
	public JSONObject getPrivilegeList(final JSONObject serializer, @Parameter("groupId") final Long groupId)
			throws JSONException, AuthException {
		securityLogic = new SecurityLogic(TemporaryObjectsBeforeSpringDI.getSystemView());
		final List<PrivilegeInfo> groupPrivileges = securityLogic.getPrivilegesForGroup(groupId);
		serializer.put("row", Serializer.serializePrivilegeList(groupPrivileges));
		return serializer;
	}

	@JSONExported
	public JSONObject getUserList(final JSONObject serializer) throws JSONException, AuthException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final List<CMUser> usersList = authLogic.getAllUsers();
		serializer.put("rows", Serializer.serializeUsers(usersList));
		return serializer;
	}

	@JSONExported
	public JSONObject getUserGroupList(final JSONObject serializer, @Parameter(value = "userid") final Long userId)
			throws JSONException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final CMUser user = authLogic.getUserWithId(userId);
		final List<GroupInfo> groupsForLogin = Lists.newArrayList();
		for (final String name : user.getGroupNames()) {
			groupsForLogin.add(authLogic.getGroupInfoForGroup(name));
		}
		final JSONArray jsonGroupList = Serializer.serializeGroupsForUser(user, groupsForLogin);
		serializer.put("result", jsonGroupList);
		return serializer;
	}

	@Admin
	@JSONExported
	public JSONObject getGroupUserList(@Parameter("groupId") final Long groupId,
			@Parameter("alreadyAssociated") final boolean associated, final JSONObject serializer) throws JSONException {
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
			return serializer.put("users", Serializer.serializeUsers(notAssociatedUsers));
		}
		return serializer.put("users", Serializer.serializeUsers(associatedUsers));
	}

	@JSONExported
	public void changePassword(final UserContext userCtx, @Parameter("newpassword") final String newPassword,
			@Parameter("oldpassword") final String oldPassword) {
		userCtx.changePassword(oldPassword, newPassword);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void savePrivilege(final JSONObject serializer, @Parameter("groupId") final Long groupId,
			@Parameter("classid") final Long grantedClassId, @Parameter("privilege_mode") final String privilegeMode)
			throws JSONException, AuthException {
		securityLogic = new SecurityLogic(TemporaryObjectsBeforeSpringDI.getSystemView());
		final DataAccessLogic dal = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final CMClass grantedClass = dal.findClassById(grantedClassId);
		String mode = null;
		if (privilegeMode.equals("write_privilege")) {
			mode = "w";
		} else if (privilegeMode.equals("read_privilege")) {
			mode = "r";
		} else {
			mode = "-";
		}
		securityLogic.savePrivilege(new PrivilegeInfo(groupId, grantedClass, mode));
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
		serializer.put("rows", Serializer.serialize(createdOrUpdatedUser));
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
		serializer.put("rows", Serializer.serialize(user));
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
				.setActive(isActive);
		if (newGroup) {
			final GroupDTO groupDTO = builder.build();
			createdOrUpdatedGroup = authLogic.createGroup(groupDTO);
		} else {
			final GroupDTO groupDTO = builder.withGroupId(groupId).build();
			createdOrUpdatedGroup = authLogic.updateGroup(groupDTO);
		}
		serializer.put("group", Serializer.serialize(createdOrUpdatedGroup));
		return serializer;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject enableDisableGroup(final JSONObject serializer, @Parameter("isActive") final boolean isActive,
			@Parameter("groupId") final int groupId) throws JSONException, AuthException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final CMGroup group = authLogic.changeGroupStatusTo(Long.valueOf(groupId), isActive);
		serializer.put("group", Serializer.serialize(group));
		return serializer;
	}

	/**
	 * 
	 * @param users
	 *            a String of comma separeted user identifiers. These are the id
	 *            of the users that belong to the group with id = groupId
	 * @param groupId
	 */
	@Transacted
	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveGroupUserList(@Parameter(value = "users", required = false) final String users,
			@Parameter("groupId") final Long groupId, final UserContext userCtx) {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		final List<Long> newUserIds = Lists.newArrayList();
		if (!users.isEmpty()) {
			final String[] splittedUserIds = users.split(",");
			for (final String userId : splittedUserIds) {
				newUserIds.add(Long.valueOf(userId));
			}
		}
		final List<Long> oldUserIds = authLogic.getUserIdsForGroupWithId(groupId);
		for (final Long userId : newUserIds) {
			if (!oldUserIds.contains(userId)) {
				authLogic.addUserToGroup(userId, groupId);
			}
		}
		for (final Long userId : oldUserIds) {
			if (!newUserIds.contains(userId)) {
				authLogic.removeUserFromGroup(userId, groupId);
			}
		}
	}
	
	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject enableDisableGroup(
			JSONObject serializer,
			@Parameter("isActive") boolean isActive,
			@Parameter("groupId") int groupId,
			ITableFactory tf,
			UserContext userCtx
		) throws JSONException, AuthException {

		ICard card = tf.get(GroupCard.GROUP_CLASS_NAME).cards().list().ignoreStatus().id(groupId).get();
		GroupCard group = new GroupCard(card);

		// The CloudAdmin could not disable/enalbe groups with administrator
		// privileges if not its own group
		if (userCtx.getDefaultGroup().getUIConfiguration().isCloudAdmin()
				&& group.isAdmin()
				&& groupId != userCtx.getDefaultGroup().getId()) {

			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}

		setGroupStatus(group, isActive);
		group.save();
		serializer.put("group", Serializer.serializeGroupCard(group));
		return serializer;
	}

	private void setGroupStatus(GroupCard group, boolean isActive) {
		if (isActive) {
			group.setStatus(ElementStatus.ACTIVE);
		} else {
			group.setStatus(ElementStatus.INACTIVE);
		}	
	}

}

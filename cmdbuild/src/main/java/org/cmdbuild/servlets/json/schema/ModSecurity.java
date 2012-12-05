package org.cmdbuild.servlets.json.schema;

import java.io.IOException;
import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.UserImpl;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.elements.wrappers.PrivilegeCard;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.UserDTO;
import org.cmdbuild.logic.auth.UserDTO.UserDTOBuilder;
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

	@JSONExported
	public String getGroupList(JSONObject serializer) throws JSONException, AuthException, ORMException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		Iterable<CMGroup> allGroups = authLogic.getAllGroups();
		JSONArray groups = new JSONArray();
		for (CMGroup group : allGroups) {
			JSONObject jsonGroup = Serializer.serializeGroup(group);
			groups.put(jsonGroup);
		}
		serializer.put("groups", groups);
		return serializer.toString();
	}

	@JSONExported
	public JsonResponse getUIConfiguration(UserContext userCtx) throws JSONException, AuthException, ORMException {
		return JsonResponse.success(userCtx.getDefaultGroup().getUIConfiguration());
	}

	@Admin
	@JSONExported
	public JsonResponse getGroupUIConfiguration(@Parameter("id") int groupId, UserContext userCtx)
			throws JSONException, AuthException, ORMException {

		GroupCard group = GroupCard.getOrDie(groupId);
		return JsonResponse.success(group.getUIConfiguration());
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveGroupUIConfiguration(@Parameter("id") int groupId,
			@Parameter("uiConfiguration") String jsonUIConfiguration, UserContext userCtx) throws JSONException,
			AuthException, JsonParseException, JsonMappingException, IOException {

		final GroupCard group = GroupCard.getOrDie(groupId);
		final UIConfiguration uiConfiguration = mapper.readValue(jsonUIConfiguration, UIConfiguration.class);

		group.setUIConfiguration(uiConfiguration);
		group.save();
	}

	@JSONExported
	public JSONObject getPrivilegeList(JSONObject serializer, ITableFactory tf, @Parameter("groupId") int groupId)
			throws JSONException, AuthException {
		Iterable<PrivilegeCard> privilegeList = PrivilegeCard.forGroup(groupId);
		serializer.put("rows", Serializer.serializePrivilegeList(privilegeList, tf));

		return serializer;
	}

	@JSONExported
	public JSONObject getUserList(JSONObject serializer, ITableFactory tf) throws JSONException, AuthException {
		Iterable<ICard> userList = tf
				.get(UserCard.USER_CLASS_NAME)
				.cards()
				.list()
				.filter(ICard.CardAttributes.Status.name(), AttributeFilterType.DIFFERENT,
						ElementStatus.UPDATED.value()).order("Username", OrderFilterType.ASC).ignoreStatus();
		serializer.put("rows", Serializer.serializeUserList(userList));

		return serializer;
	}

	@JSONExported
	public JSONObject getUserGroupList(JSONObject serializer, @Parameter(value = "userid") Long userId)
			throws JSONException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		CMUser user = authLogic.getUserWithId(userId);
		JSONArray jsonGroupList = Serializer.serializeGroupsForUser(user);
		serializer.put("result", jsonGroupList);
		return serializer;
	}

	@Admin
	@JSONExported
	public JSONObject getGroupUserList(@Parameter("groupId") Long groupId,
			@Parameter("alreadyAssociated") boolean associated, JSONObject serializer) throws JSONException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		List<CMUser> associatedUsers = authLogic.getUsersForGroupWithId(groupId);
		if (!associated) {
			List<CMUser> allUsers = authLogic.getAllUsers();
			List<CMUser> notAssociatedUsers = Lists.newArrayList();
			for (CMUser user : allUsers) {
				if (!associatedUsers.contains(user)) {
					notAssociatedUsers.add(user);
				}
			}
			return serializer.put("users", Serializer.serializeCMUserList(notAssociatedUsers));
		}
		return serializer.put("users", Serializer.serializeCMUserList(associatedUsers));
	}

	@JSONExported
	public void changePassword(UserContext userCtx, @Parameter("newpassword") String newPassword,
			@Parameter("oldpassword") String oldPassword) {
		/**
		 * TODO: implement this method (implement the "modifyCard" method of
		 * DbDataView class) and don't use UserCtx
		 */
		userCtx.changePassword(oldPassword, newPassword);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void savePrivilege(JSONObject serializer, @Parameter("groupId") int groupId,
			@Parameter("classid") int grantedClassId, @Parameter("privilege_mode") String privilegeMode,
			ITableFactory tf) throws JSONException, AuthException {

		PrivilegeCard privilege = PrivilegeCard.get(groupId, grantedClassId);

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
	public JSONObject saveUser(JSONObject serializer, @Parameter("userid") Long userId,
			@Parameter(value = "description", required = false) String description,
			@Parameter(value = "username", required = false) String username,
			@Parameter(value = "password", required = false) String password,
			@Parameter(value = "email", required = false) String email, @Parameter("isactive") boolean isActive,
			@Parameter("defaultgroup") Long defaultGroupId) throws JSONException, AuthException {

		/**
		 * Note: if userId == -1, create a new user, otherwise modify it
		 */
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		boolean newUser = userId == -1;
		CMUser createdOrUpdatedUser = null;
		UserDTOBuilder userDTOBuilder = UserDTO.newInstance() //
				.withDescription(description) //
				.withUsername(username) //
				.withPassword(password) //
				.withEmail(email) //
				.withDefaultGroupId(defaultGroupId) //
				.setActive(isActive);
		if (newUser) {
			UserDTO userDTO = userDTOBuilder.build();
			createdOrUpdatedUser = authLogic.createUser(userDTO);
		} else {
			UserDTO userDTO = userDTOBuilder.withUserId(userId).build();
			createdOrUpdatedUser = authLogic.updateUser(userDTO);
		}
		serializer.put("rows", Serializer.serializeCMUser(createdOrUpdatedUser));
		return serializer;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject disableUser(JSONObject serializer, @Parameter("userid") int userId,
			@Parameter("disable") boolean disable, ITableFactory tf) throws JSONException, AuthException {

		ICard card = tf.get(UserCard.USER_CLASS_NAME).cards().list().ignoreStatus().id(userId).get();

		UserCard user = new UserCard(card);
		if (disable) {
			user.setStatus(ElementStatus.INACTIVE_USER);
		} else {
			user.setStatus(ElementStatus.ACTIVE);
		}
		user.save();

		serializer.put("rows", Serializer.serializeUser(user));
		return serializer;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject saveGroup(JSONObject serializer, @Parameter("id") int groupId,
			@Parameter(value = "name", required = false) String name, @Parameter("description") String description,
			@Parameter("email") String email, @Parameter("startingClass") int startingClass,
			@Parameter("isActive") boolean isActive, @Parameter("isAdministrator") boolean isAdministrator,
			@Parameter(value = "users", required = false) String users, UserContext userCtx) throws JSONException,
			AuthException {
		GroupCard group = GroupCard.getOrCreate(groupId);
		if (name != null) {
			group.setName(name);
		}
		group.setDescription(description);
		if (email != null) {
			group.setEmail(email);
		}
		group.setIsAdmin(isAdministrator);
		group.setStartingClass(startingClass);
		if (isActive) {
			group.setStatus(ElementStatus.ACTIVE);
		} else {
			group.setStatus(ElementStatus.INACTIVE);
		}

		group.save();
		serializer.put("group", Serializer.serializeGroupCard(group));
		return serializer;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveGroupUserList(@Parameter(value = "users", required = false) String users,
			@Parameter("groupId") int groupId, UserContext userCtx) {
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
	public JSONObject enableDisableGroup(JSONObject serializer, @Parameter("isActive") boolean isActive,
			@Parameter("groupId") int groupId) throws JSONException, AuthException {
		authLogic = applicationContext.getBean(AuthenticationLogic.class);
		CMGroup group = authLogic.changeGroupStatusTo(Long.valueOf(groupId), isActive);
		serializer.put("group", Serializer.serializeGroup(group));
		return serializer;
	}
}

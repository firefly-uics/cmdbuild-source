package org.cmdbuild.servlets.json.schema;

import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class ModSecurity extends JSONBase {

	@JSONExported
	public String getGroupList(final JSONObject serializer) throws JSONException {
		// Iterable<GroupCard> list = GroupCard.all();
		// JSONArray groups = new JSONArray();
		// for (GroupCard groupCard : list) {
		// JSONObject jsonGroup = Serializer.serializeGroupCard(groupCard);
		// groups.put(jsonGroup);
		// }
		// serializer.put("groups", groups);
		// return serializer.toString();
		throw new UnsupportedOperationException("Temporary disabled");
	}

	@JSONExported
	public JSONObject getPrivilegeList(final JSONObject serializer, final ITableFactory tf,
			@Parameter("groupId") final int groupId) throws JSONException {
		// Iterable<PrivilegeCard> privilegeList =
		// PrivilegeCard.forGroup(groupId);
		// serializer.put("rows",
		// Serializer.serializePrivilegeList(privilegeList, tf));
		// return serializer;
		throw new UnsupportedOperationException("Temporary disabled");
	}

	@JSONExported
	public JSONObject getUserList(final JSONObject serializer, final ITableFactory tf) throws JSONException,
			AuthException {
		// Iterable<ICard>
		// userList=tf.get(UserCard.USER_CLASS_NAME).cards().list().filter(ICard.CardAttributes.Status.name(),
		// AttributeFilterType.DIFFERENT,ElementStatus.UPDATED.value()).order("Username",OrderFilterType.ASC).ignoreStatus();
		// serializer.put("rows", Serializer.serializeUserList(userList));
		// return serializer;
		throw new UnsupportedOperationException("Temporary disabled");
	}

	@JSONExported
	public JSONObject getUserGroupList(final JSONObject serializer, @Parameter("userid") final int userId)
			throws JSONException {
		// Iterable<Group> groupList =
		// AuthenticationFacade.getGroupListForUser(userId);
		// JSONArray jsonGroupList = new JSONArray();
		// for (Group g : groupList) {
		// jsonGroupList.put(Serializer.serializeGroup(g));
		// }
		// serializer.put("result", jsonGroupList);
		// return serializer;
		throw new UnsupportedOperationException("Temporary disabled");
	}

	@Admin
	@JSONExported
	public JSONObject getGroupUserList(@Parameter("groupId") final int groupId,
			@Parameter("alreadyAssociated") final boolean associated, final JSONObject serializer,
			final ITableFactory tf) throws JSONException {
		// Iterable<UserCard> userList;
		//
		// final Iterable<UserCard> associatedUserList =
		// AuthenticationFacade.getUserList(groupId);
		// if (associated) {
		// userList = associatedUserList;
		// } else {
		// // FIXME
		// // userList contains duplicate users! But distinct on ("User"."Id")
		// is not
		// // supported yet. To eliminate duplicate users, we use an awful
		// HashMap.
		// Set<Integer> associatedUserIds = new HashSet<Integer>();
		// for (UserCard associatedUserCard : associatedUserList) {
		// associatedUserIds.add(associatedUserCard.getId());
		// }
		//
		// final HashMap<Integer, UserCard> unassociatedUserMap = new
		// HashMap<Integer, UserCard>();
		// for (UserCard userCard : UserCard.allByUsername()) {
		// if (!associatedUserIds.contains(userCard.getId())) {
		// unassociatedUserMap.put(userCard.getId(), userCard);
		// }
		// }
		// userList = unassociatedUserMap.values();
		// }
		//
		// serializer.put("users", Serializer.serializeUserList(userList));
		// return serializer;
		throw new UnsupportedOperationException("Temporary disabled");
	}

	@JSONExported
	public void changePassword(final UserContext userCtx, @Parameter("newpassword") final String newPassword,
			@Parameter("oldpassword") final String oldPassword) {
		// userCtx.changePassword(oldPassword, newPassword);
		throw new UnsupportedOperationException("Temporary disabled");
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void savePrivilege(final JSONObject serializer, @Parameter("groupId") final int groupId,
			@Parameter("classid") final int grantedClassId, @Parameter("privilege_mode") final String privilegeMode,
			final ITableFactory tf) throws JSONException {
		// PrivilegeCard privilege = PrivilegeCard.get(groupId, grantedClassId);
		//
		// if (privilegeMode.equals("write_privilege"))
		// privilege.setMode(PrivilegeType.WRITE);
		// else if (privilegeMode.equals("read_privilege"))
		// privilege.setMode(PrivilegeType.READ);
		// else
		// privilege.setMode(PrivilegeType.NONE);
		//
		// privilege.save();
		throw new UnsupportedOperationException("Temporary disabled");
	}

	@Admin(AdminAccess.DEMOSAFE)
	@Transacted
	@JSONExported
	public JSONObject saveUser(final JSONObject serializer, @Parameter("userid") final int userId,
			@Parameter(value = "description", required = false) final String description,
			@Parameter(value = "username", required = false) final String username,
			@Parameter(value = "password", required = false) final String password,
			@Parameter(value = "email", required = false) final String email,
			@Parameter("isactive") final boolean isActive, @Parameter("defaultgroup") final int defaultGroupId,
			final ITableFactory tf) throws JSONException {
		// ICard card = null;
		// if (userId==-1) {
		// CardQuery cardQuery =
		// tf.get(UserCard.USER_CLASS_NAME).cards().list().ignoreStatus().filter(ICard.CardAttributes.Status.name(),
		// AttributeFilterType.DIFFERENT,ElementStatus.UPDATED.value()).filter("Username",
		// AttributeFilterType.EQUALS,username);
		// if (cardQuery.iterator().hasNext())
		// throw ORMExceptionType.ORM_DUPLICATE_USER.createException();
		// else
		// card= tf.get(UserCard.USER_CLASS_NAME).cards().create();
		// } else {
		// card=
		// tf.get(UserCard.USER_CLASS_NAME).cards().list().ignoreStatus().id(userId).get();
		// }
		// UserCard user = new UserCard(card);
		// if (username != null) {
		// user.setUsername(username);
		// }
		// if (description != null) {
		// user.setDescription(description);
		// }
		// if (email != null) {
		// user.setEmail(email);
		// }
		// if (password !=null && !password.equals("")) {
		// user.setUnencryptedPassword(password);
		// }
		// if (isActive) {
		// user.setStatus(ElementStatus.ACTIVE);
		// } else {
		// user.setStatus(ElementStatus.INACTIVE_USER);
		// }
		// user.save();
		//
		// AuthenticationFacade.setDefaultGroupForUser(user.getId(),
		// defaultGroupId);
		//
		// serializer.put("rows", Serializer.serializeUser(user));
		//
		// return serializer;
		throw new UnsupportedOperationException("Temporary disabled");
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject disableUser(final JSONObject serializer, @Parameter("userid") final int userId,
			@Parameter("disable") final boolean disable, final ITableFactory tf) throws JSONException {
		// ICard card =
		// tf.get(UserCard.USER_CLASS_NAME).cards().list().ignoreStatus().id(userId).get();
		//
		// UserCard user = new UserCard(card);
		// if (disable) {
		// user.setStatus(ElementStatus.INACTIVE_USER);
		// } else {
		// user.setStatus(ElementStatus.ACTIVE);
		// }
		// user.save();
		//
		// serializer.put("rows", Serializer.serializeUser(user));
		// return serializer;
		throw new UnsupportedOperationException("Temporary disabled");
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject saveGroup(final JSONObject serializer, @Parameter("id") final int groupId,
			@Parameter(value = "name", required = false) final String name,
			@Parameter("description") final String description, @Parameter("email") final String email,
			@Parameter("startingClass") final int startingClass, @Parameter("isActive") final boolean isActive,
			@Parameter("isAdministrator") final boolean isAdministrator,
			@Parameter(value = "users", required = false) final String users,
			@Parameter(value = "disabledModules", required = false) final String[] disabledModules,
			final UserContext userCtx) throws JSONException, AuthException {
		// GroupCard group = GroupCard.get(groupId, userCtx);
		// if (name != null) {
		// group.setName(name);
		// }
		// group.setDescription(description);
		// if (email != null) {
		// group.setEmail(email);
		// }
		// group.setIsAdmin(isAdministrator);
		// group.setStartingClass(startingClass);
		// if (isActive) {
		// group.setStatus(ElementStatus.ACTIVE);
		// } else {
		// group.setStatus(ElementStatus.INACTIVE);
		// }
		// group.setDisabledModules(disabledModules);
		// group.save();
		// serializer.put("group", Serializer.serializeGroupCard(group));
		// return serializer;
		throw new UnsupportedOperationException("Temporary disabled");
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveGroupUserList(@Parameter(value = "users", required = false) final String users,
			@Parameter("groupId") final int groupId, final UserContext userCtx) {
		// final GroupCard group = GroupCard.get(groupId, userCtx);
		// final IDomain userGroupDomain =
		// userCtx.domains().get(AuthenticationFacade.USER_GROUP_DOMAIN_NAME);
		//
		// final List<UserCard> oldUserList =
		// AuthenticationFacade.getUserList(groupId);
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
		// IRelation relation = userCtx.relations().get(userGroupDomain, user,
		// group);
		// relation.delete();
		// }
		// }
		//
		// //newUserIdList contains only the IDs of new group's users
		// for (String userId : newUserIdList) {
		// ICard userCard =
		// userCtx.tables().get(UserCard.USER_CLASS_NAME).cards().list().ignoreStatus().id(Integer.parseInt(userId)).get();
		// UserCard user = new UserCard(userCard);
		// IRelation relation = userCtx.relations().create(userGroupDomain,
		// user, group);
		// relation.save();
		// }
		throw new UnsupportedOperationException("Temporary disabled");
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject enableDisableGroup(final JSONObject serializer, @Parameter("isActive") final boolean isActive,
			@Parameter("groupId") final int groupId, final ITableFactory tf) throws JSONException {
		// ICard card =
		// tf.get(GroupCard.GROUP_CLASS_NAME).cards().list().ignoreStatus().id(groupId).get();
		// GroupCard group = new GroupCard(card);
		// setGroupStatus(group, isActive);
		// group.save();
		// serializer.put("group", Serializer.serializeGroupCard(group));
		// return serializer;
		throw new UnsupportedOperationException("Temporary disabled");
	}

	// private void setGroupStatus(GroupCard group, boolean isActive) {
	// if (isActive) {
	// group.setStatus(ElementStatus.ACTIVE);
	// } else {
	// group.setStatus(ElementStatus.INACTIVE);
	// }
	// }
}

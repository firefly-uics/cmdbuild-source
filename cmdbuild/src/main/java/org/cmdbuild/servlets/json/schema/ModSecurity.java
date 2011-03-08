package org.cmdbuild.servlets.json.schema;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.filters.AttributeFilter;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.elements.wrappers.PrivilegeCard;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.auth.AuthenticationFacade;
import org.cmdbuild.services.auth.Group;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModSecurity extends JSONBase {

	@JSONExported
	public String getGroupList(JSONObject serializer) throws JSONException,
			AuthException, ORMException {

		Iterable<GroupCard> list = GroupCard.all();
		JSONArray groups = new JSONArray();
		for (GroupCard groupCard : list) {
			JSONObject jsonGroup = Serializer.serializeGroupCard(groupCard);
			groups.put(jsonGroup);
		}
		serializer.put("groups", groups);
		return serializer.toString();
	}
	
	@JSONExported
	public JSONObject getGroup(
			JSONObject serializer,
			@Parameter("groupId") int groupId,
			ITableFactory tf
			) throws JSONException, AuthException {
			GroupCard role= new GroupCard (tf.get(GroupCard.GROUP_CLASS_NAME).cards().list().id(groupId).ignoreStatus().get());
			serializer.put("data", Serializer.serializeGroupCard(role));	
		return serializer;
	}
	
	@JSONExported
	public JSONObject getPrivilegeList(
			JSONObject serializer,
			ITableFactory tf,
			@Parameter("groupId") int groupId
			) throws JSONException, AuthException {
			Iterable<PrivilegeCard> privilegeList = PrivilegeCard.forGroup(groupId);
			serializer.put("rows", Serializer.serializePrivilegeList(privilegeList, tf));
	
		return serializer;
	}
	
	@JSONExported
	public JSONObject getUserList(
			JSONObject serializer,
			ITableFactory tf
			) throws JSONException, AuthException {
			Iterable<ICard> userList=tf.get(UserCard.USER_CLASS_NAME).cards().list().filter(ICard.CardAttributes.Status.name(), AttributeFilterType.DIFFERENT,ElementStatus.UPDATED.value()).order("Username",OrderFilterType.ASC).ignoreStatus();
			serializer.put("rows", Serializer.serializeUserList(userList));
	
		return serializer;
	}

	@JSONExported
	public JSONObject getUserGroupList(
			JSONObject serializer,
			@Parameter("userid") int userId
		) throws JSONException {
		Iterable<Group> groupList = AuthenticationFacade.getGroupListForUser(userId);
		JSONArray jsonGroupList = new JSONArray();
		boolean foundDefault = false;
		for (Group g : groupList) {
			jsonGroupList.put(Serializer.serializeGroup(g));
			foundDefault |= g.isDefault();
		}
		serializer.put("result", jsonGroupList);
		return serializer;
	}

	@JSONExported
	public JSONObject getGroupUserList(
			@Parameter("groupId") int groupId,
			@Parameter("alreadyAssociated") boolean associated,
			JSONObject serializer,
			ITableFactory tf
			) throws JSONException, AuthException {
			Iterable<UserCard> userList;
			AttributeFilter filterId = new AttributeFilter(UserContext.systemContext().tables().get(GroupCard.GROUP_CLASS_NAME).getAttribute(ICard.CardAttributes.Id.name()), AttributeFilterType.EQUALS, String.valueOf(groupId));
			ITable user = tf.get(UserCard.USER_CLASS_NAME);
			IAttribute attrStatus = user.getAttribute(CardAttributes.Status.name());
			AttributeFilter filterStatus = new AttributeFilter(attrStatus, AttributeFilterType.DIFFERENT, ElementStatus.UPDATED.value());
			userList= AuthenticationFacade.getUserList(filterStatus,filterId);
			//FIXME - userList contains duplicate users! But distinct on on ("User"."Id") is not supported yet
			// To eliminate duplicate users, I will use an awful HashMap.
			if(!associated){
				CardQuery list=tf.get(UserCard.USER_CLASS_NAME).cards().list().filter(ICard.CardAttributes.Status.name(), AttributeFilterType.DIFFERENT,ElementStatus.UPDATED.value()).order("Username",OrderFilterType.ASC).ignoreStatus();	
				HashMap<Integer, UserCard> map = new HashMap<Integer, UserCard>();
				LinkedList<Integer> idUserList = new LinkedList<Integer>();
				for(UserCard u: userList){idUserList.add(u.getId());}
				for(ICard card :list){
					if(idUserList.contains(card.getId()))
						continue;
					UserCard u = new UserCard(card);
					map.put(u.getId(), u);
				}
				userList=map.values();			
			}
			//end fixme
			serializer.put("users", Serializer.serializeUserList(userList));
		return serializer;
	}
	
	@JSONExported
	public void changePassword(
		UserContext userCtx,
		@Parameter("newpassword") String newPassword,
		@Parameter("oldpassword") String oldPassword) {
		userCtx.changePassword(oldPassword, newPassword);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void savePrivilege(JSONObject serializer,
			@Parameter("groupId") int groupId,
			@Parameter("classid") int grantedClassId,
			@Parameter("privilege_mode") String privilegeMode, ITableFactory tf)
			throws JSONException, AuthException {

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
	public JSONObject saveUser(
			JSONObject serializer,
			@Parameter("userid") int userId,
			@Parameter(value="description", required=false) String description,
			@Parameter(value="username", required=false) String username,
			@Parameter(value="password", required=false) String password,
			@Parameter(value="email", required=false) String email,
			@Parameter("isactive") boolean isActive,
			@Parameter("defaultgroup") int defaultGroupId,
			ITableFactory tf
		) throws JSONException, AuthException {
		ICard card = null;
		if (userId==-1) {
			CardQuery cardQuery = tf.get(UserCard.USER_CLASS_NAME).cards().list().ignoreStatus().filter(ICard.CardAttributes.Status.name(), AttributeFilterType.DIFFERENT,ElementStatus.UPDATED.value()).filter("Username", AttributeFilterType.EQUALS,username);
			if (cardQuery.iterator().hasNext())
				throw ORMExceptionType.ORM_DUPLICATE_USER.createException();
			else
				card= tf.get(UserCard.USER_CLASS_NAME).cards().create();		
		} else {
			card= tf.get(UserCard.USER_CLASS_NAME).cards().list().ignoreStatus().id(userId).get();
		}
		UserCard user = new UserCard(card);
		if (username != null) {
			user.setUsername(username);
		}
		if (description != null) {
			user.setDescription(description);
		}
		if (email != null) {
			user.setEmail(email);
		}
		if (password !=null && !password.equals("")) {
			user.setUnencryptedPassword(password);
		}
		if (isActive) {
			user.setStatus(ElementStatus.ACTIVE);
		} else {
			user.setStatus(ElementStatus.INACTIVE_USER);
		}
		user.save();

		AuthenticationFacade.setDefaultGroupForUser(user.getId(), defaultGroupId);

		serializer.put("rows", Serializer.serializeUser(user));

		return serializer;
	}
	
	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject disableUser(
			JSONObject serializer,
			@Parameter("userid") int userId,
			@Parameter("disable") boolean disable,
			ITableFactory tf
		) throws JSONException, AuthException {
		
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
	public JSONObject saveGroup(
			JSONObject serializer,
			@Parameter("id") int groupId,
			@Parameter(value="name", required=false) String name,
			@Parameter("description") String description,
			@Parameter("email") String email,
			@Parameter("startingClass") int startingClass,
			@Parameter("isActive") boolean isActive,
			@Parameter("isAdministrator") boolean isAdministrator,
			@Parameter(value="users", required=false) String users,
			@Parameter(value="disabledModules", required=false) String[] disabledModules,
			UserContext userCtx
		) throws JSONException, AuthException {
		GroupCard group = getGroupById(groupId, userCtx);
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
		group.setDisabledModules(disabledModules);
		group.save();
		serializer.put("group", Serializer.serializeGroupCard(group));
		return serializer;
	}

	private GroupCard getGroupById(int groupId, UserContext userCtx) {
		ICard card;
		if (groupId > 0) {
			card = userCtx.tables().get(GroupCard.GROUP_CLASS_NAME).cards().list().ignoreStatus().id(groupId).get();
		} else {
			card = userCtx.tables().get(GroupCard.GROUP_CLASS_NAME).cards().create();
		}
		GroupCard group = new GroupCard(card);
		return group;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveGroupUserList(
			@Parameter(value="users", required=false) String users,
			@Parameter("groupId") int groupId,
			UserContext userCtx) {
		GroupCard group = getGroupById(groupId, userCtx);
		//get old users
		AttributeValue attrValueId = group.getAttributeValue(CardAttributes.Id.name());
		AttributeFilter filterId = new AttributeFilter(attrValueId.getSchema(), AttributeFilterType.EQUALS, attrValueId.toString());
		
		ITable userClass = userCtx.tables().get(UserCard.USER_CLASS_NAME);
		IAttribute attrStatus = userClass.getAttribute(CardAttributes.Status.name());
		AttributeFilter filterStatus = new AttributeFilter(attrStatus, AttributeFilterType.DIFFERENT, ElementStatus.UPDATED.value());
		
		IDomain userGroupDomain = userCtx.domains().get(AuthenticationFacade.USER_GROUP_DOMAIN_NAME);
		List<UserCard> oldUserList = AuthenticationFacade.getUserList(filterStatus,filterId);

		LinkedList<String> newUserIdList = new LinkedList<String>();
		if (users != null && !users.equals("")) {
			StringTokenizer tokenizer = new StringTokenizer(users, ",");
			while (tokenizer.hasMoreTokens()) {
				newUserIdList.add(tokenizer.nextToken());
			}
		}

		for (UserCard user : oldUserList) {
			String userId = ((Integer) user.getId()).toString();
			if (newUserIdList.contains(userId)) {
				newUserIdList.remove(userId);
			} else {
				IRelation relation = userCtx.relations().get(userGroupDomain, user, group);
				relation.delete();
			}
		}

		//newUserIdList contains only the IDs of new group's users		
		for (String userId : newUserIdList) {
			ICard userCard = userCtx.tables().get(UserCard.USER_CLASS_NAME).cards().list().ignoreStatus().id(Integer.parseInt(userId)).get();
			UserCard user = new UserCard(userCard);
			IRelation relation = userCtx.relations().create(userGroupDomain, user, group);
			relation.save();
		}
	}
	
	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject enableDisableGroup(
			JSONObject serializer,
			@Parameter("isActive") boolean isActive,
			@Parameter("groupId") int groupId,
			ITableFactory tf
		) throws JSONException, AuthException {
		ICard card = tf.get(GroupCard.GROUP_CLASS_NAME).cards().list().ignoreStatus().id(groupId).get();
		GroupCard group = new GroupCard(card);
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

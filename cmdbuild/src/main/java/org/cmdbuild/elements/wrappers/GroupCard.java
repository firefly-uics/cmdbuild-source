package org.cmdbuild.elements.wrappers;

import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.dao.type.StringArray;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.auth.Group;
import org.cmdbuild.services.auth.GroupImpl;
import org.cmdbuild.services.auth.UserContext;

public class GroupCard extends LazyCard {

	private static final long serialVersionUID = 1L;

	public static final String GROUP_CLASS_NAME = "Role";
	public static final String GROUP_NAME_ATTRIBUTE = ICard.CardAttributes.Code.toString();
	public static final String GROUP_DESCRIPTION_ATTRIBUTE = ICard.CardAttributes.Description.toString();

	public static final String GROUP_ATTRIBUTE_DISABLEDMODULES = "DisabledModules";
	public static final String GROUP_ATTRIBUTE_EMAIL = "Email";
	public static final String GROUP_ATTRIBUTE_ISADMIN = "Administrator";
	public static final String GROUP_ATTRIBUTE_STARTINGCLASS = "startingClass";

	private static final ITable roleClass = UserContext.systemContext().tables().get(GROUP_CLASS_NAME);

	public GroupCard() throws NotFoundException {
		super(roleClass.cards().create());
	}

	// Should not be public but this class moved where it is used
	public GroupCard(ICard card) throws NotFoundException {
		super(card);
	}

	public Group toGroup(boolean defaultGroup) {
		return new GroupImpl(this.getId(), this.getName(), this.getDescription(),
				this.isAdmin(), this.getStartingClassOrDefault(), defaultGroup, this.getDisabledModules());
	}
	
	public String getName() {
		return getCode();
	}

	public void setName(String name) {
		if (isNew()) {
			setCode(name);
		} else {
			throw ORMExceptionType.ORM_ERROR_CARD_UPDATE.createException();
		}
	}

	public String getEmail(){
		return getAttributeValue(GROUP_ATTRIBUTE_EMAIL).getString();
	}
	
	public void setEmail(String email){
		getAttributeValue(GROUP_ATTRIBUTE_EMAIL).setValue(email);
	}

	public String[] getDisabledModules() {
		StringArray sa = (StringArray) getValue(GROUP_ATTRIBUTE_DISABLEDMODULES);
		if (sa != null) {
			return sa.getValue();
		} else {
			return null;
		}
	}

	public void setDisabledModules(String[] modules) {		
		setValue(GROUP_ATTRIBUTE_DISABLEDMODULES, modules);
	}
	
	public boolean isAdmin(){
		return getAttributeValue(GROUP_ATTRIBUTE_ISADMIN).getBoolean();
	}

	/*
	 * returns null on no class set
	 */
	public ITable getStartingClassOrDefault() {
		AttributeValue startingClassValue = getAttributeValue(GROUP_ATTRIBUTE_STARTINGCLASS);
		try {
			if (startingClassValue.isNull() || startingClassValue.getInt() <= 0) {
				String startingClassName = CmdbuildProperties.getInstance().getStartingClassName();
				if (startingClassName != null)
					return UserContext.systemContext().tables().get(startingClassName);
			} else {
				Integer startingClassId = this.getStartingClassId();
				if (startingClassId != null)
					return UserContext.systemContext().tables().get(startingClassId);
			}
		} catch (Exception e) {
		}
		return null;
	}

	public Integer getStartingClassId(){
		if(!getAttributeValue(GROUP_ATTRIBUTE_STARTINGCLASS).isNull())
			return getAttributeValue(GROUP_ATTRIBUTE_STARTINGCLASS).getInt(); //TODO
		return null;
	}

	public void setStartingClass(Integer startingClass){
		getAttributeValue(GROUP_ATTRIBUTE_STARTINGCLASS).setValue(startingClass);
	}

	public void setIsAdmin(Boolean isAdmin){
		getAttributeValue(GROUP_ATTRIBUTE_ISADMIN).setValue(isAdmin);
	}

	public static Iterable<GroupCard> allActive() throws NotFoundException, ORMException {
		List<GroupCard> list = new LinkedList<GroupCard>();
		for(ICard card : roleClass.cards().list())
			list.add(new GroupCard(card));
		return list;
	}

	public static Iterable<GroupCard> all() throws NotFoundException, ORMException {
		List<GroupCard> list = new LinkedList<GroupCard>();
		for(ICard card : roleClass.cards().list().ignoreStatus())
			list.add(new GroupCard(card));
		return list;
	}

	public static GroupCard get(int groupId, UserContext userCtx) {
		ICard card;
		if (groupId > 0) {
			card = userCtx.tables().get(GroupCard.GROUP_CLASS_NAME).cards().list().ignoreStatus().id(groupId).get();
		} else {
			card = userCtx.tables().get(GroupCard.GROUP_CLASS_NAME).cards().create();
		}
		GroupCard group = new GroupCard(card);
		return group;
	}
}

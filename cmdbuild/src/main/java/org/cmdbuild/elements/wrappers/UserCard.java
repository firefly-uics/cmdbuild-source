package org.cmdbuild.elements.wrappers;

import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.auth.User;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserImpl;
import org.cmdbuild.utils.SecurityEncrypter;

public class UserCard extends LazyCard implements User {

	private static final long serialVersionUID = 1L;

	public static final String USER_CLASS_NAME = "User";
	private static final ITable userClass = UserContext.systemContext().tables().get(USER_CLASS_NAME);

	public UserCard() throws NotFoundException {
		super(userClass.cards().create());
	}

	// Should not be public but this class moved where it is used
	public UserCard(ICard card) throws NotFoundException {
		super(card);
	}

	public User toUser() {
		return new UserImpl(this.getId(), this.getName(), this.getDescription(), this.getEncryptedPassword());
	}

	public String getName(){
		return getAttributeValue("Username").getString();
	}
	
	public void setUsername(String userName){
		getAttributeValue("Username").setValue(userName);
	}

	public String getEmail(){
		return getAttributeValue("Email").getString();
	}
	
	public void setEmail(String email){
		getAttributeValue("Email").setValue(email);
	}

	public String getEncryptedPassword(){
		return getAttributeValue("Password").getString();
	}

	public void setUnencryptedPassword(String password){
		SecurityEncrypter sd = new SecurityEncrypter();
		getAttributeValue("Password").setValue(sd.encrypt(password));
	}

	public static User findByUserName(String userName) {
		if (UserImpl.SYSTEM_USERNAME.equals(userName)) {
			return UserImpl.getSystemUser();
		}
		return getUserCardByName(userName).toUser();
	}

	public static UserCard getUserCardByName(String userName) {
		ICard card = userClass.cards().list().filter("Username", AttributeFilterType.EQUALS, userName).get(false);
		return new UserCard(card);
	}

	public static Iterable<UserCard> all() throws NotFoundException, ORMException {
		List<UserCard> list = new LinkedList<UserCard>();
		for(ICard card : userClass.cards().list())
			list.add(new UserCard(card));
		return list;
	}
}

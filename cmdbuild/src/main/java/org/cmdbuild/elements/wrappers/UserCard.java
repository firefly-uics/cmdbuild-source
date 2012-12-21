package org.cmdbuild.elements.wrappers;

import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.common.digest.Base64Digester;
import org.cmdbuild.common.digest.Digester;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.CardFactory;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.auth.AuthInfo;
import org.cmdbuild.services.auth.User;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserImpl;

public class UserCard extends LazyCard implements User {

	private static final long serialVersionUID = 1L;

	private static final String ATTRIBUTE_USERNAME = "Username";
	private static final String ATTRIBUTE_PASSWORD = "Password";
	private static final String ATTRIBUTE_EMAIL = "Email";

	public static final String USER_CLASS_NAME = "User";
	private static final ITable userClass = UserContext.systemContext().tables().get(USER_CLASS_NAME);

	public UserCard() throws NotFoundException {
		super(userClass.cards().create());
	}

	// Should not be public but this class moved where it is used
	public UserCard(final ICard card) throws NotFoundException {
		super(card);
	}

	public User toUser() {
		return new UserImpl(this.getId(), this.getName(), this.getDescription(), this.getEncryptedPassword());
	}

	@Override
	public String getName() {
		return getAttributeValue(ATTRIBUTE_USERNAME).getString();
	}

	public void setUsername(final String username) {
		getAttributeValue(ATTRIBUTE_USERNAME).setValue(username);
	}

	public String getEmail() {
		return getAttributeValue(ATTRIBUTE_EMAIL).getString();
	}

	public void setEmail(final String email) {
		getAttributeValue(ATTRIBUTE_EMAIL).setValue(email);
	}

	@Override
	public String getEncryptedPassword() {
		return getAttributeValue(ATTRIBUTE_PASSWORD).getString();
	}

	public void setUnencryptedPassword(final String password) {
		final Digester digester = new Base64Digester();
		getAttributeValue(ATTRIBUTE_PASSWORD).setValue(digester.encrypt(password));
	}

	public static User getUser(final String login) {
		return getUser(login, false);
	}

	public static User getUser(final AuthInfo authInfo) {
		final String authusername;
		final boolean elevatePrivileges;
		if (authInfo.isPrivilegedServiceUser() && authInfo.hasServiceUser()) {
			authusername = authInfo.getUsername();
			elevatePrivileges = true;
		} else {
			authusername = authInfo.getUsernameForAuthentication();
			elevatePrivileges = false;
		}
		return getUser(authusername, elevatePrivileges);
	}

	public static User getUser(final String login, final boolean elevatePrivileges) {
		final User user;
		if (elevatePrivileges) {
			user = UserImpl.getElevatedPrivilegesUser(login);
		} else if (UserImpl.SYSTEM_USER_USERNAME.equals(login)) {
			user = UserImpl.getSystemUser();
		} else {
			user = getUserCard(login).toUser();
		}
		return user;
	}

	public static UserCard getUserCard(final String login) {
		try {
			final CardFactory cardFactory = userClass.cards();
			final CardQuery cardQuery = cardFactory.list();
			final String attributeName = login.contains("@") ? ATTRIBUTE_EMAIL : ATTRIBUTE_USERNAME;
			final CardQuery filteredCardQuery = cardQuery.filter(attributeName, AttributeFilterType.EQUALS, login);
			final ICard card = filteredCardQuery.get(false);
			return new UserCard(card);
		} catch (final CMDBException e) {
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
		}
	}

	public static Iterable<UserCard> all() throws NotFoundException, ORMException {
		final List<UserCard> list = new LinkedList<UserCard>();
		for (final ICard card : userClass.cards().list()) {
			list.add(new UserCard(card));
		}
		return list;
	}

	public static Iterable<UserCard> allByUsername() throws NotFoundException, ORMException {
		final List<UserCard> list = new LinkedList<UserCard>();
		final Iterable<ICard> query = userClass
				.cards()
				.list()
				.filter(ICard.CardAttributes.Status.name(), AttributeFilterType.DIFFERENT,
						ElementStatus.UPDATED.value()).order("Username", OrderFilterType.ASC).ignoreStatus();
		for (final ICard card : query) {
			list.add(new UserCard(card));
		}
		return list;
	}
}

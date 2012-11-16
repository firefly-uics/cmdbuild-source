package utils;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cmdbuild.auth.password.NaivePasswordHandler;
import org.cmdbuild.auth.password.PasswordHandler;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Class containing some utility methods to create/delete cards, relations and
 * query them
 */
public abstract class DBFixture extends IntegrationTestBase {

	protected static final String USER_CLASS = "User";
	protected static final String ROLE_CLASS = "Role";
	protected static final String USER_ROLE_DOMAIN = "UserRole";
	protected static final String USERNAME_ATTRIBUTE = "Username";
	protected static final String PASSWORD_ATTRIBUTE = "Password";
	protected static final String EMAIL_ATTRIBUTE = "Email";
	protected static final PasswordHandler digester = new NaivePasswordHandler();
	protected List<Long> insertedGroupIds = Lists.newArrayList();
	protected List<Long> insertedUserIds = Lists.newArrayList();
	protected Map<Long, List<Long>> userIdToGroupIds = Maps.newHashMap();

	protected static String uniqueUUID() {
		return UUID.randomUUID().toString();
	}

	protected DBCard insertCardWithCode(final DBClass c, final Object value) {
		return insertCard(c, c.getCodeAttributeName(), value);
	}

	protected DBCard insertCard(final DBClass c, final String key, final Object value) {
		return DBCard.newInstance(rollbackDriver, c).set(key, value).save();
	}

	protected void insertCards(final DBClass c, final int quantity) {
		for (long i = 0; i < quantity; ++i) {
			insertCardWithCode(c, String.valueOf(i));
		}
	}

	protected void insertCardsWithCodeAndDescription(final DBClass c, final int quantity) {
		for (long i = 0; i < quantity; ++i) {
			DBCard.newInstance(rollbackDriver, c) //
					.setCode(String.valueOf(i)) //
					.setDescription(String.valueOf(i)) //
					.save();
		}
	}

	protected DBRelation insertRelation(final DBDomain d, final DBCard c1, final DBCard c2) {
		return DBRelation.newInstance(rollbackDriver, d) //
				.setCard1(c1) //
				.setCard2(c2) //
				.save();
	}

	protected void deleteCard(final DBCard c) {
		deleteEntry(c);
	}

	protected void deleteRelation(final DBRelation r) {
		deleteEntry(r);
	}

	protected void deleteEntry(final DBEntry e) {
		rollbackDriver.delete(e);
	}

	protected Iterable<String> namesOf(final Iterable<? extends CMEntryType> entityTypes) {
		return Iterables.transform(entityTypes, new Function<CMEntryType, String>() {

			@Override
			public String apply(final CMEntryType input) {
				return input.getName();
			}

		});
	}

	protected QueryAliasAttribute keyAttribute(final CMEntryType et) {
		return attribute(et, et.getKeyAttributeName());
	}

	protected QueryAliasAttribute codeAttribute(final CMClass c) {
		return attribute(c, c.getCodeAttributeName());
	}

	protected QueryAliasAttribute codeAttribute(final Alias alias, final CMClass c) {
		return attribute(alias, c.getCodeAttributeName());
	}

	protected QueryAliasAttribute descriptionAttribute(final CMClass c) {
		return attribute(c, c.getDescriptionAttributeName());
	}

	protected DBCard insertUserWithUsernameAndPassword(final String username, final String password) {
		final DBClass users = rollbackDriver.findClassByName(USER_CLASS);
		final DBCard user = DBCard.newInstance(rollbackDriver, users);
		final DBCard insertedUser = user.set(USERNAME_ATTRIBUTE, username) //
				.set(PASSWORD_ATTRIBUTE, digester.encrypt(password)) //
				.set(EMAIL_ATTRIBUTE, username + "@tecnoteca.com").save();
		insertedUserIds.add(insertedUser.getId());
		return insertedUser;
	}

	protected DBCard insertRoleWithCode(final String code) {
		final DBClass roles = rollbackDriver.findClassByName(ROLE_CLASS);
		final DBCard group = DBCard.newInstance(rollbackDriver, roles);
		final DBCard insertedGroup = (DBCard) group.setCode(code).save();
		insertedGroupIds.add(insertedGroup.getId());
		return insertedGroup;
	}

	protected DBRelation insertBindingBetweenUserAndRole(final DBCard user, final DBCard role) {
		final DBDomain userRoleDomain = rollbackDriver.findDomainByName(USER_ROLE_DOMAIN);
		final DBRelation relation = DBRelation.newInstance(rollbackDriver, userRoleDomain);
		relation.setCard1(user);
		relation.setCard2(role);

		List<Long> groupIds = userIdToGroupIds.get(user.getId());
		if (groupIds == null) {
			groupIds = Lists.newArrayList();
		}
		groupIds.add(role.getId());
		userIdToGroupIds.put(user.getId(), groupIds);

		return relation.save();

	}

}

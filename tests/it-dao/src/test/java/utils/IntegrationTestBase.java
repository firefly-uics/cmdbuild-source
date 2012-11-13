package utils;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.apache.commons.configuration.ConfigurationException;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.DBDataView;
import org.junit.After;
import org.junit.BeforeClass;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Subclasses using the @RunWith(value = Parameterized.class) annotation will
 * run the tests on every database driver. Otherwise a driver must be specified
 * in the (empty) constructor.
 */
public abstract class IntegrationTestBase {

	protected final GenericRollbackDriver rollbackDriver;
	protected final DBDataView view;

	protected IntegrationTestBase() {
		final DBDriver pgDriver = DBInitializer.getDBDriver();
		this.rollbackDriver = new GenericRollbackDriver(pgDriver);
		this.view = new DBDataView(rollbackDriver);
	}

	@BeforeClass
	public static void init() {
		try {
			DBInitializer.initDatabase();
		} catch (final ConfigurationException e) {
			fail("Exception while reading database configuration properties file");
		}
	}

	@After
	public void rollback() {
		rollbackDriver.rollback();
	}

	/*
	 * Utility methods
	 */

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

}

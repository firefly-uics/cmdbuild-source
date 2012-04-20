package integration.driver;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.view.DBDataView;
import org.junit.After;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import utils.GenericRollbackDriver;

/**
 * Subclasses using the @RunWith(value = Parameterized.class) annotation
 * will run the tests on every database driver. Otherwise a driver must
 * be specified in the (empty) constructor.
 */
public class DriverFixture {

	protected static ApplicationContext context;

	static {
		context = new ClassPathXmlApplicationContext("structure-test-context.xml");
	}

	@Parameters
	public static Collection<Object[]> data() {
		final Collection<Object[]> params = new ArrayList<Object[]>();
		for (final String name : context.getBeanNamesForType(PostgresDriver.class)) {
			final Object[] o = { name };
			params.add(o);
		}
		return params;
	}

	protected final GenericRollbackDriver driver;
	protected final DBDataView view;

	protected DriverFixture(final String driverBeanName) {
		final DBDriver driverToBeTested = context.getBean(driverBeanName, DBDriver.class);
		this.driver = new GenericRollbackDriver(driverToBeTested);
		this.view = new DBDataView(driver);
	}

	@After
	public void rollback() {
		driver.rollback();
	}

	/*
	 * Utility methods
	 */

	protected DBCard insertCardWithCode(final DBClass c, final Object value) {
		return insertCard(c, c.getCodeAttributeName(), value);
	}

	protected DBCard insertCard(final DBClass c, final String key, final Object value) {
		return DBCard.newInstance(driver, c).set(key, value).save();
	}

	protected void insertCards(final DBClass c, final int quantity) {
		for (long i = 0; i < quantity; ++i) {
			DBCard.newInstance(driver, c).setCode(String.valueOf(i)).save();
		}
	}

	protected DBRelation insertRelation(DBDomain d, DBCard c1, DBCard c2) {
		return DBRelation.newInstance(driver, d).setCard1(c1).setCard2(c2).save();
	}

	protected void deleteCard(DBCard c) {
		deleteEntry(c);
	}

	protected void deleteRelation(DBRelation r) {
		deleteEntry(r);
	}

	protected void deleteEntry(DBEntry e) {
		driver.delete(e);
	}



	protected final Iterable<String> names(final Iterable<? extends CMEntryType> entityTypes) {
		final Collection<String> names = new HashSet<String>();
		for (CMEntryType et : entityTypes) {
			names.add(et.getName());
		}
		return names;
	}

	protected final QueryAliasAttribute keyAttribute(CMEntryType et) {
		return attribute(et, et.getKeyAttributeName());
	}

	protected final QueryAliasAttribute codeAttribute(CMClass c) {
		return attribute(c, c.getCodeAttributeName());
	}

	protected final QueryAliasAttribute descriptionAttribute(CMClass c) {
		return attribute(c, c.getKeyAttributeName());
	}
}

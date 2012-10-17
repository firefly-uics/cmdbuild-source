package integration.driver;

import java.util.ArrayList;
import java.util.Collection;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.view.DBDataView;
import org.junit.After;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import utils.GenericRollbackDriver;

public class QueryTestFixture {

	protected static ApplicationContext context;

	// FIXME CREATE NEW ATTRIBUTES!
	protected static final String ATTRIBUTE_1 = org.cmdbuild.dao.driver.postgres.Const.CODE_ATTRIBUTE;
	protected static final String ATTRIBUTE_2 = org.cmdbuild.dao.driver.postgres.Const.DESCRIPTION_ATTRIBUTE;
	// FIXME SHOULD BE DYNAMIC
	protected static final String ID_ATTRIBUTE = org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;

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

	public QueryTestFixture(final String driverBeanName) {
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

	protected DBCard insertCard(final DBClass c, final String key, final Object value) {
		return DBCard.newInstance(driver, c).set(key, value).save();
	}

	protected void insertCards(final DBClass c, final int quantity) {
		for (long i = 0; i < quantity; ++i) {
			DBCard.newInstance(driver, c).set(ATTRIBUTE_1, String.valueOf(i)).save();
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

}

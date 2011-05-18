package integration.driver;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecs;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import utils.GenericRollbackDriver;
import utils.QuerySpecsTestDouble;

@RunWith(value = Parameterized.class)
public class QueryTest {

	private static ApplicationContext context;

	// FIXME CREATE NEW ATTRIBUTES!
	private static String ATTRIBUTE_1 = org.cmdbuild.dao.driver.postgres.Utils.CODE_ATTRIBUTE;
	private static String ATTRIBUTE_2 = org.cmdbuild.dao.driver.postgres.Utils.DESCRIPTION_ATTRIBUTE;
	// FIXME SHOULD BE DYNAMIC
	private static String ID_ATTRIBUTE = org.cmdbuild.dao.driver.postgres.Utils.ID_ATTRIBUTE;

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

	private final GenericRollbackDriver driver;

	public QueryTest(final String driverBeanName) {
		final DBDriver driverToBeTested = context.getBean(driverBeanName, DBDriver.class);
		this.driver = new GenericRollbackDriver(driverToBeTested);
	}

	@After
	public void rollback() {
		driver.rollback();
	}

	/*
	 * Tests
	 */

	@Test
	public void simpleSubclassQuery() {
		final DBClass newClass = driver.createClass("A", null);
		final Object attr1Value = "Pizza";
		final Object attr2Value = "Calzone";

		DBCard.create(driver, newClass)
			.set(ATTRIBUTE_1, attr1Value)
			.set(ATTRIBUTE_2, attr2Value)
			.save();

		final QuerySpecs query = new QuerySpecsTestDouble();
		query.setFrom(newClass);
		query.addSelectAttribute(newClass.getAttribute(ID_ATTRIBUTE));
		query.addSelectAttribute(newClass.getAttribute(ATTRIBUTE_1));
		final CMQueryResult result = driver.query(query);

		// TEST ATTRIBUTE_1 is extracted but not ATTRIBUTE_2
		final CMQueryRow firstRow = result.iterator().next();
		assertThat(result.size(), is(1));
		assertThat(firstRow.getCard(newClass).get(ATTRIBUTE_1), is(attr1Value));
		try {
			firstRow.getCard(newClass).get(ATTRIBUTE_2);
			fail("Value for attribute " + ATTRIBUTE_2 + " should have not been loaded");
		} catch (final UnsupportedOperationException e) {
			assertThat(e.getMessage(), is("Not implemented"));
		}
	}

	@Test
	public void simpleSuperclassQuery() {
		// given
		final DBClass S1 = driver.createClass("S1", null);
		final DBClass S2 = driver.createClass("S2", S1);
		final DBClass A = driver.createClass("A", S2);
		final DBClass B = driver.createClass("B", S1);
		final DBClass C = driver.createClass("C", S1);
		insertCard(A, ATTRIBUTE_1, "A");
		insertCard(B, ATTRIBUTE_1, "B");
		insertCard(C, ATTRIBUTE_1, "C");
		// when
		final QuerySpecs query = new QuerySpecsTestDouble();
		query.setFrom(S1);
		query.addSelectAttribute(S1.getAttribute(ID_ATTRIBUTE));
		query.addSelectAttribute(S1.getAttribute(ATTRIBUTE_1));
		final CMQueryResult result = driver.query(query);
		// then
		assertThat(result.size(), is(3));
		for (CMQueryRow row : result) {
			final CMCard c = row.getCard(S1);
			// the value was intentionally set to the class name
			final String expectedClassName = (String) c.get(ATTRIBUTE_1);
			assertThat(c.getType().getName(), is(expectedClassName));
		}
	}

	@Test
	public void simpleCountedQuery() {
		// given
		final int TOTAL_SIZE = 400000;
		final int OFFSET = 2000;
		final int LIMIT = 100;
//		final DBClass newClass = driver.createClass("Huge", null);
//		insertCards(newClass, TOTAL_SIZE);
		final DBClass newClass = driver.findClassByName("Huge");
		// when
		final QuerySpecs query = new QuerySpecsTestDouble();
		query.setFrom(newClass);
		query.addSelectAttribute(newClass.getAttribute(ID_ATTRIBUTE));
		query.addSelectAttribute(newClass.getAttribute(ATTRIBUTE_1));
		query.setOffset(OFFSET);
		query.setLimit(LIMIT);
		final CMQueryResult result = driver.query(query);
		// then
		assertThat(result.size(), is(LIMIT));
		assertThat(result.totalSize(), is(TOTAL_SIZE));
	}

	/*
	 * Utility methods
	 */

	private void insertCard(final DBClass c, final String key, final Object value) {
		DBCard.create(driver, c).set(key, value).save();
	}

//	private void insertCards(final DBClass c, final int quantity) {
//		for (long i = 0; i < quantity; ++i) {
//			DBCard.create(driver, c).set(ATTRIBUTE_1, String.valueOf(i)).save();
//		}
//	}
}

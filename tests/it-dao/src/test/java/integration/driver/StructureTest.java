package integration.driver;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import utils.GenericRollbackDriver;

@RunWith(value = Parameterized.class)
public class StructureTest {

	private static final String A_NEW_CLASS_NAME = "ANewClass";

	private static ApplicationContext appContext;

	static {
		appContext = new ClassPathXmlApplicationContext("structure-test-context.xml");
	}

	@Parameters
	public static Collection<Object[]> data() {
		final Collection<Object[]> params = new ArrayList<Object[]>();
		for (final String name : appContext.getBeanNamesForType(DBDriver.class)) {
			final Object[] o = { name };
			params.add(o);
		}
		return params;
	}

	private final GenericRollbackDriver driver;
	private final DBDriver driverToBeTested;

	public StructureTest(final String driverBeanName) {
		driverToBeTested = appContext.getBean(driverBeanName, DBDriver.class);
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
	public void theBaseClassIsAlwaysThereForPostgresDriver() {
		// FIXME
		if (driverToBeTested instanceof PostgresDriver) {
			// There are a dozen classes in the empty database...
			// At least this comes in handy for the tests!
			final Collection<DBClass> allClasses = driver.findAllClasses();
			assertTrue(allClasses.size() > 0);
			assertThat(names(allClasses), hasItem(DBDriver.BASE_CLASS_NAME));
		}
	}

	@Test
	public void classesCanBeAdded() {
		Collection<DBClass> allClasses;

		allClasses = driver.findAllClasses();
		final int initialSize = allClasses.size();
		assertThat(names(allClasses), not(hasItem(A_NEW_CLASS_NAME)));

		final DBClass newClass = driver.createClass(A_NEW_CLASS_NAME, null);

		assertThat(newClass.getName(), is(A_NEW_CLASS_NAME));

		allClasses = driver.findAllClasses();
		assertThat(allClasses.size(), is(initialSize + 1));
		assertThat(names(allClasses), hasItem(A_NEW_CLASS_NAME));
	}

	@Test
	public void classesCanBeCreatedHierarchically() {
		DBClass superClass = driver.createClass("S", null);
		DBClass subClassA = driver.createClass("A", superClass);
		DBClass subClassB = driver.createClass("B", superClass);

		assertThat(names(superClass.getChildren()), hasItem("A"));
		assertThat(names(superClass.getChildren()), hasItem("B"));
		assertThat(subClassA.getParent().getName(), is("S"));
		assertThat(subClassB.getParent().getName(), is("S"));

		// reload classes
		superClass = driver.findClassById(superClass.getId());
		subClassA = driver.findClassById(subClassA.getId());
		subClassB = driver.findClassById(subClassB.getId());

		assertThat(names(superClass.getChildren()), hasItem("A"));
		assertThat(names(superClass.getChildren()), hasItem("B"));
		assertThat(subClassA.getParent().getName(), is("S"));
		assertThat(subClassB.getParent().getName(), is("S"));
	}

	@Test
	public void classDeletionUpdatesTheHierarchy() {
		// given
		DBClass superClass = driver.createClass("S", null);
		DBClass subClass = driver.createClass("A", superClass);

		// when
		driver.deleteClass(subClass);

		// then
		assertThat(names(superClass.getChildren()), not(hasItem("A")));
//		assertThat(subClass.getId(), is(nullValue()));

		// reload classes
		superClass = driver.findClassById(superClass.getId());
		assertThat(names(superClass.getChildren()), not(hasItem("A")));
	}

	/*
	 * Utility methods
	 */

	private Iterable<String> names(final Iterable<? extends CMEntryType> entityTypes) {
		final Collection<String> names = new HashSet<String>();
		for (CMEntryType et : entityTypes) {
			names.add(et.getName());
		}
		return names;
	}
}

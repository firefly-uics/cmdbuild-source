package integration.driver;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;

import java.util.Collection;

import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class StructureTest extends DriverFixture {

	private static final String A_NEW_CLASS_NAME = "ANewClass";

	public StructureTest(final String driverBeanName) {
		super(driverBeanName);
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
}

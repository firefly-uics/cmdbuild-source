package integration.driver;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class StructureTest extends DriverFixture {

	private static final String A_NEW_CLASS_NAME = uniqueUUID();
	private static final String A_NEW_SUPERCLASS_NAME = uniqueUUID();
	private static final String A_NEW_SUBCLASS_NAME = uniqueUUID();
	private static final String ANOTHER_NEW_SUBCLASS_NAME = uniqueUUID();

	public StructureTest(final String driverBeanName) {
		super(driverBeanName);
	}

	@Test
	public void noTestClassesShouldBeDefinedAsDefault() {
		final Collection<DBClass> allClasses = driver.findAllClasses();
		assertThat(namesOf(allClasses), not(hasItem(A_NEW_CLASS_NAME)));
		assertThat(namesOf(allClasses), not(hasItem(A_NEW_SUPERCLASS_NAME)));
		assertThat(namesOf(allClasses), not(hasItem(A_NEW_SUBCLASS_NAME)));
		assertThat(namesOf(allClasses), not(hasItem(ANOTHER_NEW_SUBCLASS_NAME)));
	}

	@Test
	public void singleClassCanBeAdded() {
		final int actualClassesCount = driver.findAllClasses().size();

		DBClass newClass = driver.createClass(A_NEW_CLASS_NAME, null);

		assertThat(newClass.getName(), equalTo(A_NEW_CLASS_NAME));

		final Collection<DBClass> allClasses = driver.findAllClasses();
		assertThat(allClasses.size(), equalTo(actualClassesCount + 1));
		assertThat(namesOf(allClasses), hasItem(A_NEW_CLASS_NAME));

		newClass = driver.findClassByName(A_NEW_CLASS_NAME);
		assertThat(newClass, not(is(nullValue())));
	}

	@Test
	public void singleSuperClassCanBeAdded() {
		DBClass newClass = driver.createSuperClass(A_NEW_CLASS_NAME, null);

		assertThat(newClass.getName(), equalTo(A_NEW_CLASS_NAME));
		assertThat(newClass.isSuperclass(), is(true));

		final Collection<DBClass> allClasses = driver.findAllClasses();
		assertThat(namesOf(allClasses), hasItem(A_NEW_CLASS_NAME));

		newClass = driver.findClassByName(A_NEW_CLASS_NAME);
		assertThat(newClass, not(is(nullValue())));
		assertThat(newClass.getName(), equalTo(A_NEW_CLASS_NAME));
		assertThat(newClass.isSuperclass(), is(true));
	}

	@Test
	public void classesCanBeCreatedHierarchically() {
		DBClass superClass = driver.createSuperClass(A_NEW_SUPERCLASS_NAME, null);
		DBClass subClassA = driver.createClass(A_NEW_SUBCLASS_NAME, superClass);
		DBClass subClassB = driver.createClass(ANOTHER_NEW_SUBCLASS_NAME, superClass);

		assertThatHierarchyIsCorrect(superClass, subClassA, subClassB);

		// reload classes
		superClass = driver.findClassById(superClass.getId());
		subClassA = driver.findClassById(subClassA.getId());
		subClassB = driver.findClassById(subClassB.getId());

		assertThatHierarchyIsCorrect(superClass, subClassA, subClassB);
	}

	private void assertThatHierarchyIsCorrect(final DBClass superClass, final DBClass subClassA, final DBClass subClassB) {
		assertThat(superClass.isSuperclass(), is(true));
		assertThat(superClass.isAncestorOf(subClassA), is(true));
		assertThat(superClass.isAncestorOf(subClassB), is(true));
		assertThat(namesOf(superClass.getChildren()), hasItem(A_NEW_SUBCLASS_NAME));
		assertThat(namesOf(superClass.getChildren()), hasItem(ANOTHER_NEW_SUBCLASS_NAME));
		assertThat(subClassA.getParent().getName(), equalTo(A_NEW_SUPERCLASS_NAME));
		assertThat(subClassB.getParent().getName(), equalTo(A_NEW_SUPERCLASS_NAME));
	}

	@Test
	public void classDeletionUpdatesTheHierarchy() {
		DBClass superClass = driver.createClass(A_NEW_SUPERCLASS_NAME, null);
		final DBClass subClass = driver.createClass(A_NEW_SUBCLASS_NAME, superClass);

		driver.deleteClass(subClass);

		assertThat(namesOf(superClass.getChildren()), not(hasItem(A_NEW_SUPERCLASS_NAME)));
		// assertThat(subClass.getId(), is(nullValue()));

		// reload classes
		superClass = driver.findClassById(superClass.getId());
		assertThat(namesOf(superClass.getChildren()), not(hasItem(A_NEW_SUPERCLASS_NAME)));
	}

}

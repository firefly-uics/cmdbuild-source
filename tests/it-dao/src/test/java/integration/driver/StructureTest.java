package integration.driver;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@RunWith(value = Parameterized.class)
public class StructureTest extends DriverFixture {

	private static final String A_NEW_CLASS_NAME = uniqueUUID();
	private static final String A_NEW_SUPERCLASS_NAME = uniqueUUID();
	private static final String A_NEW_SUBCLASS_NAME = uniqueUUID();
	private static final String ANOTHER_NEW_SUBCLASS_NAME = uniqueUUID();
	private static final String LEAF_CLASS_NAME = uniqueUUID();
	private static final String ANOTHER_LEAF_CLASS_NAME = uniqueUUID();
	private static final String NOT_EXISTING_ATTRIBUTE = uniqueUUID();

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

	@Test(expected = Exception.class)
	public void shouldThrowExceptionCreatingClassesWithSameName() {
		driver.createClass(A_NEW_CLASS_NAME, null);
		driver.createClass(A_NEW_CLASS_NAME, null);
	}

	@Test
	public void shouldReturnNullIfNotExistingAttribute() {
		final DBClass newClass = driver.createClass(A_NEW_CLASS_NAME, null);
		final DBAttribute attribute = newClass.getAttribute(NOT_EXISTING_ATTRIBUTE);
		assertThat(attribute, nullValue());
	}

	@Test
	public void shouldRetrieveAllDefaultUserAttributes() {
		final DBClass newClass = driver.createClass(A_NEW_CLASS_NAME, null);
		final DBAttribute codeAttribute = newClass.getAttribute("Code");
		final DBAttribute descriptionAttribute = newClass.getAttribute("Description");
		final DBAttribute notesAttribute = newClass.getAttribute("Notes");
		assertThat(codeAttribute, notNullValue());
		assertThat(descriptionAttribute, notNullValue());
		assertThat(notesAttribute, notNullValue());
	}

	@Test
	public void shouldRetrieveAllLeavesClassesFromRoot() {
		// given
		final DBClass superClass = driver.createSuperClass(A_NEW_SUPERCLASS_NAME, null);
		final DBClass subClassA = driver.createSuperClass(A_NEW_SUBCLASS_NAME, superClass);
		final DBClass subClassB = driver.createSuperClass(ANOTHER_NEW_SUBCLASS_NAME, superClass);
		final DBClass leafA = driver.createClass(LEAF_CLASS_NAME, subClassA);
		final DBClass leafB = driver.createClass(ANOTHER_LEAF_CLASS_NAME, subClassA);

		// when
		final Iterable<DBClass> items = superClass.getLeaves();
		final List<DBClass> leaves = Lists.newArrayList();
		Iterables.addAll(leaves, items);

		// then
		assertThat(leaves.size(), is(equalTo(2)));
		assertThat(leaves, hasItems(leafA, leafB));
	}

	@Test
	public void shouldRetrieveAllLeavesClassesFromGenericSuperclass() {
		// given
		final DBClass superClass = driver.createClass(A_NEW_SUPERCLASS_NAME, null);
		final DBClass subClassA = driver.createSuperClass(A_NEW_SUBCLASS_NAME, superClass);
		final DBClass subClassB = driver.createSuperClass(ANOTHER_NEW_SUBCLASS_NAME, superClass);
		final DBClass leafA = driver.createClass(LEAF_CLASS_NAME, subClassA);
		final DBClass leafB = driver.createClass(ANOTHER_LEAF_CLASS_NAME, subClassA);

		// when
		final Iterable<DBClass> items = subClassA.getLeaves();
		final List<DBClass> leaves = Lists.newArrayList();
		Iterables.addAll(leaves, items);

		// then
		assertThat(leaves.size(), is(equalTo(2)));
		assertThat(leaves, hasItems(leafA, leafB));
	}

	@Test
	public void superClassShouldNotBeALeaf() {
		// given
		final DBClass superClass = driver.createSuperClass(A_NEW_SUPERCLASS_NAME, null);

		// when
		final Iterable<DBClass> items = superClass.getLeaves();
		final List<DBClass> leaves = Lists.newArrayList();
		Iterables.addAll(leaves, items);

		// then
		assertThat(leaves.size(), is(equalTo(0)));
	}

	@Test
	public void simpleClassShouldBeALeaf() {
		// given
		final DBClass simpleClass = driver.createClass(LEAF_CLASS_NAME, null);

		// when
		final Iterable<DBClass> items = simpleClass.getLeaves();
		final List<DBClass> leaves = Lists.newArrayList();
		Iterables.addAll(leaves, items);

		// then
		assertThat(leaves.size(), is(equalTo(1)));
		assertThat(leaves, hasItems(simpleClass));
	}

	@Test
	public void shouldReturnTrueIfSuperclass() {
		// given
		final DBClass superClass = driver.createSuperClass(A_NEW_SUPERCLASS_NAME, null);
		final DBClass leafClass = driver.createClass(LEAF_CLASS_NAME, superClass);

		// then
		assertThat(superClass.isSuperclass(), is(equalTo(true)));
		assertThat(leafClass.isSuperclass(), is(equalTo(false)));
	}

}

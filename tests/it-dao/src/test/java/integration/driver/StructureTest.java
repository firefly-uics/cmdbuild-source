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

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Ignore;
import org.junit.Test;

import utils.DBFixture;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class StructureTest extends DBFixture {

	private static final String A_NEW_CLASS_NAME = uniqueUUID();
	private static final String A_NEW_SUPERCLASS_NAME = uniqueUUID();
	private static final String A_NEW_SUBCLASS_NAME = uniqueUUID();
	private static final String ANOTHER_NEW_SUBCLASS_NAME = uniqueUUID();
	private static final String LEAF_CLASS_NAME = uniqueUUID();
	private static final String ANOTHER_LEAF_CLASS_NAME = uniqueUUID();
	private static final String NOT_EXISTING_ATTRIBUTE = uniqueUUID();

	@Test
	public void noTestClassesShouldBeDefinedAsDefault() {
		final Collection<DBClass> allClasses = rollbackDriver.findAllClasses();
		assertThat(namesOf(allClasses), not(hasItem(A_NEW_CLASS_NAME)));
		assertThat(namesOf(allClasses), not(hasItem(A_NEW_SUPERCLASS_NAME)));
		assertThat(namesOf(allClasses), not(hasItem(A_NEW_SUBCLASS_NAME)));
		assertThat(namesOf(allClasses), not(hasItem(ANOTHER_NEW_SUBCLASS_NAME)));
	}

	@Test
	public void singleClassCanBeAdded() {
		final int actualClassesCount = rollbackDriver.findAllClasses().size();

		DBClass newClass = rollbackDriver.createClass(A_NEW_CLASS_NAME, null);

		assertThat(newClass.getName(), equalTo(A_NEW_CLASS_NAME));

		final Collection<DBClass> allClasses = rollbackDriver.findAllClasses();
		assertThat(allClasses.size(), equalTo(actualClassesCount + 1));
		assertThat(namesOf(allClasses), hasItem(A_NEW_CLASS_NAME));

		newClass = rollbackDriver.findClassByName(A_NEW_CLASS_NAME);
		assertThat(newClass, not(is(nullValue())));
	}

	@Test
	public void singleSuperClassCanBeAdded() {
		DBClass newClass = rollbackDriver.createSuperClass(A_NEW_CLASS_NAME, null);

		assertThat(newClass.getName(), equalTo(A_NEW_CLASS_NAME));
		assertThat(newClass.isSuperclass(), is(true));

		final Collection<DBClass> allClasses = rollbackDriver.findAllClasses();
		assertThat(namesOf(allClasses), hasItem(A_NEW_CLASS_NAME));

		newClass = rollbackDriver.findClassByName(A_NEW_CLASS_NAME);
		assertThat(newClass, not(is(nullValue())));
		assertThat(newClass.getName(), equalTo(A_NEW_CLASS_NAME));
		assertThat(newClass.isSuperclass(), is(true));
	}

	@Test
	public void classesCanBeCreatedHierarchically() {
		DBClass superClass = rollbackDriver.createSuperClass(A_NEW_SUPERCLASS_NAME, null);
		DBClass subClassA = rollbackDriver.createClass(A_NEW_SUBCLASS_NAME, superClass);
		DBClass subClassB = rollbackDriver.createClass(ANOTHER_NEW_SUBCLASS_NAME, superClass);

		assertThatHierarchyIsCorrect(superClass, subClassA, subClassB);

		// reload classes
		superClass = rollbackDriver.findClassById(superClass.getId());
		subClassA = rollbackDriver.findClassById(subClassA.getId());
		subClassB = rollbackDriver.findClassById(subClassB.getId());

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
		DBClass superClass = rollbackDriver.createClass(A_NEW_SUPERCLASS_NAME, null);
		final DBClass subClass = rollbackDriver.createClass(A_NEW_SUBCLASS_NAME, superClass);

		rollbackDriver.deleteClass(subClass);

		assertThat(namesOf(superClass.getChildren()), not(hasItem(A_NEW_SUPERCLASS_NAME)));
		// assertThat(subClass.getId(), is(nullValue()));

		// reload classes
		superClass = rollbackDriver.findClassById(superClass.getId());
		assertThat(namesOf(superClass.getChildren()), not(hasItem(A_NEW_SUPERCLASS_NAME)));
	}

	@Test(expected = Exception.class)
	public void shouldThrowExceptionCreatingClassesWithSameName() {
		rollbackDriver.createClass(A_NEW_CLASS_NAME, null);
		rollbackDriver.createClass(A_NEW_CLASS_NAME, null);
	}

	@Test
	public void shouldReturnNullIfNotExistingAttribute() {
		final DBClass newClass = rollbackDriver.createClass(A_NEW_CLASS_NAME, null);
		final DBAttribute attribute = newClass.getAttribute(NOT_EXISTING_ATTRIBUTE);
		assertThat(attribute, nullValue());
	}

	@Test
	public void shouldRetrieveAllDefaultUserAttributes() {
		final DBClass newClass = rollbackDriver.createClass(A_NEW_CLASS_NAME, null);
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
		final DBClass superClass = rollbackDriver.createSuperClass(A_NEW_SUPERCLASS_NAME, null);
		final DBClass subClassA = rollbackDriver.createSuperClass(A_NEW_SUBCLASS_NAME, superClass);
		final DBClass subClassB = rollbackDriver.createSuperClass(ANOTHER_NEW_SUBCLASS_NAME, superClass);
		final DBClass leafA = rollbackDriver.createClass(LEAF_CLASS_NAME, subClassA);
		final DBClass leafB = rollbackDriver.createClass(ANOTHER_LEAF_CLASS_NAME, subClassA);

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
		final DBClass superClass = rollbackDriver.createClass(A_NEW_SUPERCLASS_NAME, null);
		final DBClass subClassA = rollbackDriver.createSuperClass(A_NEW_SUBCLASS_NAME, superClass);
		final DBClass subClassB = rollbackDriver.createSuperClass(ANOTHER_NEW_SUBCLASS_NAME, superClass);
		final DBClass leafA = rollbackDriver.createClass(LEAF_CLASS_NAME, subClassA);
		final DBClass leafB = rollbackDriver.createClass(ANOTHER_LEAF_CLASS_NAME, subClassA);

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
		final DBClass superClass = rollbackDriver.createSuperClass(A_NEW_SUPERCLASS_NAME, null);

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
		final DBClass simpleClass = rollbackDriver.createClass(LEAF_CLASS_NAME, null);

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
		final DBClass superClass = rollbackDriver.createSuperClass(A_NEW_SUPERCLASS_NAME, null);
		final DBClass leafClass = rollbackDriver.createClass(LEAF_CLASS_NAME, superClass);

		// then
		assertThat(superClass.isSuperclass(), is(equalTo(true)));
		assertThat(leafClass.isSuperclass(), is(equalTo(false)));
	}

	@Ignore
	@Test
	public void reservedAttributeAreNotFetched() {
		final DBClass grants = rollbackDriver.findClassByName("Grant");
		final CMAttribute att = grants.getAttribute("IdGrantedClass");
		assertThat(att, is(equalTo(null)));
	}

}

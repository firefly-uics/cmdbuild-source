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
		final Collection<DBClass> allClasses = dbDriver().findAllClasses();
		assertThat(namesOf(allClasses), not(hasItem(A_NEW_CLASS_NAME)));
		assertThat(namesOf(allClasses), not(hasItem(A_NEW_SUPERCLASS_NAME)));
		assertThat(namesOf(allClasses), not(hasItem(A_NEW_SUBCLASS_NAME)));
		assertThat(namesOf(allClasses), not(hasItem(ANOTHER_NEW_SUBCLASS_NAME)));
	}

	@Test
	public void singleClassCanBeAdded() {
		final int actualClassesCount = dbDriver().findAllClasses().size();

		DBClass newClass = dbDriver().createClass(newClass(A_NEW_CLASS_NAME, null));

		assertThat(newClass.getName(), equalTo(A_NEW_CLASS_NAME));

		final Collection<DBClass> allClasses = dbDriver().findAllClasses();
		assertThat(allClasses.size(), equalTo(actualClassesCount + 1));
		assertThat(namesOf(allClasses), hasItem(A_NEW_CLASS_NAME));

		newClass = dbDriver().findClassByName(A_NEW_CLASS_NAME);
		assertThat(newClass, not(is(nullValue())));
	}

	@Test
	public void singleSuperClassCanBeAdded() {
		DBClass newClass = dbDriver().createClass(newSuperClass(A_NEW_CLASS_NAME, null));

		assertThat(newClass.getName(), equalTo(A_NEW_CLASS_NAME));
		assertThat(newClass.isSuperclass(), is(true));

		final Collection<DBClass> allClasses = dbDriver().findAllClasses();
		assertThat(namesOf(allClasses), hasItem(A_NEW_CLASS_NAME));

		newClass = dbDriver().findClassByName(A_NEW_CLASS_NAME);
		assertThat(newClass, not(is(nullValue())));
		assertThat(newClass.getName(), equalTo(A_NEW_CLASS_NAME));
		assertThat(newClass.isSuperclass(), is(true));
	}

	@Test
	public void classesCanBeCreatedHierarchically() {
		DBClass superClass = dbDriver().createClass(newSuperClass(A_NEW_SUPERCLASS_NAME, null));
		DBClass subClassA = dbDriver().createClass(newClass(A_NEW_SUBCLASS_NAME, superClass));
		DBClass subClassB = dbDriver().createClass(newClass(ANOTHER_NEW_SUBCLASS_NAME, superClass));

		assertThatHierarchyIsCorrect(superClass, subClassA, subClassB);

		// reload classes
		superClass = dbDriver().findClassById(superClass.getId());
		subClassA = dbDriver().findClassById(subClassA.getId());
		subClassB = dbDriver().findClassById(subClassB.getId());

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
		DBClass superClass = dbDriver().createClass(newClass(A_NEW_SUPERCLASS_NAME, null));
		final DBClass subClass = dbDriver().createClass(newClass(A_NEW_SUBCLASS_NAME, superClass));

		dbDriver().deleteClass(subClass);

		assertThat(namesOf(superClass.getChildren()), not(hasItem(A_NEW_SUPERCLASS_NAME)));
		// assertThat(subClass.getId(), is(nullValue()));

		// reload classes
		superClass = dbDriver().findClassById(superClass.getId());
		assertThat(namesOf(superClass.getChildren()), not(hasItem(A_NEW_SUPERCLASS_NAME)));
	}

	@Test(expected = Exception.class)
	public void shouldThrowExceptionCreatingClassesWithSameName() {
		dbDriver().createClass(newClass(A_NEW_CLASS_NAME, null));
		dbDriver().createClass(newClass(A_NEW_CLASS_NAME, null));
	}

	@Test
	public void shouldReturnNullIfNotExistingAttribute() {
		final DBClass newClass = dbDriver().createClass(newClass(A_NEW_CLASS_NAME, null));
		final DBAttribute attribute = newClass.getAttribute(NOT_EXISTING_ATTRIBUTE);
		assertThat(attribute, nullValue());
	}

	@Test
	public void shouldRetrieveAllDefaultUserAttributes() {
		final DBClass newClass = dbDriver().createClass(newClass(A_NEW_CLASS_NAME, null));
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
		final DBClass superClass = dbDriver().createClass(newSuperClass(A_NEW_SUPERCLASS_NAME, null));
		final DBClass subClassA = dbDriver().createClass(newSuperClass(A_NEW_SUBCLASS_NAME, superClass));
		final DBClass subClassB = dbDriver().createClass(newSuperClass(ANOTHER_NEW_SUBCLASS_NAME, superClass));
		final DBClass leafA = dbDriver().createClass(newClass(LEAF_CLASS_NAME, subClassA));
		final DBClass leafB = dbDriver().createClass(newClass(ANOTHER_LEAF_CLASS_NAME, subClassA));

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
		final DBClass superClass = dbDriver().createClass(newClass(A_NEW_SUPERCLASS_NAME, null));
		final DBClass subClassA = dbDriver().createClass(newSuperClass(A_NEW_SUBCLASS_NAME, superClass));
		final DBClass subClassB = dbDriver().createClass(newSuperClass(ANOTHER_NEW_SUBCLASS_NAME, superClass));
		final DBClass leafA = dbDriver().createClass(newClass(LEAF_CLASS_NAME, subClassA));
		final DBClass leafB = dbDriver().createClass(newClass(ANOTHER_LEAF_CLASS_NAME, subClassA));

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
		final DBClass superClass = dbDriver().createClass(newSuperClass(A_NEW_SUPERCLASS_NAME, null));

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
		final DBClass simpleClass = dbDriver().createClass(newClass(LEAF_CLASS_NAME, null));

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
		final DBClass superClass = dbDriver().createClass(newSuperClass(A_NEW_SUPERCLASS_NAME, null));
		final DBClass leafClass = dbDriver().createClass(newClass(LEAF_CLASS_NAME, superClass));

		// then
		assertThat(superClass.isSuperclass(), is(equalTo(true)));
		assertThat(leafClass.isSuperclass(), is(equalTo(false)));
	}

	@Ignore
	@Test
	public void reservedAttributeAreNotFetched() {
		final DBClass grants = dbDriver().findClassByName("Grant");
		final CMAttribute att = grants.getAttribute("IdGrantedClass");
		assertThat(att, is(equalTo(null)));
	}

}

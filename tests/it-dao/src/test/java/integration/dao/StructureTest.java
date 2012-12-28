package integration.dao;

import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Before;
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

	private int startingClassesCount;

	@Before
	public void setUp() throws Exception {
		startingClassesCount = size(dbDataView().findAllClasses());
	}

	@Test
	public void noTestClassesShouldBeDefinedAsDefault() {
		final Iterable<DBClass> allClasses = dbDataView().findAllClasses();
		assertThat(namesOf(allClasses), not(hasItem(A_NEW_CLASS_NAME)));
		assertThat(namesOf(allClasses), not(hasItem(A_NEW_SUPERCLASS_NAME)));
		assertThat(namesOf(allClasses), not(hasItem(A_NEW_SUBCLASS_NAME)));
		assertThat(namesOf(allClasses), not(hasItem(ANOTHER_NEW_SUBCLASS_NAME)));
	}

	@Test
	public void singleClassCanBeAdded() {
		// given
		final DBClass createdClass = dbDataView().createClass(newClass(A_NEW_CLASS_NAME, null));
		assertThat(createdClass.getName(), equalTo(A_NEW_CLASS_NAME));

		// when
		final Iterable<DBClass> allClasses = dbDataView().findAllClasses();
		final DBClass readedClass = dbDataView().findClassByName(A_NEW_CLASS_NAME);

		// then
		assertThat(size(allClasses), equalTo(startingClassesCount + 1));
		assertThat(namesOf(allClasses), hasItem(A_NEW_CLASS_NAME));
		assertThat(readedClass, not(is(nullValue())));
		assertThat(readedClass.getName(), equalTo(A_NEW_CLASS_NAME));
	}

	@Test
	public void singleSuperClassCanBeAdded() {
		final DBClass createdClass = dbDataView().createClass(newSuperClass(A_NEW_CLASS_NAME, null));

		assertThat(createdClass.getName(), equalTo(A_NEW_CLASS_NAME));
		assertThat(createdClass.isSuperclass(), is(true));

		final Iterable<DBClass> allClasses = dbDataView().findAllClasses();
		assertThat(namesOf(allClasses), hasItem(A_NEW_CLASS_NAME));

		final DBClass readedClass = dbDataView().findClassByName(A_NEW_CLASS_NAME);
		assertThat(readedClass, not(is(nullValue())));
		assertThat(readedClass.getName(), equalTo(A_NEW_CLASS_NAME));
		assertThat(readedClass.isSuperclass(), is(true));
	}

	@Test
	public void classesCanBeCreatedHierarchically() {
		DBClass superClass = dbDataView().createClass(newSuperClass(A_NEW_SUPERCLASS_NAME, null));
		DBClass subClassA = dbDataView().createClass(newClass(A_NEW_SUBCLASS_NAME, superClass));
		DBClass subClassB = dbDataView().createClass(newClass(ANOTHER_NEW_SUBCLASS_NAME, superClass));

		assertThatHierarchyIsCorrect(superClass, subClassA, subClassB);

		// reload classes
		superClass = dbDataView().findClassById(superClass.getId());
		subClassA = dbDataView().findClassById(subClassA.getId());
		subClassB = dbDataView().findClassById(subClassB.getId());

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
		// given
		final DBClass createdSuperClass = dbDataView().createClass(newClass(A_NEW_SUPERCLASS_NAME, null));
		final DBClass subClass = dbDataView().createClass(newClass(A_NEW_SUBCLASS_NAME, createdSuperClass));

		// when
		dbDataView().deleteClass(subClass);

		// then
		assertThat(namesOf(createdSuperClass.getChildren()), not(hasItem(A_NEW_SUPERCLASS_NAME)));
		// reload classes
		final DBClass readedSuperClass = dbDataView().findClassById(createdSuperClass.getId());
		assertThat(namesOf(readedSuperClass.getChildren()), not(hasItem(A_NEW_SUPERCLASS_NAME)));
	}

	@Test(expected = Exception.class)
	public void shouldThrowExceptionCreatingClassesWithSameName() {
		dbDataView().createClass(newClass(A_NEW_CLASS_NAME, null));
		dbDataView().createClass(newClass(A_NEW_CLASS_NAME, null));
	}

	@Test
	public void shouldReturnNullIfNotExistingAttribute() {
		// given
		final DBClass newClass = dbDataView().createClass(newClass(A_NEW_CLASS_NAME, null));

		// when
		final DBAttribute attribute = newClass.getAttribute(NOT_EXISTING_ATTRIBUTE);

		// then
		assertThat(attribute, nullValue());
	}

	@Test
	public void shouldRetrieveAllDefaultUserAttributes() {
		// given
		final DBClass newClass = dbDataView().createClass(newClass(A_NEW_CLASS_NAME, null));

		// when

		// then
		assertThat(newClass.getAttribute("Code"), notNullValue());
		assertThat(newClass.getAttribute("Description"), notNullValue());
		assertThat(newClass.getAttribute("Notes"), notNullValue());
	}

	@Test
	public void shouldRetrieveAllLeavesClassesFromRoot() {
		// given
		final DBClass superClass = dbDataView().createClass(newSuperClass(A_NEW_SUPERCLASS_NAME, null));
		final DBClass subClassA = dbDataView().createClass(newSuperClass(A_NEW_SUBCLASS_NAME, superClass));
		final DBClass subClassB = dbDataView().createClass(newSuperClass(ANOTHER_NEW_SUBCLASS_NAME, superClass));
		final DBClass leafA = dbDataView().createClass(newClass(LEAF_CLASS_NAME, subClassA));
		final DBClass leafB = dbDataView().createClass(newClass(ANOTHER_LEAF_CLASS_NAME, subClassA));

		// when
		final List<DBClass> leaves = Lists.newArrayList(superClass.getLeaves());

		// then
		assertThat(leaves.size(), is(equalTo(2)));
		assertThat(leaves, hasItems(leafA, leafB));
	}

	@Test
	public void shouldRetrieveAllLeavesClassesFromGenericSuperclass() {
		// given
		final DBClass superClass = dbDataView().createClass(newClass(A_NEW_SUPERCLASS_NAME, null));
		final DBClass subClassA = dbDataView().createClass(newSuperClass(A_NEW_SUBCLASS_NAME, superClass));
		final DBClass subClassB = dbDataView().createClass(newSuperClass(ANOTHER_NEW_SUBCLASS_NAME, superClass));
		final DBClass leafA = dbDataView().createClass(newClass(LEAF_CLASS_NAME, subClassA));
		final DBClass leafB = dbDataView().createClass(newClass(ANOTHER_LEAF_CLASS_NAME, subClassA));

		// when
		final List<DBClass> leaves = Lists.newArrayList(subClassA.getLeaves());

		// then
		assertThat(leaves.size(), is(equalTo(2)));
		assertThat(leaves, hasItems(leafA, leafB));
	}

	@Test
	public void superClassShouldNotBeALeaf() {
		// given
		final DBClass superClass = dbDataView().createClass(newSuperClass(A_NEW_SUPERCLASS_NAME, null));

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
		final DBClass simpleClass = dbDataView().createClass(newClass(LEAF_CLASS_NAME, null));

		// when
		final List<DBClass> leaves = Lists.newArrayList(simpleClass.getLeaves());

		// then
		assertThat(leaves.size(), is(equalTo(1)));
		assertThat(leaves, hasItems(simpleClass));
	}

	@Test
	public void shouldReturnTrueIfSuperclass() {
		// given
		final DBClass superClass = dbDataView().createClass(newSuperClass(A_NEW_SUPERCLASS_NAME, null));
		final DBClass leafClass = dbDataView().createClass(newClass(LEAF_CLASS_NAME, superClass));

		// when

		// then
		assertThat(superClass.isSuperclass(), is(equalTo(true)));
		assertThat(leafClass.isSuperclass(), is(equalTo(false)));
	}

	@Ignore
	@Test
	public void reservedAttributeAreNotFetched() {
		// given
		final DBClass grants = dbDataView().findClassByName("Grant");

		// when
		final CMAttribute att = grants.getAttribute("IdGrantedClass");

		// then
		assertThat(att, is(equalTo(null)));
	}

}

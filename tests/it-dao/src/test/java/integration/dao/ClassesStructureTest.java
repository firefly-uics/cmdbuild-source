package integration.dao;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static utils.IntergrationTestUtils.namesOf;
import static utils.IntergrationTestUtils.newClass;
import static utils.IntergrationTestUtils.newSimpleClass;
import static utils.IntergrationTestUtils.newSuperClass;

import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Test;

import utils.IntegrationTestBase;

public class ClassesStructureTest extends IntegrationTestBase {

	@Test
	public void classesReturnedFromCreationMethodHaveAllData() {
		// given

		// when
		final DBClass superClass = dbDataView().createClass(newSuperClass("super"));
		final DBClass classWithNoSuperClass = dbDataView().createClass(newClass("foo"));
		final DBClass classWithSuperClass = dbDataView().createClass(newClass("bar", superClass));
		final DBClass simpleClass = dbDataView().createClass(newSimpleClass("baz"));

		// then
		assertThat(superClass.getName(), equalTo("super"));
		assertThat(superClass.isSuperclass(), equalTo(true));
		assertThat(superClass.getParent(), nullValue());
		assertThat(superClass.getLeaves(), hasItems(classWithSuperClass));

		assertThat(classWithNoSuperClass.getName(), equalTo("foo"));
		assertThat(classWithNoSuperClass.isSuperclass(), equalTo(false));
		assertThat(classWithNoSuperClass.getParent(), nullValue());
		assertThat(classWithNoSuperClass.holdsHistory(), equalTo(true));

		assertThat(classWithSuperClass.getName(), equalTo("bar"));
		assertThat(classWithSuperClass.isSuperclass(), equalTo(false));
		assertThat(classWithSuperClass.getParent(), equalTo(superClass));
		assertThat(classWithSuperClass.holdsHistory(), equalTo(true));

		assertThat(simpleClass.getName(), equalTo("baz"));
		assertThat(simpleClass.isSuperclass(), equalTo(false));
		assertThat(simpleClass.getParent(), nullValue());
		assertThat(simpleClass.holdsHistory(), equalTo(false));
	}

	@Test
	public void classesHierarchyBuildedWithoutReloadingClasses() {
		// given

		// when
		final DBClass superClass = dbDataView().createClass(newSuperClass("superClass"));
		final DBClass subClassA = dbDataView().createClass(newClass("subClassA", superClass));
		final DBClass subClassB = dbDataView().createClass(newClass("subClassB", superClass));

		// then
		assertThat(superClass.isSuperclass(), is(true));
		assertThat(superClass.isAncestorOf(subClassA), is(true));
		assertThat(superClass.isAncestorOf(subClassB), is(true));
		assertThat(namesOf(superClass.getChildren()), hasItem("subClassA"));
		assertThat(namesOf(superClass.getChildren()), hasItem("subClassB"));
		assertThat(subClassA.getParent().getName(), equalTo("superClass"));
		assertThat(subClassB.getParent().getName(), equalTo("superClass"));
	}

	@Test
	public void newlyCreatedClassesCanBeFoundBetweenAllClasses() {
		// given
		final DBClass superClass = dbDataView().createClass(newSuperClass("super"));
		dbDataView().createClass(newClass("foo"));
		dbDataView().createClass(newClass("bar", superClass));
		dbDataView().createClass(newSimpleClass("baz"));

		// when
		final Iterable<String> classNames = namesOf(dbDataView().findAllClasses());

		// then
		assertThat(classNames, hasItems("super", "foo", "bar", "baz"));
	}

	@Test
	public void classCreatedAndRead() {
		// given
		dbDataView().createClass(newClass("foo"));

		// when
		final DBClass clazz = dbDataView().findClassByName("foo");

		// then
		assertThat(clazz.getName(), equalTo("foo"));
		assertThat(clazz.isSuperclass(), equalTo(false));
		assertThat(clazz.getParent(), nullValue());
		assertThat(clazz.holdsHistory(), equalTo(true));
	}

	@Test
	public void superClassCreatedAndRead() {
		// given
		dbDataView().createClass(newSuperClass("foo"));

		// when
		final DBClass superClass = dbDataView().findClassByName("foo");

		// then
		assertThat(superClass.getName(), equalTo("foo"));
		assertThat(superClass.isSuperclass(), equalTo(true));
		assertThat(superClass.getParent(), nullValue());
		assertThat(isEmpty(superClass.getLeaves()), equalTo(true));
	}

	@Test
	public void simpleClassCreatedAndRead() {
		// given
		dbDataView().createClass(newSimpleClass("foo"));

		// when
		final DBClass clazz = dbDataView().findClassByName("foo");

		// then
		assertThat(clazz.getName(), equalTo("foo"));
		assertThat(clazz.isSuperclass(), equalTo(false));
		assertThat(clazz.getParent(), nullValue());
		assertThat(clazz.holdsHistory(), equalTo(false));
	}

	@Test
	public void classesHierarchyAfterReloadingClasses() {
		// given
		DBClass superClass = dbDataView().createClass(newSuperClass("superClass"));
		DBClass subClassA = dbDataView().createClass(newClass("subClassA", superClass));
		DBClass subClassB = dbDataView().createClass(newClass("subClassB", superClass));

		// when
		superClass = dbDataView().findClassById(superClass.getId());
		subClassA = dbDataView().findClassById(subClassA.getId());
		subClassB = dbDataView().findClassById(subClassB.getId());

		assertThat(superClass.isSuperclass(), is(true));
		assertThat(superClass.isAncestorOf(subClassA), is(true));
		assertThat(superClass.isAncestorOf(subClassB), is(true));
		assertThat(namesOf(superClass.getChildren()), hasItem("subClassA"));
		assertThat(namesOf(superClass.getChildren()), hasItem("subClassB"));
		assertThat(subClassA.getParent().getName(), equalTo("superClass"));
		assertThat(subClassB.getParent().getName(), equalTo("superClass"));
	}

	@Test
	public void classesHierarchyUpdatedAfterDeletingClassButWithoutReloadingClasses() {
		// given
		final DBClass root = dbDataView().createClass(newClass("root"));
		final DBClass sub = dbDataView().createClass(newClass("sub", root));

		// when
		dbDataView().deleteClass(sub);

		// then
		assertThat(namesOf(root.getChildren()), not(hasItem(sub.getName())));
	}

	@Test
	public void classesHierarchyUpdatedAfterDeletingClassAndAfterReloadingClasses() {
		// given
		final DBClass _root = dbDataView().createClass(newSuperClass("root"));
		final DBClass _sub = dbDataView().createClass(newClass("sub", _root));

		// when
		dbDataView().deleteClass(_sub);
		final DBClass root = dbDataView().findClassByName(_root.getName());

		// then
		assertThat(namesOf(root.getChildren()), not(hasItem(_sub.getName())));
	}

	@Test(expected = Exception.class)
	public void cannotCreateTwoClassesWithSameName() {
		// given
		dbDataView().createClass(newClass("foo"));

		// when
		dbDataView().createClass(newClass("foo"));

		// then
		// ...
	}

	@Test
	public void leafClassesCanBeFetchedFromRootClass() {
		// given
		final DBClass root = dbDataView().createClass(newSuperClass("root"));
		final DBClass subClassA = dbDataView().createClass(newSuperClass("subClassA", root));
		final DBClass subClassB = dbDataView().createClass(newSuperClass("subClassB", root));
		final DBClass leafA = dbDataView().createClass(newClass("leafA", subClassA));
		final DBClass leafB = dbDataView().createClass(newClass("leafB", subClassA));

		// when
		final Iterable<DBClass> leaves = dbDataView().findClassByName(root.getName()).getLeaves();

		// then
		assertThat(size(leaves), is(equalTo(2)));
		assertThat(leaves, hasItems(leafA, leafB));
	}

	@Test
	public void leafClassesCanBeFetchedFromAnyNonRootSuperClass() {
		// given
		final DBClass root = dbDataView().createClass(newSuperClass("root"));
		final DBClass subClassA = dbDataView().createClass(newSuperClass("subClassA", root));
		final DBClass subClassB = dbDataView().createClass(newSuperClass("subClassB", root));
		final DBClass leafA = dbDataView().createClass(newClass("leafA", subClassA));
		final DBClass leafB = dbDataView().createClass(newClass("leafB", subClassA));

		// when
		final Iterable<DBClass> leaves = dbDataView().findClassByName(subClassA.getName()).getLeaves();

		// then
		assertThat(size(leaves), is(equalTo(2)));
		assertThat(leaves, hasItems(leafA, leafB));
	}

	@Test
	public void superClassShouldNotBeALeaf() {
		// given
		dbDataView().createClass(newSuperClass("root"));

		// when
		final Iterable<DBClass> items = dbDataView().findClassByName("root").getLeaves();

		// then
		assertThat(isEmpty(items), is(true));
	}

}

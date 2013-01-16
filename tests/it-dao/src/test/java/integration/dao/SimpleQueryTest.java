package integration.dao;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static utils.IntegrationTestUtils.newClass;
import static utils.IntegrationTestUtils.newSuperClass;

import java.util.NoSuchElementException;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.junit.Test;

import utils.IntegrationTestBase;

import com.google.common.collect.Iterables;

public class SimpleQueryTest extends IntegrationTestBase {

	@Test
	public void simpleSubclassQuery() {
		final DBClass newClass = dbDataView().create(newClass("foo"));

		final Object attr1Value = "Pizza";
		final Object attr2Value = "Calzone";

		dbDataView().createCardFor(newClass) //
				.setCode(attr1Value) //
				.setDescription(attr2Value) //
				.save();

		final CMQueryResult result = dbDataView() //
				.select(newClass.getCodeAttributeName()) //
				.from(newClass) //
				.run();

		// TEST ATTRIBUTE_1 is extracted but not ATTRIBUTE_2
		final CMQueryRow firstRow = result.iterator().next();
		assertThat(result.size(), equalTo(1));

		final CMCard cmCard = firstRow.getCard(newClass);
		assertThat(cmCard.getCode(), equalTo(attr1Value));
		try {
			cmCard.getDescription();
			fail("Value for description attribute should have not been loaded");
		} catch (final UnsupportedOperationException e) {
			// FIXME misleading behavior
			assertThat(e.getMessage(), equalTo("Not implemented"));
		}
	}

	@Test
	public void simpleSubclassQueryForAnyAttribute() {
		final int TOTAL = 5;

		final DBClass newClass = dbDataView().create(newClass("foo"));
		for (int i = 0; i < TOTAL; i++) {
			dbDataView().createCardFor(newClass) //
					.set(newClass.getCodeAttributeName(), String.valueOf(i)) //
					.save();
		}

		final Alias classAlias = as("foo");

		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(classAlias)) //
				.from(newClass, as(classAlias)) //
				.run();

		assertThat(result.size(), equalTo(TOTAL));
		assertThat(result.totalSize(), equalTo(TOTAL));

		final CMQueryRow[] rows = Iterables.toArray(result, CMQueryRow.class);
		for (int i = 0; i < TOTAL; i++) {
			final CMQueryRow row = rows[i];
			final CMCard cmCard = row.getCard(classAlias);
			assertThat(cmCard.getCode(), equalTo((Object) Integer.toString(i)));
		}
	}

	@Test
	public void simpleSuperclassQuery() {
		final DBClass root = dbDataView().create(newSuperClass("root"));
		final DBClass superNotRoot = dbDataView().create(newSuperClass("superNotRoot", root));
		final DBClass leafOfSuperNotRoot = dbDataView().create(newClass("leafOfSuperNotRoot", superNotRoot));
		final DBClass leafOfRoot = dbDataView().create(newClass("leafOfRoot", root));
		final DBClass anotherLeafOfRoot = dbDataView().create(newClass("anotherLeafOfRoot", root));
		dbDataView().createCardFor(leafOfSuperNotRoot) //
				.set(leafOfSuperNotRoot.getCodeAttributeName(), leafOfSuperNotRoot.getName()) //
				.save();
		dbDataView().createCardFor(leafOfRoot) //
				.set(leafOfRoot.getCodeAttributeName(), leafOfRoot.getName()) //
				.save();
		dbDataView().createCardFor(anotherLeafOfRoot) //
				.set(anotherLeafOfRoot.getCodeAttributeName(), anotherLeafOfRoot.getName()) //
				.save();

		final CMQueryResult result = dbDataView() //
				.select(root.getCodeAttributeName()) //
				.from(root) //
				.run();

		assertThat(result.size(), equalTo(3));
		for (final CMQueryRow row : result) {
			final CMCard c = row.getCard(root);
			final String expectedClassName = (String) c.getCode();
			assertThat(c.getType().getName(), equalTo(expectedClassName));
		}
	}

	@Test
	public void simpleSuperclassQueryForAnyAttribute() {
		// given
		final DBClass root = dbDataView().create(newSuperClass("root"));
		final DBClass superNotRoot = dbDataView().create(newSuperClass("superNotRoot", root));
		final DBClass leafOfSuperNotRoot = dbDataView().create(newClass("leafOfSuperNotRoot", superNotRoot));
		final DBClass leafOfRoot = dbDataView().create(newClass("leafOfRoot", root));
		final DBClass anotherLeafOfRoot = dbDataView().create(newClass("anotherLeafOfRoot", root));
		dbDataView().createCardFor(leafOfSuperNotRoot) //
				.set(leafOfSuperNotRoot.getCodeAttributeName(), leafOfSuperNotRoot.getName()) //
				.save();
		dbDataView().createCardFor(leafOfRoot) //
				.set(leafOfRoot.getCodeAttributeName(), leafOfRoot.getName()) //
				.save();
		dbDataView().createCardFor(anotherLeafOfRoot) //
				.set(anotherLeafOfRoot.getCodeAttributeName(), anotherLeafOfRoot.getName()) //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(root)) //
				.from(root) //
				.run();

		// then
		assertThat(result.size(), equalTo(3));
		for (final CMQueryRow row : result) {
			final CMCard c = row.getCard(root);
			final String expectedClassName = (String) c.getCode();
			assertThat(c.getType().getName(), equalTo(expectedClassName));
		}
	}

	@Test
	public void simpleCountedQuery() {
		final int TOTAL_SIZE = 10;
		final int OFFSET = 5;
		final int LIMIT = 3;

		final DBClass newClass = dbDataView().create(newClass("foo"));

		for (int i = 0; i < TOTAL_SIZE; i++) {
			dbDataView().createCardFor(newClass) //
					.set(newClass.getCodeAttributeName(), String.valueOf(i)) //
					.save();
		}

		final CMQueryResult result = dbDataView() //
				.select(newClass.getCodeAttributeName()) //
				.from(newClass) //
				.offset(OFFSET) //
				.limit(LIMIT) //
				.run();

		assertThat(result.size(), equalTo(LIMIT));
		assertThat(result.totalSize(), equalTo(TOTAL_SIZE));
	}

	@Test
	public void singleWhereClause() {
		final DBClass newClass = dbDataView().create(newClass("foo"));
		final int NUMBER_OF_INSERTED_CARDS = 5;
		for (int i = 0; i < NUMBER_OF_INSERTED_CARDS; i++) {
			dbDataView().createCardFor(newClass) //
					.set(newClass.getCodeAttributeName(), String.valueOf(i)) //
					.save();
		}
		final Object codeValueToFind = "" + (NUMBER_OF_INSERTED_CARDS - 1);
		final String codeAttributeName = newClass.getCodeAttributeName();

		final CMQueryRow row = dbDataView() //
				.select(codeAttributeName) //
				.from(newClass) //
				.where(condition(attribute(newClass, codeAttributeName), eq(codeValueToFind))) //
				.run().getOnlyRow();

		assertThat(row.getCard(newClass).get(codeAttributeName), equalTo(codeValueToFind));
	}

	@Test
	public void simpleQueryWithoutWhereClause() {
		final DBClass newClass = dbDataView().create(newClass("foo"));

		final int NUMBER_OF_INSERTED_CARDS = 5;
		for (int i = 0; i < NUMBER_OF_INSERTED_CARDS; i++) {
			dbDataView().createCardFor(newClass) //
					.set(newClass.getCodeAttributeName(), String.valueOf(i)) //
					.save();
		}
		final String codeAttributeName = newClass.getCodeAttributeName();

		final CMQueryResult result = dbDataView() //
				.select(codeAttributeName) //
				.from(newClass) //
				.run();

		assertThat(result.size(), equalTo(NUMBER_OF_INSERTED_CARDS));
	}

	@Test(expected = NoSuchElementException.class)
	public void getOnlyRowShouldThrowExceptionBecauseOfMoreThanOneRowAsResult() {
		// given
		final DBClass newClass = dbDataView().create(newClass("foo"));
		dbDataView().createCardFor(newClass) //
				.set(newClass.getCodeAttributeName(), newClass.getName()) //
				.save();
		dbDataView().createCardFor(newClass) //
				.set(newClass.getCodeAttributeName(), newClass.getName()) //
				.save();

		// when
		dbDataView() //
				.select(newClass.getCodeAttributeName()) //
				.from(newClass) //
				.run().getOnlyRow();

		// then
		// ...
	}

	@Test(expected = NoSuchElementException.class)
	public void getOnlyRowShouldThrowExceptionBecauseOfNoResults() {
		final DBClass newClass = dbDataView().create(newClass("foo"));
		dbDataView() //
				.select(newClass.getCodeAttributeName()) //
				.from(newClass) //
				.run() //
				.getOnlyRow();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void malformedQueryShouldThrowException() {
		// given
		final DBClass newClass = dbDataView().create(newClass("foo"));

		// when
		final String codeAttributeName = newClass.getCodeAttributeName();
		dbDataView() //
				.select(codeAttributeName) //
				.run();

		// then
		// exception
	}

}

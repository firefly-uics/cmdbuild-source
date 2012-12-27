package integration.dao;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.junit.Test;

import utils.DBFixture;

import com.google.common.collect.Iterables;

public class SimpleQueryTest extends DBFixture {

	@Test
	public void simpleSubclassQuery() {
		final DBClass newClass = dbDataView().createClass(newClass(uniqueUUID(), null));

		final Object attr1Value = "Pizza";
		final Object attr2Value = "Calzone";

		dbDataView().newCard(newClass) //
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

		final DBClass newClass = dbDataView().createClass(newClass(uniqueUUID(), null));
		insertCards(newClass, TOTAL);

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
		final Alias rootAlias = as("root");

		final DBClass root = dbDataView().createClass(newSuperClass(uniqueUUID(), null));
		final DBClass superNotRoot = dbDataView().createClass(newSuperClass(uniqueUUID(), root));
		final DBClass leafOfSuperNotRoot = dbDataView().createClass(newClass(uniqueUUID(), superNotRoot));
		final DBClass leafOfRoot = dbDataView().createClass(newClass(uniqueUUID(), root));
		final DBClass anotherLeafOfRoot = dbDataView().createClass(newClass(uniqueUUID(), root));
		insertCardWithCode(leafOfSuperNotRoot, leafOfSuperNotRoot.getName());
		insertCardWithCode(leafOfRoot, leafOfRoot.getName());
		insertCardWithCode(anotherLeafOfRoot, anotherLeafOfRoot.getName());

		final CMQueryResult result = dbDataView() //
				.select(root.getCodeAttributeName()) //
				.from(root, rootAlias) //
				.run();

		assertThat(result.size(), equalTo(3));
		for (final CMQueryRow row : result) {
			final CMCard c = row.getCard(rootAlias);
			// the value was intentionally set to the class name
			final String expectedClassName = (String) c.getCode();
			assertThat(c.getType().getName(), equalTo(expectedClassName));
		}
	}

	@Test
	public void simpleSuperclassQueryForAnyAttribute() {
		final Alias rootAlias = as("root");

		final DBClass root = dbDataView().createClass(newSuperClass(uniqueUUID(), null));
		final DBClass superNotRoot = dbDataView().createClass(newSuperClass(uniqueUUID(), root));
		final DBClass leafOfSuperNotRoot = dbDataView().createClass(newClass(uniqueUUID(), superNotRoot));
		final DBClass leafOfRoot = dbDataView().createClass(newClass(uniqueUUID(), root));
		final DBClass anotherLeafOfRoot = dbDataView().createClass(newClass(uniqueUUID(), root));
		insertCardWithCode(leafOfSuperNotRoot, leafOfSuperNotRoot.getName());
		insertCardWithCode(leafOfRoot, leafOfRoot.getName());
		insertCardWithCode(anotherLeafOfRoot, anotherLeafOfRoot.getName());

		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(rootAlias)) //
				.from(root, rootAlias) //
				.run();

		assertThat(result.size(), equalTo(3));
		for (final CMQueryRow row : result) {
			final CMCard c = row.getCard(rootAlias);
			// the value was intentionally set to the class name
			final String expectedClassName = (String) c.getCode();
			assertThat(c.getType().getName(), equalTo(expectedClassName));
		}
	}

	@Test
	public void simpleCountedQuery() {
		final int TOTAL_SIZE = 10;
		final int OFFSET = 5;
		final int LIMIT = 3;

		final DBClass newClass = dbDataView().createClass(newClass(uniqueUUID(), null));

		insertCards(newClass, TOTAL_SIZE);

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
		final DBClass newClass = dbDataView().createClass(newClass(uniqueUUID(), null));
		final int NUMBER_OF_INSERTED_CARDS = 5;
		insertCards(newClass, NUMBER_OF_INSERTED_CARDS);
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
		final DBClass newClass = dbDataView().createClass(newClass(uniqueUUID(), null));

		final int NUMBER_OF_INSERTED_CARDS = 5;
		insertCards(newClass, NUMBER_OF_INSERTED_CARDS);
		final String codeAttributeName = newClass.getCodeAttributeName();

		final CMQueryResult result = dbDataView() //
				.select(codeAttributeName) //
				.from(newClass) //
				.run();

		assertThat(result.size(), equalTo(NUMBER_OF_INSERTED_CARDS));
	}

	@Test(expected = NoSuchElementException.class)
	public void getOnlyRowShouldThrowExceptionBecauseOfMoreThanOneRowAsResult() {
		final DBClass newClass = dbDataView().createClass(newClass(uniqueUUID(), null));
		final int NUMBER_OF_INSERTED_CARDS = 5;
		insertCards(newClass, NUMBER_OF_INSERTED_CARDS);
		dbDataView() //
				.select(newClass.getCodeAttributeName()) //
				.from(newClass) //
				.run().getOnlyRow();
	}

	@Test(expected = NoSuchElementException.class)
	public void getOnlyRowShouldThrowExceptionBecauseOfNoResults() {
		final DBClass newClass = dbDataView().createClass(newClass(uniqueUUID(), null));
		dbDataView() //
				.select(newClass.getCodeAttributeName()) //
				.from(newClass) //
				.run() //
				.getOnlyRow();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void malformedQueryShouldThrowException() {
		// given
		final DBClass newClass = dbDataView().createClass(newClass(uniqueUUID(), null));

		// when
		final String codeAttributeName = newClass.getCodeAttributeName();
		dbDataView() //
				.select(codeAttributeName) //
				.run();

		// then
		// exception
	}

}

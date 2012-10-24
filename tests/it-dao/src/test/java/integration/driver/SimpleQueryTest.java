package integration.driver;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.alias.Alias.canonicalAlias;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.Operator.EQUALS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.collect.Iterables;

@RunWith(value = Parameterized.class)
public class SimpleQueryTest extends DriverFixture {

	public SimpleQueryTest(final String driverBeanName) {
		super(driverBeanName);
	}

	@Test
	public void simpleSubclassQuery() {
		final DBClass newClass = driver.createClass(uniqueUUID(), null);

		final Object attr1Value = "Pizza";
		final Object attr2Value = "Calzone";

		DBCard.newInstance(driver, newClass) //
				.setCode(attr1Value) //
				.setDescription(attr2Value) //
				.save();

		final CMQueryResult result = new QuerySpecsBuilder(view) //
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

		final DBClass newClass = driver.createClass(uniqueUUID(), null);
		insertCards(newClass, TOTAL);

		final Alias classAlias = as("foo");

		final CMQueryResult result = new QuerySpecsBuilder(view) //
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

		final DBClass root = driver.createSuperClass(uniqueUUID(), null);
		final DBClass superNotRoot = driver.createSuperClass(uniqueUUID(), root);
		final DBClass leafOfSuperNotRoot = driver.createClass(uniqueUUID(), superNotRoot);
		final DBClass leafOfRoot = driver.createClass(uniqueUUID(), root);
		final DBClass anotherLeafOfRoot = driver.createClass(uniqueUUID(), root);
		insertCardWithCode(leafOfSuperNotRoot, leafOfSuperNotRoot.getName());
		insertCardWithCode(leafOfRoot, leafOfRoot.getName());
		insertCardWithCode(anotherLeafOfRoot, anotherLeafOfRoot.getName());

		final CMQueryResult result = new QuerySpecsBuilder(view) //
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

		final DBClass root = driver.createSuperClass(uniqueUUID(), null);
		final DBClass superNotRoot = driver.createSuperClass(uniqueUUID(), root);
		final DBClass leafOfSuperNotRoot = driver.createClass(uniqueUUID(), superNotRoot);
		final DBClass leafOfRoot = driver.createClass(uniqueUUID(), root);
		final DBClass anotherLeafOfRoot = driver.createClass(uniqueUUID(), root);
		insertCardWithCode(leafOfSuperNotRoot, leafOfSuperNotRoot.getName());
		insertCardWithCode(leafOfRoot, leafOfRoot.getName());
		insertCardWithCode(anotherLeafOfRoot, anotherLeafOfRoot.getName());

		final CMQueryResult result = new QuerySpecsBuilder(view) //
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

		final DBClass newClass = driver.createClass(uniqueUUID(), null);

		insertCards(newClass, TOTAL_SIZE);

		final CMQueryResult result = new QuerySpecsBuilder(view) //
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
		final DBClass newClass = driver.createClass(uniqueUUID(), null);
		insertCards(newClass, 5);
		final Object cardAttributeToFind = "3";
		final String codeAttributeName = newClass.getCodeAttributeName();

		final CMQueryRow row = new QuerySpecsBuilder(view) //
				.select(codeAttributeName) //
				.from(newClass) //
				.where(attribute(canonicalAlias(newClass), codeAttributeName), EQUALS, cardAttributeToFind) //
				.run().getOnlyRow();

		assertThat(row.getCard(newClass).get(codeAttributeName), equalTo(cardAttributeToFind));
	}

}

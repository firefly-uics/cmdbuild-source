package integration.driver;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class SimpleQueryTest extends DriverFixture {

	private DBClass newClass;

	public SimpleQueryTest(final String driverBeanName) {
		super(driverBeanName);
	}

	@Before
	public void createNewClass() {
		newClass = driver.createClass(uniqueUUID(), null);
	}

	@Test
	public void simpleSubclassQuery() {
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
	public void simpleSuperclassQuery() {
		final DBClass root = newClass;
		final DBClass superNotRoot = driver.createClass(uniqueUUID(), root);
		final DBClass leafOfSuperNotRoot = driver.createClass(uniqueUUID(), superNotRoot);
		final DBClass leafOfRoot = driver.createClass(uniqueUUID(), root);
		final DBClass anotherLeafOfRoot = driver.createClass(uniqueUUID(), root);
		insertCardWithCode(leafOfSuperNotRoot, "foo");
		insertCardWithCode(leafOfRoot, "bar");
		insertCardWithCode(anotherLeafOfRoot, "baz");

		final CMQueryResult result = new QuerySpecsBuilder(view) //
				.select(root.getCodeAttributeName()) //
				.from(root) //
				.run();

		assertThat(result.size(), equalTo(3));
		for (CMQueryRow row : result) {
			final CMCard c = row.getCard(root);
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

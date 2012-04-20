package integration.driver;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause.Operator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class SimpleQueryTest extends DriverFixture {

	public SimpleQueryTest(final String driverBeanName) {
		super(driverBeanName);
	}

	/*
	 * Tests
	 */

	@Test
	public void simpleSubclassQuery() {
		final DBClass newClass = driver.createClass("A", null);
		final Object attr1Value = "Pizza";
		final Object attr2Value = "Calzone";

		DBCard.newInstance(driver, newClass).setCode(attr1Value).setDescription(attr2Value).save();

		final CMQueryResult result = new QuerySpecsBuilder(view)
			.select(newClass.getCodeAttributeName()).from(newClass).run();

		// TEST ATTRIBUTE_1 is extracted but not ATTRIBUTE_2
		final CMQueryRow firstRow = result.iterator().next();
		assertThat(result.size(), is(1));
		assertThat(firstRow.getCard(newClass).getCode(), is(attr1Value));
		try {
			firstRow.getCard(newClass).getDescription();
			fail("Value for description attribute should have not been loaded");
		} catch (final UnsupportedOperationException e) {
			assertThat(e.getMessage(), is("Not implemented"));
		}
	}

	@Test
	public void simpleSuperclassQuery() {
		// given
		final DBClass S1 = driver.createClass("S1", null);
		final DBClass S2 = driver.createClass("S2", S1);
		final DBClass A = driver.createClass("A", S2);
		final DBClass B = driver.createClass("B", S1);
		final DBClass C = driver.createClass("C", S1);
		insertCardWithCode(A, "A");
		insertCardWithCode(B, "B");
		insertCardWithCode(C, "C");

		// when
		final CMQueryResult result = new QuerySpecsBuilder(view)
			.select(S1.getCodeAttributeName()).from(S1).run();

		// then
		assertThat(result.size(), is(3));
		for (CMQueryRow row : result) {
			final CMCard c = row.getCard(S1);
			// the value was intentionally set to the class name
			final String expectedClassName = (String) c.getCode();
			assertThat(c.getType().getName(), is(expectedClassName));
		}
	}

	@Test
	public void simpleCountedQuery() {
		// given
		final int TOTAL_SIZE = 400000;
		final int OFFSET = 2000;
		final int LIMIT = 100;
		// final DBClass newClass = driver.createClass("Huge", null);
		// insertCards(newClass, TOTAL_SIZE);
		final DBClass newClass = driver.findClassByName("Huge");

		// when
		final CMQueryResult result = new QuerySpecsBuilder(view)
			.select(newClass.getCodeAttributeName()).from(newClass).offset(OFFSET).limit(LIMIT).run();

		// then
		assertThat(result.size(), is(LIMIT));
		assertThat(result.totalSize(), is(TOTAL_SIZE));
	}

	@Test
	public void singleWhereClause() {
		// given
		final DBClass newClass = driver.createClass("A", null);
		insertCards(newClass, 5);
		final Object cardAttributeToFind = "3";
		final String codeAttributeName = newClass.getCodeAttributeName();

		// when
		final CMQueryResult result = new QuerySpecsBuilder(view)
			.select(codeAttributeName)
			.from(newClass)
			.where(
				QueryAliasAttribute.attribute(Alias.canonicalAlias(newClass), codeAttributeName),
				Operator.EQUALS,
				cardAttributeToFind)
			.run();

		// then
		final CMQueryRow firstRow = result.iterator().next();
		assertThat(result.size(), is(1));
		assertThat(firstRow.getCard(newClass).get(codeAttributeName), is(cardAttributeToFind));
	}
}

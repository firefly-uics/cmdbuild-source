package integration.driver;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.AnyDomain.anyDomain;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause.Operator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class JoinQueryTest extends QueryTestFixture {

	private DBClass SRC;
	private DBClass DST;
	private DBClass DST1;
	private DBClass DST2;
	private DBDomain DOM;

	private static final Object DST1_ATTR1 = "DST1";
	private static final Object DST2_ATTR1 = "DST2";

	public JoinQueryTest(final String driverBeanName) {
		super(driverBeanName);
	}

	// TODO: create the structure for all the tests and roll it back at the end (needs checkpoints)
	@Before
	public void createDomainStructure() {
		SRC = driver.createClass("SRC", null);
		DST = driver.createClass("DST", null);
		DST1 = driver.createClass("DST1", DST);
		DST2 = driver.createClass("DST2", DST);
		DOM = driver.createDomain("DOM", SRC, DST);
	}

	@Test
	public void joinWithOneTargetClassOnly() {
		// given
		final DBCard src1 = insertCard(SRC, ATTRIBUTE_1, "SRC1");
		final DBCard dst1 = insertCard(DST1, ATTRIBUTE_1, DST1_ATTR1);
		final DBCard dst2 = insertCard(DST2, ATTRIBUTE_1, DST2_ATTR1);
		insertRelation(DOM, src1, dst1);
		insertRelation(DOM, src1, dst2);

		// when
		final CMQueryResult result = new QuerySpecsBuilder(driver, view)
				.select(
					attribute(SRC, ATTRIBUTE_2),
					"DST."+ATTRIBUTE_1)
				.from(SRC)
				.join(DST1, as("DST"), over(DOM))
				.where(attribute(SRC, ID_ATTRIBUTE), Operator.EQUALS, src1.getId())
				.run();

		// then
		assertThat(result.size(), is(1));

		final CMQueryRow firstRow = result.iterator().next();
		assertThat(firstRow.getCard(SRC).getId(), is(src1.getId()));
		assertThat(firstRow.getCard(DST).getId(), is(dst1.getId()));
		assertThat(firstRow.getCard(DST).get(ATTRIBUTE_1), is(DST1_ATTR1));
		//assertThat(firstRow.getRelation(DOM).getId(), is(not(nullValue())));
	}

	@Ignore // needs the delete operation to be implemented
	@Test
	public void joinDoesNotCountDeletedRelationsAndCards() {
		// given
		final DBCard src1 = insertCard(SRC, ATTRIBUTE_1, "SRC1");

		final DBCard dst1 = insertCard(DST1, ATTRIBUTE_1, DST1_ATTR1);
		final DBRelation rel1 = insertRelation(DOM, src1, dst1);
		deleteRelation(rel1);

		final DBCard dst2 = insertCard(DST2, ATTRIBUTE_1, DST2_ATTR1);
		insertRelation(DOM, src1, dst2);
		deleteCard(dst2);

		// when
		final CMQueryResult result = new QuerySpecsBuilder(driver, view)
			.select(
				attribute(SRC, ATTRIBUTE_2),
				"DST."+ATTRIBUTE_1)
			.from(SRC)
			.join(anyClass(), as("DST"), over(DOM))
			.where(attribute(SRC, ID_ATTRIBUTE), Operator.EQUALS, src1.getId())
			.run();

		// then
		assertThat(result.size(), is(0));
	}

	@Test
	public void joinWithAnyClassAndAnyDomain() {
		// given
		final DBCard src1 = insertCard(SRC, ATTRIBUTE_1, "SRC1");
		final DBCard dst1 = insertCard(DST1, ATTRIBUTE_1, DST1_ATTR1);
		final DBCard dst2 = insertCard(DST2, ATTRIBUTE_1, DST2_ATTR1);
		insertRelation(DOM, src1, dst1);
		insertRelation(DOM, src1, dst2);
		final DBDomain DOM2 = driver.createDomain("DOM2", DST2, SRC);
		insertRelation(DOM2, dst2, src1);

		Alias DOM_ALIAS = Alias.as("DOM");
		Alias DST_ALIAS = Alias.as("DST");

		// when
		final CMQueryResult result = new QuerySpecsBuilder(driver, view)
			.select(
				attribute(SRC, ATTRIBUTE_1),
				anyAttribute(DOM_ALIAS),
				attribute(DST_ALIAS, ATTRIBUTE_1))
			.from(SRC)
			.join(anyClass(), as(DST_ALIAS), over(anyDomain(), as(DOM_ALIAS)))
			.where(attribute(SRC, ID_ATTRIBUTE), Operator.EQUALS, src1.getId())
			.run();

		// then
		assertThat(result.size(), is(3));
	}

}

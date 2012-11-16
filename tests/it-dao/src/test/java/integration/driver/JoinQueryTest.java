package integration.driver;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.AnyDomain.anyDomain;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.cmdbuild.dao.driver.DBDriver.DomainDefinition;
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

import utils.DBFixture;

public class JoinQueryTest extends DBFixture {

	private DBClass SRC;
	private DBClass DST;

	private DBClass DST1;
	private DBClass DST2;

	private DBDomain DOM;

	private static final Object DST1_ATTR1 = "DST1";
	private static final Object DST2_ATTR1 = "DST2";

	@Before
	public void createDomainStructure() {
		SRC = rollbackDriver.createClass(uniqueUUID(), null);
		DST = rollbackDriver.createSuperClass(uniqueUUID(), null);
		DST1 = rollbackDriver.createClass(uniqueUUID(), DST);
		DST2 = rollbackDriver.createClass(uniqueUUID(), DST);
		DOM = rollbackDriver.createDomain(nnDomain(uniqueUUID(), SRC, DST));
	}

	@Test
	public void joinWithOneTargetClassOnly() {
		final DBCard src1 = insertCardWithCode(SRC, "SRC1");
		final DBCard dst1 = insertCardWithCode(DST1, DST1_ATTR1);
		final DBCard dst2 = insertCardWithCode(DST2, DST2_ATTR1);
		insertRelation(DOM, src1, dst1);
		insertRelation(DOM, src1, dst2);

		final Alias DST_ALIAS = Alias.as("DST");

		final CMQueryResult result = new QuerySpecsBuilder(dbView) //
				.select(descriptionAttribute(SRC), codeAttribute(DST_ALIAS, DST1)) //
				.from(SRC) //
				.join(DST1, as(DST_ALIAS), over(DOM)) //
				.where(keyAttribute(SRC), Operator.EQUALS, src1.getId()) //
				.run();

		assertThat(result.size(), is(1));

		final CMQueryRow firstRow = result.iterator().next();
		assertThat(firstRow.getCard(SRC).getId(), is(src1.getId()));
		assertThat(firstRow.getCard(DST_ALIAS).getId(), is(dst1.getId()));
		assertThat(firstRow.getCard(DST_ALIAS).getCode(), is(DST1_ATTR1));
		// assertThat(firstRow.getRelation(DOM).getId(), is(not(nullValue())));
	}

	@Ignore
	// needs the delete operation to be implemented
	@Test
	public void joinDoesNotCountDeletedRelationsAndCards() {
		// given
		final DBCard src1 = insertCardWithCode(SRC, "SRC1");

		final DBCard dst1 = insertCardWithCode(DST1, DST1_ATTR1);
		final DBRelation rel1 = insertRelation(DOM, src1, dst1);
		deleteRelation(rel1);

		final DBCard dst2 = insertCardWithCode(DST2, DST2_ATTR1);
		insertRelation(DOM, src1, dst2);
		deleteCard(dst2);

		// when
		final CMQueryResult result = new QuerySpecsBuilder(dbView) //
				.select(descriptionAttribute(SRC), codeAttribute(DST)) //
				.from(SRC) //
				.join(anyClass(), as("DST"), over(DOM)) //
				.where(keyAttribute(SRC), Operator.EQUALS, src1.getId()) //
				.run();

		// then
		assertThat(result.size(), is(0));
	}

	@Test
	public void joinWithAnyClassAndAnyDomain() {
		final DBCard src1 = insertCardWithCode(SRC, "SRC1");
		final DBCard dst1 = insertCardWithCode(DST1, DST1_ATTR1);
		final DBCard dst2 = insertCardWithCode(DST2, DST2_ATTR1);
		insertRelation(DOM, src1, dst1);
		insertRelation(DOM, src1, dst2);
		final DBDomain DOM2 = rollbackDriver.createDomain(nnDomain(uniqueUUID(), DST2, SRC));
		insertRelation(DOM2, dst2, src1);

		final Alias DOM_ALIAS = Alias.as("DOM");
		final Alias DST_ALIAS = Alias.as("DST");

		final CMQueryResult result = new QuerySpecsBuilder(dbView) //
				.select(codeAttribute(SRC), anyAttribute(DOM_ALIAS), codeAttribute(DST_ALIAS, DST)) //
				.from(SRC) //
				.join(anyClass(), as(DST_ALIAS), over(anyDomain(), as(DOM_ALIAS))) //
				.where(keyAttribute(SRC), Operator.EQUALS, src1.getId()) //
				.run();

		assertThat(result.size(), is(3));
	}

	/*
	 * Utilities
	 */

	private static DomainDefinition nnDomain(final String string, final DBClass class1, final DBClass class2) {
		return new DomainDefinition() {

			@Override
			public String getName() {
				return string;
			}

			@Override
			public String getDirectDescription() {
				return EMPTY;
			}

			@Override
			public String getInverseDescription() {
				return EMPTY;
			}

			@Override
			public DBClass getClass1() {
				return class1;
			}

			@Override
			public DBClass getClass2() {
				return class2;
			}

			@Override
			public String getCardinality() {
				return "N:N";
			}

		};
	}

}

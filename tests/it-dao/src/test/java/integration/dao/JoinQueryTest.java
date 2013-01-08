package integration.dao;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.AnyDomain.anyDomain;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
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
		SRC = dbDataView().createClass(newClass("src"));
		DST = dbDataView().createClass(newSuperClass("dst"));
		DST1 = dbDataView().createClass(newClass("dst1", DST));
		DST2 = dbDataView().createClass(newClass("dst2", DST));
		DOM = dbDataView().createDomain(newDomain("dom", SRC, DST));
	}

	@Test
	public void joinWithOneTargetClassOnly() {
		final DBCard src1 = dbDataView().newCard(SRC) //
				.set(SRC.getCodeAttributeName(), "SRC1") //
				.save();
		final DBCard dst1 = dbDataView().newCard(DST1) //
				.set(DST1.getCodeAttributeName(), DST1_ATTR1) //
				.save();
		final DBCard dst2 = dbDataView().newCard(DST2) //
				.set(DST2.getCodeAttributeName(), DST2_ATTR1) //
				.save();
		insertRelation(DOM, src1, dst1);
		insertRelation(DOM, src1, dst2);

		final Alias DST_ALIAS = Alias.as("DST");

		final CMQueryResult result = dbDataView() //
				.select(descriptionAttribute(SRC), codeAttribute(DST_ALIAS, DST1)) //
				.from(SRC) //
				.join(DST1, as(DST_ALIAS), over(DOM)) //
				.where(condition(keyAttribute(SRC), eq(src1.getId()))) //
				.run();

		assertThat(result.size(), is(1));

		final CMQueryRow firstRow = result.iterator().next();
		assertThat(firstRow.getCard(SRC).getId(), is(src1.getId()));
		assertThat(firstRow.getCard(DST_ALIAS).getId(), is(dst1.getId()));
		assertThat(firstRow.getCard(DST_ALIAS).getCode(), is(DST1_ATTR1));
		// assertThat(firstRow.getRelation(DOM).getId(), is(not(nullValue())));
	}

	@Ignore("needs the delete operation to be implemented")
	@Test
	public void joinDoesNotCountDeletedRelationsAndCards() {
		// given
		final DBCard src1 = dbDataView().newCard(SRC) //
				.set(SRC.getCodeAttributeName(), "SRC1") //
				.save();
		final DBCard dst1 = dbDataView().newCard(SRC) //
				.set(DST1.getCodeAttributeName(), DST1_ATTR1) //
				.save();
		final DBRelation rel1 = insertRelation(DOM, src1, dst1);
		// TODO use the dataview
		dbDriver().delete(rel1);

		final DBCard dst2 = dbDataView().newCard(SRC) //
				.set(DST2.getCodeAttributeName(), DST2_ATTR1) //
				.save();
		insertRelation(DOM, src1, dst2);
		// TODO use the dataview
		dbDriver().delete(dst2);

		// when
		final CMQueryResult result = dbDataView() //
				.select(descriptionAttribute(SRC), codeAttribute(DST)) //
				.from(SRC) //
				.join(anyClass(), as("DST"), over(DOM)) //
				.where(condition(keyAttribute(SRC), eq(src1.getId()))) //
				.run();

		// then
		assertThat(result.size(), is(0));
	}

	@Test
	public void joinWithAnyClassAndAnyDomain() {
		final DBCard src1 = dbDataView().newCard(SRC) //
				.set(SRC.getCodeAttributeName(), "SRC1") //
				.save();
		final DBCard dst1 = dbDataView().newCard(DST1) //
				.set(DST1.getCodeAttributeName(), DST1_ATTR1) //
				.save();
		final DBCard dst2 = dbDataView().newCard(DST2) //
				.set(DST2.getCodeAttributeName(), DST2_ATTR1) //
				.save();
		insertRelation(DOM, src1, dst1);
		insertRelation(DOM, src1, dst2);
		final DBDomain DOM2 = dbDriver().createDomain(newDomain("foo", DST2, SRC));
		insertRelation(DOM2, dst2, src1);

		final Alias DOM_ALIAS = Alias.as("DOM");
		final Alias DST_ALIAS = Alias.as("DST");

		final CMQueryResult result = dbDataView() //
				.select(codeAttribute(SRC), anyAttribute(DOM_ALIAS), codeAttribute(DST_ALIAS, DST)) //
				.from(SRC) //
				.join(anyClass(), as(DST_ALIAS), over(anyDomain(), as(DOM_ALIAS))) //
				.where(condition(keyAttribute(SRC), eq(src1.getId()))) //
				.run();

		assertThat(result.size(), is(3));
	}

	/*
	 * Utilities
	 */

	private DBRelation insertRelation(final DBDomain d, final DBCard c1, final DBCard c2) {
		return (DBRelation) dbDataView().newRelation(d) //
				.setCard1(c1) //
				.setCard2(c2) //
				.save();
	}

}

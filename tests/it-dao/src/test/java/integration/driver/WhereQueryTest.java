package integration.driver;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.GreatherThanOperatorAndValue.gt;
import static org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue.lt;
import static org.cmdbuild.dao.query.clause.where.NotWhereClause.not;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.junit.Before;
import org.junit.Test;

import utils.DBFixture;

public class WhereQueryTest extends DBFixture {

	private static final String CLASS_NAME = "foo";

	private DBClass clazz;

	@Before
	public void createData() throws Exception {
		clazz = dbDriver().createClass(newClass(CLASS_NAME, null));
	}

	@Test
	public void singleCardRespectingSimpleCondition() throws Exception {
		// given
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("foo") //
				.save();
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("baz") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(codeAttribute(clazz), eq("foo"))) //
				.run();

		// then
		final CMQueryRow row = result.getOnlyRow();
		final CMCard card = row.getCard(clazz);
		assertThat((String) card.getCode(), equalTo("foo"));
	}

	@Test
	public void singleCardRespectingBothConditions() throws Exception {
		// given
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("foo") //
				.setDescription("bar") //
				.save();
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("foo") //
				.setDescription("baz") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(and( //
						condition(codeAttribute(clazz), eq("foo")), //
						condition(descriptionAttribute(clazz), eq("bar")))) //
				.run();

		// then
		final CMQueryRow row = result.getOnlyRow();
		final CMCard card = row.getCard(clazz);
		assertThat((String) card.getCode(), equalTo("foo"));
		assertThat((String) card.getDescription(), equalTo("bar"));
	}

	@Test
	public void moreCardsRespectingOrConditions() throws Exception {
		// given
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("foo") //
				.save();
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("bar") //
				.save();
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("baz") //
				.setDescription("baz") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(or( //
						condition(codeAttribute(clazz), eq("foo")), //
						condition(codeAttribute(clazz), eq("bar")))) //
				.run();

		// then
		assertThat(result.size(), equalTo(2));
		final Iterator<CMQueryRow> rows = result.iterator();
		assertThat((String) rows.next().getCard(clazz).getCode(), equalTo("foo"));
		assertThat((String) rows.next().getCard(clazz).getCode(), equalTo("bar"));
	}

	@Test
	public void singleCardRespectingNotCondition() throws Exception {
		// given
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("foo") //
				.save();
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(not( //
				condition(codeAttribute(clazz), eq("foo")))) //
				.run().getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("bar"));
	}

	@Test
	public void noResultWithMoreThanTwoAndConditions() throws Exception {
		// given
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("foo") //
				.setDescription("foo") //
				.save();
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(and( //
						condition(codeAttribute(clazz), eq("foo")), //
						condition(descriptionAttribute(clazz), eq("foo")), //
						condition(codeAttribute(clazz), eq("bar")))) //
				.run();

		// then
		assertThat(result.size(), equalTo(0));
	}

	@Test
	public void singleResultWithCompositeConditions() throws Exception {
		// given
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("foo") //
				.setDescription("foo") //
				.save();
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(and(condition(codeAttribute(clazz), eq("foo")), //
						not(condition(descriptionAttribute(clazz), eq("fake"))))) //
				.run().getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("foo"));
	}

	@Test
	public void whereClausesWithGreatherThanOperatorWork() {
		// given
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("foo") //
				.setDescription("foo") //
				.save();
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(codeAttribute(clazz), gt("f"))) //
				.run().getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("foo"));
	}

	@Test
	public void whereClausesWithLessThanOperatorWork() {
		// given
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("foo") //
				.setDescription("foo") //
				.save();
		DBCard.newInstance(dbDriver(), clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(codeAttribute(clazz), lt("e"))) //
				.run().getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("bar"));
	}

}

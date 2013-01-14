package integration.logic.data.filter;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static integration.logic.data.DataDefinitionLogicTest.newDomain;
import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.QueryOptions;
import org.json.JSONObject;
import org.junit.Test;

public class RelationFilterTest extends FilteredCardsFixture {

	private CMClass foo;
	private CMClass bar;
	private CMClass baz;

	private CMDomain foo_bar;
	private CMDomain foo_baz;

	@Override
	protected void createClassesAndDomains() {
		// classes
		foo = dataDefinitionLogic().createOrUpdate(a(newClass("foo") //
				.thatIsActive(true) //
				.thatIsHoldingHistory(true)));
		bar = dataDefinitionLogic().createOrUpdate(a(newClass("bar") //
				.thatIsActive(true) //
				.thatIsHoldingHistory(true)));
		baz = dataDefinitionLogic().createOrUpdate(a(newClass("baz") //
				.thatIsActive(true) //
				.thatIsHoldingHistory(true)));

		// domains
		foo_bar = dataDefinitionLogic().createOrUpdate(a(newDomain("foo_bar") //
				.withIdClass1(foo.getId()) //
				.withIdClass2(bar.getId()) //
				.thatIsActive(true)));
		foo_baz = dataDefinitionLogic().createOrUpdate(a(newDomain("foo_baz") //
				.withIdClass1(foo.getId()) //
				.withIdClass2(baz.getId()) //
				.thatIsActive(true)));
	}

	@Override
	protected void initializeDatabaseData() {
		// nothing to do
	}

	@Override
	protected void clearAndDeleteClassesAndDomains() {
		dbDataView().clear(foo_baz);
		dbDataView().deleteDomain(foo_baz);

		dbDataView().clear(foo_bar);
		dbDataView().deleteDomain(foo_bar);

		dbDataView().clear(baz);
		dbDataView().deleteClass(baz);

		dbDataView().clear(bar);
		dbDataView().deleteClass(bar);

		dbDataView().clear(foo);
		dbDataView().deleteClass(foo);
	}

	@Test
	public void fetchingCardsWithAnyRelationOverSingleDomainButNothingIsFound() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().newCard(foo).setCode("foo_1").save();
		final CMCard bar_1 = dbDataView().newCard(bar).setCode("bar_1").save();
		dbDataView().newRelation(foo_bar).setCard1(foo_1).setCard2(bar_1).save();

		// when
		final Iterable<CMCard> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				filter(anyRelation(ofDomain(foo_baz), overClass(baz))));

		// then
		assertThat(isEmpty(cards), equalTo(true));
	}

	@Test
	public void fetchingCardsWithAnyRelationOverSingleDomainOneCardThatHaveTwoRelationsIsFound() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().newCard(foo).setCode("foo_1").save();
		final CMCard bar_1 = dbDataView().newCard(bar).setCode("bar_1").save();
		final CMCard bar_2 = dbDataView().newCard(bar).setCode("bar_2").save();
		dbDataView().newRelation(foo_bar).setCard1(foo_1).setCard2(bar_1).save();
		dbDataView().newRelation(foo_bar).setCard1(foo_1).setCard2(bar_2).save();

		// when
		final Iterable<CMCard> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				filter(anyRelation(ofDomain(foo_bar), overClass(bar))));

		// then
		assertThat(size(cards), equalTo(1));
	}

	/*
	 * Utilities
	 */

	private String forClass(final CMClass clazz) {
		return clazz.getName();
	}

	private QueryOptions filter(final QueryOptions queryOptions) {
		return queryOptions;
	}

	private QueryOptions anyRelation(final CMDomain domain, final CMClass clazz) throws Exception {
		final String source = (domain.getClass1().equals(clazz)) ? "_2" : "_1";
		return QueryOptions.newQueryOption() //
				.filter(json(format("{relation:[{domain: %s, src: %s, type: any}]}", //
						domain.getName(), //
						source))) //
				.build();
	}

	private JSONObject json(final String source) throws Exception {
		return new JSONObject(source);
	}

	private CMDomain ofDomain(final CMDomain domain) {
		return domain;
	}

	private final CMClass overClass(final CMClass clazz) {
		return clazz;
	}

}

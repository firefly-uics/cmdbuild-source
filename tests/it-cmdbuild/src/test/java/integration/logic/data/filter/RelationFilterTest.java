package integration.logic.data.filter;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static integration.logic.data.DataDefinitionLogicTest.newDomain;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.join;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_CLASSNAME_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_ID_KEY;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.QueryOptions.QueryOptionsBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.google.common.collect.Lists;

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
				.thatIsActive(true)));
		bar = dataDefinitionLogic().createOrUpdate(a(newClass("bar") //
				.thatIsActive(true)));
		baz = dataDefinitionLogic().createOrUpdate(a(newClass("baz") //
				.thatIsActive(true)));

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
		dbDataView().delete(foo_baz);

		dbDataView().clear(foo_bar);
		dbDataView().delete(foo_bar);

		dbDataView().clear(baz);
		dbDataView().delete(baz);

		dbDataView().clear(bar);
		dbDataView().delete(bar);

		dbDataView().clear(foo);
		dbDataView().delete(foo);
	}

	@Test
	public void fetchingCardsWithAnyRelationOverSingleDomainButNothingIsFound() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().createCardFor(foo).setCode("foo_1").save();
		final CMCard bar_1 = dbDataView().createCardFor(bar).setCode("bar_1").save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_1).setCard2(bar_1).save();

		// when
		final Iterable<CMCard> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				query(anyRelation(withDomain(foo_baz), withSourceClass(foo))));

		// then
		assertThat(isEmpty(cards), equalTo(true));
	}

	@Test
	public void fetchingCardsWithAnyRelationOverSingleDomainOneCardThatHaveTwoRelationsIsFound() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().createCardFor(foo).setCode("foo_1").save();
		final CMCard bar_1 = dbDataView().createCardFor(bar).setCode("bar_1").save();
		final CMCard bar_2 = dbDataView().createCardFor(bar).setCode("bar_2").save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_1).setCard2(bar_1).save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_1).setCard2(bar_2).save();

		// when
		final Iterable<CMCard> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				query(anyRelation(withDomain(foo_bar), withSourceClass(foo))));

		// then
		assertThat(size(cards), equalTo(1));
	}

	@Test
	public void fetchingCardsWithAnyRelationOverSingleDomainTwoCardsAreFoundAndSorted() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().createCardFor(foo).setCode("foo_1").save();
		final CMCard foo_2 = dbDataView().createCardFor(foo).setCode("foo_2").save();
		final CMCard bar_1 = dbDataView().createCardFor(bar).setCode("bar_1").save();
		final CMCard bar_2 = dbDataView().createCardFor(bar).setCode("bar_2").save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_1).setCard2(bar_1).save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_2).setCard2(bar_2).save();

		// when
		final Iterable<CMCard> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				query(anyRelation(withDomain(foo_bar), withSourceClass(foo)), sortBy("Code", "DESC")));

		// then
		assertThat(size(cards), equalTo(2));
		assertThat((String) get(cards, 0).getCode(), equalTo("foo_2"));
		assertThat((String) get(cards, 1).getCode(), equalTo("foo_1"));
	}

	@Test
	public void fetchingCardsWithRelationOverSingleDomainLookingForSpecificDestinationCards() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().createCardFor(foo).setCode("foo_1").save();
		final CMCard foo_2 = dbDataView().createCardFor(foo).setCode("foo_2").save();
		final CMCard foo_3 = dbDataView().createCardFor(foo).setCode("foo_3").save();
		final CMCard bar_1 = dbDataView().createCardFor(bar).setCode("bar_1").save();
		final CMCard bar_2 = dbDataView().createCardFor(bar).setCode("bar_2").save();
		final CMCard bar_3 = dbDataView().createCardFor(bar).setCode("bar_3").save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_1).setCard2(bar_1).save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_2).setCard2(bar_2).save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_3).setCard2(bar_3).save();

		// when
		final Iterable<CMCard> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				query(anyRelated(withDomain(foo_bar), withSourceClass(foo), card(bar_1), card(bar_3))));

		// then
		assertThat(size(cards), equalTo(2));
		assertThat((String) get(cards, 0).getCode(), equalTo("foo_1"));
		assertThat((String) get(cards, 1).getCode(), equalTo("foo_3"));
	}

	@Test
	public void fetchingCardsWithNoRelationOverSingleDomain() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().createCardFor(foo).setCode("foo_1").save();
		final CMCard foo_2 = dbDataView().createCardFor(foo).setCode("foo_2").save();
		final CMCard bar_1 = dbDataView().createCardFor(bar).setCode("bar_1").save();
		final CMCard baz_1 = dbDataView().createCardFor(baz).setCode("baz_1").save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_1).setCard2(bar_1).save();
		dbDataView().createRelationFor(foo_baz).setCard1(foo_2).setCard2(baz_1).save();

		// when
		final Iterable<CMCard> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				query(notRelated(withDomain(foo_baz), withSourceClass(foo))));

		// then
		assertThat(size(cards), equalTo(1));
		assertThat((String) get(cards, 0).getCode(), equalTo("foo_1"));
	}

	/*
	 * Utilities
	 */

	private CMCard card(final CMCard card) {
		return card;
	}

	private String forClass(final CMClass clazz) {
		return clazz.getName();
	}

	private QueryOptions query(final QueryOptions... queryOptions) {
		final QueryOptionsBuilder builder = QueryOptions.newQueryOption();
		for (final QueryOptions qo : queryOptions) {
			builder //
			.filter(qo.getFilter()) //
					.orderBy(qo.getSorters());
		}
		return builder.build();
	}

	private QueryOptions anyRelation(final CMDomain domain, final CMClass clazz) throws Exception {
		final CMClass destination = (domain.getClass1().equals(clazz)) ? domain.getClass2() : domain.getClass1();
		return QueryOptions.newQueryOption() //
				.filter(json(format("{relation:[{domain: %s, source: %s, destination: %s, type: any}]}", //
						domain.getName(), //
						clazz.getName(), //
						destination.getName()))) //
				.build();
	}

	private QueryOptions anyRelated(final CMDomain domain, final CMClass clazz, final CMCard... cards) throws Exception {
		final CMClass destination = (domain.getClass1().equals(clazz)) ? domain.getClass2() : domain.getClass1();
		final List<String> jsonCardObjects = Lists.newArrayList();
		for (final CMCard card : cards) {
			jsonCardObjects.add(format("{" + RELATION_CARD_ID_KEY + ": %d, " + RELATION_CARD_CLASSNAME_KEY + ": %s}",
					card.getId(), card.getType().getName()));
		}
		final String jsonCards = join(jsonCardObjects, ",");
		return QueryOptions.newQueryOption() //
				.filter(json(format("{relation:[{domain: %s, source: %s, destination: %s, type: oneof, cards: [%s]}]}", //
						domain.getName(), //
						clazz.getName(), //
						destination.getName(), //
						jsonCards))) //
				.build();
	}

	private QueryOptions notRelated(final CMDomain domain, final CMClass clazz) throws Exception {
		final CMClass destination = (domain.getClass1().equals(clazz)) ? domain.getClass2() : domain.getClass1();
		return QueryOptions.newQueryOption() //
				.filter(json(format("{relation:[{domain: %s, source: %s, destination: %s, type: noone}]}", //
						domain.getName(), //
						clazz.getName(), //
						destination.getName()))) //
				.build();
	}

	private QueryOptions sortBy(final String attributeName, final String direction) throws Exception {
		return QueryOptions.newQueryOption() //
				.orderBy(new JSONArray() {
					{
						put(json(format("{property: %s, direction: %s}", attributeName, direction)));
					}
				}).build();
	}

	private JSONObject json(final String source) throws Exception {
		return new JSONObject(source);
	}

	private CMDomain withDomain(final CMDomain domain) {
		return domain;
	}

	private final CMClass withSourceClass(final CMClass clazz) {
		return clazz;
	}

}

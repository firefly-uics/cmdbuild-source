package integration.logic.data;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utils.IntegrationTestUtils.newClass;
import static utils.IntegrationTestUtils.newDomain;
import static utils.IntegrationTestUtils.withIdentifier;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

import com.google.common.collect.Iterables;

public class DataAccessLogicTest extends IntegrationTestBase {

	private DataAccessLogic dataAccessLogic;

	@Before
	public void createDataDefinitionLogic() throws Exception {
		dataAccessLogic = new DataAccessLogic(dbDataView());
	}

	@Test
	public void shouldNotRetrieveCardsIfNullClassName() throws Exception {
		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), new JSONObject());
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(null, queryOptions).getPaginatedCards();

		// then
		assertTrue(isEmpty(fetchedCards));
	}

	@Test
	public void shouldNotRetrieveCardsIfNotExistentClassName() throws Exception {
		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), new JSONObject());
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards("not_existent_class_name", queryOptions)
				.getPaginatedCards();

		// then
		assertTrue(isEmpty(fetchedCards));
	}

	@Test
	public void shouldRetrieveAllCardsIfFilterIsNull() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), null);
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				queryOptions).getPaginatedCards();

		// then
		assertEquals(size(fetchedCards), 3);
	}

	@Test
	public void shouldRetrieveAllCardsIfFilterIsEmpty() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), new JSONObject());
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				queryOptions).getPaginatedCards();

		// then
		assertEquals(size(fetchedCards), 3);
	}

	@Test
	public void shouldSortSuccessfullyFetchedCards() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("zzz") //
				.setDescription("description_baz") //
				.save();
		final JSONArray sortersArray = new JSONArray();
		sortersArray.put(new JSONObject("{property: Code, direction: ASC}"));

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, sortersArray, null);
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				queryOptions).getPaginatedCards();

		// then
		assertEquals(get(fetchedCards, 0).getCode(), "bar");
		assertEquals(get(fetchedCards, 1).getCode(), "baz");
		assertEquals(get(fetchedCards, 2).getCode(), "foo");
		assertEquals(get(fetchedCards, 3).getCode(), "zzz");
	}

	@Test
	public void shouldFetchCardsWithFilterDefinedButFullTextQueryEmpty() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("zzz") //
				.setDescription("description_baz") //
				.save();
		final JSONObject filterObject = new JSONObject(
				"{attribute: {simple: {attribute: Code, operator: equal, value:[foo]}}}");

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), filterObject);
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				queryOptions).getPaginatedCards();

		// then
		assertEquals(size(fetchedCards), 1);
		assertEquals(get(fetchedCards, 0).getCode(), "foo");
	}

	@Test
	public void shouldFetchCardsWithFilterDefinedAndFullTextQuery() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("zzz") //
				.setDescription("description_baz") //
				.save();
		final JSONObject filterObject = new JSONObject(
				"{query: dESc, attribute: {simple: {attribute: Code, operator: equal, value:['foo']}}}");

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), filterObject);
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				queryOptions).getPaginatedCards();

		// then
		assertEquals(size(fetchedCards), 1);
		assertEquals(get(fetchedCards, 0).getCode(), "foo");
	}

	@Test
	public void shouldFetchCardsWithFullTextQueryFilterAndMultipleAttributeSorting() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_aaa") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bbb") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_zzz") //
				.save();
		final JSONObject filterObject = new JSONObject("{query: CRiptioN}");
		final JSONArray sortersArray = new JSONArray();
		sortersArray.put(new JSONObject("{property: Code, direction: DESC}"));
		sortersArray.put(new JSONObject("{property: Description, direction: ASC}"));

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, sortersArray, filterObject);
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				queryOptions).getPaginatedCards();

		// then
		assertEquals(size(fetchedCards), 3);
		assertEquals(get(fetchedCards, 0).getCode(), "baz");
		assertEquals(get(fetchedCards, 1).getCode(), "bar");
		assertEquals(get(fetchedCards, 1).getDescription(), "description_aaa");
		assertEquals(get(fetchedCards, 2).getCode(), "bar");
		assertEquals(get(fetchedCards, 2).getDescription(), "description_bbb");
	}

	@Test
	public void shouldPaginateSuccessfully() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_aaa") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bbb") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_zzz") //
				.save();

		// when
		final Iterable<CMCard> firstPageOfCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				createQueryOptions(3, 0, null, null)).getPaginatedCards();
		final Iterable<CMCard> secondPageOfCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				createQueryOptions(3, 3, null, null)).getPaginatedCards();

		// then
		assertEquals(size(firstPageOfCards), 3);
		assertEquals(size(secondPageOfCards), 1);
	}

	@Test
	public void shouldFetchCardsWithAndConditionsInFilter() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_aaa") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bbb") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_zzz") //
				.save();
		final JSONObject filterObject = new JSONObject(
				"{attribute: {and: [{simple: {attribute: Code, operator: notcontain, value:['bar']}}, "
						+ "{simple: {attribute: Description, operator: contain, value: ['sc_f']}}]}}");

		// when
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				createQueryOptions(10, 0, null, filterObject)).getPaginatedCards();

		// then
		assertEquals(size(fetchedCards), 1);
		assertEquals(get(fetchedCards, 0).getCode(), "foo");
	}

	private QueryOptions createQueryOptions(final int limit, final int offset, final JSONArray sorters,
			final JSONObject filter) {
		return QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(filter) //
				.build();
	}

	@Test
	public void shouldFetchAllCardsRelatedToASpecifiedCard() throws Exception {
		// given
		final DBClass srcClass = dbDataView().create(newClass("src"));
		final DBClass dstClass = dbDataView().create(newClass("dst"));
		final DBDomain dom = dbDataView().create(newDomain(withIdentifier("dom"), srcClass, dstClass));
		final CMCard srcCard = dbDataView().createCardFor(srcClass) //
				.setCode("src1") //
				.save();
		dbDataView().createCardFor(srcClass) //
				.setCode("src2") //
				.save();
		for (int i = 0; i < 10; i++) {
			final CMCard dstCard = dbDataView().createCardFor(dstClass) //
					.setCode("dst" + i) //
					.save();
			dbDataView().createRelationFor(dom).setCard1(srcCard).setCard2(dstCard).save();
		}
		final Card card = new Card(srcClass.getIdentifier().getLocalName(), srcCard.getId());
		final DomainWithSource domWithSource = DomainWithSource.create(dom.getId(), "_1");

		// when
		final GetRelationListResponse response1 = dataAccessLogic.getRelationList(card, domWithSource);

		final QueryOptions options2 = QueryOptions.newQueryOption().limit(5).build();
		final GetRelationListResponse response2 = dataAccessLogic.getRelationList(card, domWithSource, options2);

		final QueryOptions options3 = QueryOptions.newQueryOption().limit(5)
				.filter(new JSONObject("{query: no_card_match_this_filter}")).build();
		final GetRelationListResponse response3 = dataAccessLogic.getRelationList(card, domWithSource, options3);

		// then
		assertEquals(10, response1.getTotalNumberOfRelations());
		assertEquals(5, Iterables.size(response2.iterator().next()));
		assertFalse(response3.iterator().hasNext());

	}

}

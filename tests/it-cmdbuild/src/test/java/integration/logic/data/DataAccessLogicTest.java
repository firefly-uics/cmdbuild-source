package integration.logic.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static utils.IntegrationTestUtils.newClass;

import java.util.List;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

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
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(null, queryOptions);

		// then
		assertTrue(fetchedCards.isEmpty());
	}

	@Test
	public void shouldNotRetrieveCardsIfNotExistentClassName() throws Exception {
		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), new JSONObject());
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards("not_existent_class_name", queryOptions);

		// then
		assertTrue(fetchedCards.isEmpty());
	}

	@Test
	public void shouldRetrieveAllCardsIfFilterIsNull() throws Exception {
		// given
		final DBClass newClass = dbDataView().createClass(newClass("test"));

		final CMCard card1 = dbDataView().newCard(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		final CMCard card2 = dbDataView().newCard(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		final CMCard card3 = dbDataView().newCard(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), null);
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getName(), queryOptions);

		// then
		assertEquals(fetchedCards.size(), 3);
	}

	@Test
	public void shouldRetrieveAllCardsIfFilterIsEmpty() throws Exception {
		// given
		final DBClass newClass = dbDataView().createClass(newClass("test"));

		final CMCard card1 = dbDataView().newCard(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		final CMCard card2 = dbDataView().newCard(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		final CMCard card3 = dbDataView().newCard(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), new JSONObject());
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getName(), queryOptions);

		// then
		assertEquals(fetchedCards.size(), 3);
	}

	@Test
	public void shouldSortSuccessfullyFetchedCards() throws Exception {
		// given
		final DBClass newClass = dbDataView().createClass(newClass("test"));

		final CMCard card1 = dbDataView().newCard(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		final CMCard card2 = dbDataView().newCard(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		final CMCard card3 = dbDataView().newCard(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();
		final CMCard card4 = dbDataView().newCard(newClass) //
				.setCode("zzz") //
				.setDescription("description_baz") //
				.save();
		final JSONArray sortersArray = new JSONArray();
		sortersArray.put(new JSONObject("{property: Code, direction: ASC}"));

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, sortersArray, null);
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getName(), queryOptions);

		// then
		assertEquals(fetchedCards.get(0).getCode(), "bar");
		assertEquals(fetchedCards.get(1).getCode(), "baz");
		assertEquals(fetchedCards.get(2).getCode(), "foo");
		assertEquals(fetchedCards.get(3).getCode(), "zzz");
	}

	@Test
	public void shouldFetchCardsWithFilterDefinedButFullTextQueryEmpty() throws Exception {
		// given
		final DBClass newClass = dbDataView().createClass(newClass("test"));

		final CMCard card1 = dbDataView().newCard(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		final CMCard card2 = dbDataView().newCard(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		final CMCard card3 = dbDataView().newCard(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();
		final CMCard card4 = dbDataView().newCard(newClass) //
				.setCode("zzz") //
				.setDescription("description_baz") //
				.save();
		final JSONObject filterObject = new JSONObject(
				"{attribute: {simple: {attribute: Code, operator: equal, value:[foo]}}}");

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), filterObject);
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getName(), queryOptions);

		// then
		assertEquals(fetchedCards.size(), 1);
		assertEquals(fetchedCards.get(0).getCode(), "foo");
	}

	@Test
	public void shouldFetchCardsWithFilterDefinedAndFullTextQuery() throws Exception {
		// given
		final DBClass newClass = dbDataView().createClass(newClass("test"));

		final CMCard card1 = dbDataView().newCard(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		final CMCard card2 = dbDataView().newCard(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		final CMCard card3 = dbDataView().newCard(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();
		final CMCard card4 = dbDataView().newCard(newClass) //
				.setCode("zzz") //
				.setDescription("description_baz") //
				.save();
		final JSONObject filterObject = new JSONObject(
				"{query: dESc, attribute: {simple: {attribute: Code, operator: equal, value:['foo']}}}");

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), filterObject);
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getName(), queryOptions);

		// then
		assertEquals(fetchedCards.size(), 1);
		assertEquals(fetchedCards.get(0).getCode(), "foo");
	}

	@Test
	public void shouldFetchCardsWithFullTextQueryFilterAndMultipleAttributeSorting() throws Exception {
		// given
		final DBClass newClass = dbDataView().createClass(newClass("test"));

		final CMCard card1 = dbDataView().newCard(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		final CMCard card2 = dbDataView().newCard(newClass) //
				.setCode("bar") //
				.setDescription("description_aaa") //
				.save();
		final CMCard card3 = dbDataView().newCard(newClass) //
				.setCode("bar") //
				.setDescription("description_bbb") //
				.save();
		final CMCard card4 = dbDataView().newCard(newClass) //
				.setCode("baz") //
				.setDescription("description_zzz") //
				.save();
		final JSONObject filterObject = new JSONObject("{query: CRiptioN}");
		final JSONArray sortersArray = new JSONArray();
		sortersArray.put(new JSONObject("{property: Code, direction: DESC}"));
		sortersArray.put(new JSONObject("{property: Description, direction: ASC}"));

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, sortersArray, filterObject);
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getName(), queryOptions);

		// then
		assertEquals(fetchedCards.size(), 3);
		assertEquals(fetchedCards.get(0).getCode(), "baz");
		assertEquals(fetchedCards.get(1).getCode(), "bar");
		assertEquals(fetchedCards.get(1).getDescription(), "description_aaa");
		assertEquals(fetchedCards.get(2).getCode(), "bar");
		assertEquals(fetchedCards.get(2).getDescription(), "description_bbb");
	}

	@Test
	public void shouldPaginateSuccessfully() throws Exception {
		// given
		final DBClass newClass = dbDataView().createClass(newClass("test"));

		final CMCard card1 = dbDataView().newCard(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		final CMCard card2 = dbDataView().newCard(newClass) //
				.setCode("bar") //
				.setDescription("description_aaa") //
				.save();
		final CMCard card3 = dbDataView().newCard(newClass) //
				.setCode("bar") //
				.setDescription("description_bbb") //
				.save();
		final CMCard card4 = dbDataView().newCard(newClass) //
				.setCode("baz") //
				.setDescription("description_zzz") //
				.save();

		// when
		final List<CMCard> firstPageOfCards = dataAccessLogic.fetchCards(newClass.getName(),
				createQueryOptions(3, 0, null, null));
		final List<CMCard> secondPageOfCards = dataAccessLogic.fetchCards(newClass.getName(),
				createQueryOptions(3, 3, null, null));

		// then
		assertEquals(firstPageOfCards.size(), 3);
		assertEquals(secondPageOfCards.size(), 1);
	}

	@Test
	public void shouldFetchCardsWithAndConditionsInFilter() throws Exception {
		// given
		final DBClass newClass = dbDataView().createClass(newClass("test"));

		final CMCard card1 = dbDataView().newCard(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		final CMCard card2 = dbDataView().newCard(newClass) //
				.setCode("bar") //
				.setDescription("description_aaa") //
				.save();
		final CMCard card3 = dbDataView().newCard(newClass) //
				.setCode("bar") //
				.setDescription("description_bbb") //
				.save();
		final CMCard card4 = dbDataView().newCard(newClass) //
				.setCode("baz") //
				.setDescription("description_zzz") //
				.save();
		final JSONObject filterObject = new JSONObject(
				"{attribute: {and: [{simple: {attribute: Code, operator: notcontain, value:['bar']}}, "
						+ "{simple: {attribute: Description, operator: contain, value: ['sc_f']}}]}}");

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(newClass.getName(),
				createQueryOptions(10, 0, null, filterObject));

		// then
		assertEquals(fetchedCards.size(), 1);
		assertEquals(fetchedCards.get(0).getCode(), "foo");
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

}

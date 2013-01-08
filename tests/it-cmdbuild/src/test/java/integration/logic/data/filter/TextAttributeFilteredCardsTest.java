package integration.logic.data.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mappers.json.Constants.FilterOperator;
import org.json.JSONObject;
import org.junit.Test;

/**
 * In this class there are tests that filter cards for the attribute with type
 * text.
 */
public class TextAttributeFilteredCardsTest extends FilteredCardsFixture {

	private static final String TEXT_ATTRIBUTE = "attr";

	@Override
	protected void initializeDatabaseData() {
		final DBAttribute createdAttribute = addAttributeToClass(TEXT_ATTRIBUTE, new TextAttributeType(), createdClass);

		final CMCard card1 = dbDataView().newCard(createdClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.set(createdAttribute.getName(), "").save();
		final CMCard card2 = dbDataView().newCard(createdClass) //
				.setCode("bar") //
				.setDescription("desc_bar") //
				.set(createdAttribute.getName(), "TexTual_AttriBUte_Value") //
				.save();
		final CMCard card3 = dbDataView().newCard(createdClass) //
				.setCode("baz") //
				.setDescription("desc_baz") //
				.set(createdAttribute.getName(), null) //
				.save();
		final CMCard card4 = dbDataView().newCard(createdClass) //
				.setCode("zzz") //
				.setDescription("desc_zzz") //
				.set(createdAttribute.getName(), "TEst") //
				.save();
	}

	@Test
	public void fetchFilteredCardsWithEqualOperator() throws Exception {
		// given
		final JSONObject filterObjectForEmptyResult = buildAttributeFilter(TEXT_ATTRIBUTE, FilterOperator.EQUAL, "aaa");
		final QueryOptions queryOptionsForEmptyResult = createQueryOptions(10, 0, null, filterObjectForEmptyResult);

		final JSONObject filterObject = buildAttributeFilter(TEXT_ATTRIBUTE, FilterOperator.EQUAL, "TEst");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards1 = dataAccessLogic.fetchCards(createdClass.getName(),
				queryOptionsForEmptyResult);
		final List<CMCard> fetchedCards2 = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(0, fetchedCards1.size());
		assertEquals(1, fetchedCards2.size());
	}

	@Test
	public void fetchFilteredCardsWithDifferentOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(TEXT_ATTRIBUTE, FilterOperator.NOT_EQUAL, "aaa");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(4, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(TEXT_ATTRIBUTE, FilterOperator.NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(1, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithContainsOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(TEXT_ATTRIBUTE, FilterOperator.CONTAIN, "tuaL_");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(1, fetchedCards.size());
		assertEquals("bar", fetchedCards.get(0).getCode());
	}

	@Test
	public void fetchFilteredCardsWithDoesNotContainOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(TEXT_ATTRIBUTE, FilterOperator.NOT_CONTAIN, "tuaL_");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(3, fetchedCards.size());
	}

	@Test
	public void resultsOfContainUnionNotContainMustBeComplementary() throws Exception {
		// given
		final JSONObject notContainFilterObject = buildAttributeFilter(TEXT_ATTRIBUTE, FilterOperator.NOT_CONTAIN,
				"tuaL_");
		final JSONObject containFilterObject = buildAttributeFilter(TEXT_ATTRIBUTE, FilterOperator.CONTAIN, "tuaL_");
		final QueryOptions notContainQueryOptions = createQueryOptions(10, 0, null, notContainFilterObject);
		final QueryOptions containQueryOptions = createQueryOptions(10, 0, null, containFilterObject);

		// when
		final List<CMCard> notContainFetchedCards = dataAccessLogic.fetchCards(createdClass.getName(),
				notContainQueryOptions);
		final List<CMCard> containFetchedCards = dataAccessLogic
				.fetchCards(createdClass.getName(), containQueryOptions);

		// then
		assertEquals(4, notContainFetchedCards.size() + containFetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithBeginsOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(TEXT_ATTRIBUTE, FilterOperator.BEGIN, "te");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(2, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithNotBeginsOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(TEXT_ATTRIBUTE, FilterOperator.NOT_BEGIN, "te");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(2, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithEndsOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(TEXT_ATTRIBUTE, FilterOperator.END, "st");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(1, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithNotEndsOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(TEXT_ATTRIBUTE, FilterOperator.NOT_END, "st");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(3, fetchedCards.size());
	}

}

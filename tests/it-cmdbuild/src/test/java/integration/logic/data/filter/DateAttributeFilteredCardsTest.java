package integration.logic.data.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mappers.json.Constants.FilterOperator;
import org.json.JSONObject;
import org.junit.Test;

/**
 * In this class there are tests that filter cards for the attribute with type
 * date.
 */
public class DateAttributeFilteredCardsTest extends FilteredCardsFixture {

	private static final String DATE_ATTRIBUTE = "attr";

	@Override
	protected void initializeDatabaseData() {
		final DBAttribute createdAttribute = addAttributeToClass(DATE_ATTRIBUTE, new DateAttributeType(), createdClass);

		final CMCard card1 = dbDataView().newCard(createdClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.set(createdAttribute.getName(), "06/08/2012") //
				.save();
		final CMCard card2 = dbDataView().newCard(createdClass) //
				.setCode("bar") //
				.setDescription("desc_bar") //
				.set(createdAttribute.getName(), "11/12/1995") //
				.save();
		final CMCard card3 = dbDataView().newCard(createdClass) //
				.setCode("baz") //
				.setDescription("desc_baz") //
				.set(createdAttribute.getName(), "10/09/1998") //
				.save();
		final CMCard card4 = dbDataView().newCard(createdClass) //
				.setCode("zzz") //
				.setDescription("desc_zzz") //
				.set(createdAttribute.getName(), null) //
				.save();
	}

	@Test
	public void fetchFilteredCardsWithEqualOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.EQUAL, "11/12/1995");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(1, fetchedCards.size());
		assertEquals("bar", fetchedCards.get(0).getCode());
	}

	@Test
	public void fetchFilteredCardsWithNotEqualOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.NOT_EQUAL, "11/12/1995");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(3, fetchedCards.size());
		assertEquals("foo", fetchedCards.get(0).getCode());
		assertEquals("baz", fetchedCards.get(1).getCode());
		assertEquals("zzz", fetchedCards.get(2).getCode());
	}

	@Test
	public void fetchFilteredCardsWithGreaterThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.GREATER_THAN, "10/09/1998");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(1, fetchedCards.size());
		assertEquals("foo", fetchedCards.get(0).getCode());
	}

	@Test
	public void fetchFilteredCardsWithLessThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.LESS_THAN, "11/09/1998");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(2, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithBetweenOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.BETWEEN, "19/01/1990",
				"19/01/2000");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(2, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(1, fetchedCards.size());
		assertEquals("zzz", fetchedCards.get(0).getCode());
	}

	@Test
	public void fetchFilteredCardsWithNotNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.NOT_NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(3, fetchedCards.size());
	}

}

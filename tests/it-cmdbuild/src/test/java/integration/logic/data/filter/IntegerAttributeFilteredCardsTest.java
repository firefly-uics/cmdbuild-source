package integration.logic.data.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mappers.json.Constants.FilterOperator;
import org.json.JSONObject;
import org.junit.Test;

/**
 * In this class there are tests that filter cards for the attribute with type
 * integer.
 */
public class IntegerAttributeFilteredCardsTest extends FilteredCardsFixture {

	private static final String INTEGER_ATTRIBUTE = "attr";

	@Override
	protected void initializeDatabaseData() {
		final DBAttribute createdAttribute = addAttributeToClass(INTEGER_ATTRIBUTE, new IntegerAttributeType(),
				createdClass);

		final CMCard card1 = dbDataView().newCard(createdClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.set(createdAttribute.getName(), Integer.valueOf(1)) //
				.save();
		final CMCard card2 = dbDataView().newCard(createdClass) //
				.setCode("bar") //
				.setDescription("desc_bar") //
				.set(createdAttribute.getName(), Integer.valueOf(2)) //
				.save();
		final CMCard card3 = dbDataView().newCard(createdClass) //
				.setCode("baz") //
				.setDescription("desc_baz") //
				.set(createdAttribute.getName(), Integer.valueOf(3)) //
				.save();
		final CMCard card4 = dbDataView().newCard(createdClass) //
				.setCode("zzz") //
				.setDescription("desc_zzz") //
				.set(createdAttribute.getName(), Integer.valueOf(4)) //
				.save();
	}

	@Test
	public void fetchFilteredCardsWithEqualOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.EQUAL,
				Integer.valueOf(2));
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
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.NOT_EQUAL,
				Integer.valueOf(2));
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
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.GREATER_THAN,
				Integer.valueOf(2));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(2, fetchedCards.size());
		assertEquals("baz", fetchedCards.get(0).getCode());
		assertEquals("zzz", fetchedCards.get(1).getCode());
	}

	@Test
	public void fetchFilteredCardsWithLessThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.LESS_THAN,
				Integer.valueOf(2));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(1, fetchedCards.size());
		assertEquals("foo", fetchedCards.get(0).getCode());
	}

	@Test
	public void fetchFilteredCardsWithBetweenOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.BETWEEN,
				Integer.valueOf(2), Integer.valueOf(4));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(1, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithNullOperator() throws Exception {
		// given
		final CMCard nullValueCard = dbDataView().newCard(createdClass) //
				.setCode("code_of_null_card") //
				.setDescription("desc_of_null_card") //
				.set(INTEGER_ATTRIBUTE, null) //
				.save();
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(1, fetchedCards.size());
		assertEquals("code_of_null_card", fetchedCards.get(0).getCode());
	}

	@Test
	public void fetchFilteredCardsWithNotNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.NOT_NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(4, fetchedCards.size());
	}

}

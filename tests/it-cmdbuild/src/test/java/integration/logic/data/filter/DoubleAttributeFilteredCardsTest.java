package integration.logic.data.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mappers.json.Constants.FilterOperator;
import org.json.JSONObject;
import org.junit.Test;

/**
 * In this class there are tests that filter cards for the attribute with type
 * integer.
 */
public class DoubleAttributeFilteredCardsTest extends FilteredCardsFixture {

	private static final String DOUBLE_ATTRIBUTE = "attr";

	@Override
	protected void initializeDatabaseData() {
		final DBAttribute createdAttribute = addAttributeToClass(DOUBLE_ATTRIBUTE, new DoubleAttributeType(),
				createdClass);

		final CMCard card1 = dbDataView().newCard(createdClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.set(createdAttribute.getName(), Double.valueOf(1)) //
				.save();
		final CMCard card2 = dbDataView().newCard(createdClass) //
				.setCode("bar") //
				.setDescription("desc_bar") //
				.set(createdAttribute.getName(), Double.valueOf(2.4323)) //
				.save();
		final CMCard card3 = dbDataView().newCard(createdClass) //
				.setCode("baz") //
				.setDescription("desc_baz") //
				.set(createdAttribute.getName(), Double.valueOf(-50.32129559)) //
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
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.EQUAL,
				Double.valueOf(2.4323));
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
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.NOT_EQUAL,
				Double.valueOf(2.4323));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(3, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithGreaterThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.GREATER_THAN,
				Double.valueOf(-100));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(3, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithLessThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.LESS_THAN,
				Double.valueOf(1.8));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(2, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithBetweenOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.BETWEEN,
				Double.valueOf(-10.34), Double.valueOf(4));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(2, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(1, fetchedCards.size());
	}

	@Test
	public void fetchFilteredCardsWithNotNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.NOT_NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final List<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions);

		// then
		assertEquals(3, fetchedCards.size());
	}

}

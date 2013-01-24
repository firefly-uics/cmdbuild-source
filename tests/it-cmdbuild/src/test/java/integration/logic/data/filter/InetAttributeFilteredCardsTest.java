package integration.logic.data.filter;

import static org.junit.Assert.assertEquals;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.Constants.FilterOperator;
import org.json.JSONObject;
import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * In this class there are tests that filter cards for the attribute with type
 * date.
 */
public class InetAttributeFilteredCardsTest extends FilteredCardsFixture {

	private static final String INET_ATTRIBUTE = "Attr";

	@Override
	protected void initializeDatabaseData() {
		final DBAttribute createdAttribute = addAttributeToClass(INET_ATTRIBUTE, new IpAddressAttributeType(),
				createdClass);

		final CMCard card1 = dbDataView().createCardFor(createdClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.set(createdAttribute.getName(), "192.168.0.1") //
				.save();
		final CMCard card2 = dbDataView().createCardFor(createdClass) //
				.setCode("bar") //
				.setDescription("desc_bar") //
				.set(createdAttribute.getName(), "192.168.0.10") //
				.save();
		final CMCard card3 = dbDataView().createCardFor(createdClass) //
				.setCode("baz") //
				.setDescription("desc_baz") //
				.set(createdAttribute.getName(), "192.168.1.120") //
				.save();
		final CMCard card4 = dbDataView().createCardFor(createdClass) //
				.setCode("zzz") //
				.setDescription("desc_zzz") //
				.set(createdAttribute.getName(), null) //
				.save();
	}

	@Test
	public void fetchFilteredCardsWithEqualOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INET_ATTRIBUTE, FilterOperator.EQUAL, "192.168.0.1");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions)
				.getPaginatedCards();

		// then
		assertEquals(1, Iterables.size(fetchedCards));
		final CMCard card = Iterables.get(fetchedCards, 0);
		assertEquals("foo", card.getCode());
	}

	@Test
	public void fetchFilteredCardsWithNotEqualOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INET_ATTRIBUTE, FilterOperator.NOT_EQUAL, "192.168.0.1");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions)
				.getPaginatedCards();

		// then
		assertEquals(3, Iterables.size(fetchedCards));
	}

	@Test
	public void fetchFilteredCardsWithGreaterThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INET_ATTRIBUTE, FilterOperator.GREATER_THAN, "192.168.0.1");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions)
				.getPaginatedCards();

		// then
		assertEquals(2, Iterables.size(fetchedCards));
	}

	@Test
	public void fetchFilteredCardsWithLessThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INET_ATTRIBUTE, FilterOperator.LESS_THAN, "192.168.0.11");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions)
				.getPaginatedCards();

		// then
		assertEquals(2, Iterables.size(fetchedCards));
	}

	@Test
	public void fetchFilteredCardsWithBetweenOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INET_ATTRIBUTE, FilterOperator.BETWEEN, "192.168.0.0",
				"192.168.1.121");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions)
				.getPaginatedCards();

		// then
		assertEquals(3, Iterables.size(fetchedCards));
	}

	@Test
	public void fetchFilteredCardsWithNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INET_ATTRIBUTE, FilterOperator.NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions)
				.getPaginatedCards();

		// then
		assertEquals(1, Iterables.size(fetchedCards));
		assertEquals("zzz", Iterables.get(fetchedCards, 0).getCode());
	}

	@Test
	public void fetchFilteredCardsWithNotNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INET_ATTRIBUTE, FilterOperator.NOT_NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<CMCard> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions)
				.getPaginatedCards();

		// then
		assertEquals(3, Iterables.size(fetchedCards));
	}

}

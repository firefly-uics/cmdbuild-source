package integration.logic.data.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mappers.json.Constants.FilterOperator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utils.GenericRollbackDriver;

/**
 * In this class there are tests that filter cards for the attribute with type
 * integer.
 */
public class IntegerAttributeFilteredCardsTest extends FilteredCardsFixture {

	private static final String CLASS_NAME = "test_class";
	private static final String ATTRIBUTE_NAME = "attr";
	private DataAccessLogic dataAccessLogic;
	private DBClass createdClass;

	/*
	 * Here I'm using the postgres driver directly due to problems to rollback
	 * on create attribute command
	 * 
	 * @see utils.IntegrationTestBase#createTestDriver()
	 */
	protected DBDriver createTestDriver() {
		return createBaseDriver();
	}

	@Before
	public void createDataDefinitionLogic() throws Exception {
		dataAccessLogic = new DataAccessLogic(dbDataView());
		initializeDatabaseData();
	}

	private void initializeDatabaseData() {
		createdClass = createClass(CLASS_NAME, null);
		final DBAttribute createdAttribute = addAttributeToClass(ATTRIBUTE_NAME, new IntegerAttributeType(),
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
		JSONObject filterObject = buildAttributeFilter(ATTRIBUTE_NAME, FilterOperator.EQUAL, Integer.valueOf(2));
		QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		List<CMCard> fetchedCards = dataAccessLogic.fetchCards(CLASS_NAME, queryOptions);

		// then
		assertEquals(1, fetchedCards.size());
		assertEquals("bar", fetchedCards.get(0).getCode());
	}
	
	@Test
	public void fetchFilteredCardsWithNotEqualOperator() throws Exception {
		// given
		JSONObject filterObject = buildAttributeFilter(ATTRIBUTE_NAME, FilterOperator.NOT_EQUAL, Integer.valueOf(2));
		QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);
		
		// when
		List<CMCard> fetchedCards = dataAccessLogic.fetchCards(CLASS_NAME, queryOptions);
		
		// then
		assertEquals(3, fetchedCards.size());
		assertEquals("foo", fetchedCards.get(0).getCode());
		assertEquals("baz", fetchedCards.get(1).getCode());
		assertEquals("zzz", fetchedCards.get(2).getCode());
	}
	
	@Test
	public void fetchFilteredCardsWithGreaterThanOperator() throws Exception {
		// given
		JSONObject filterObject = buildAttributeFilter(ATTRIBUTE_NAME, FilterOperator.GREATER_THAN, Integer.valueOf(2));
		QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);
		
		// when
		List<CMCard> fetchedCards = dataAccessLogic.fetchCards(CLASS_NAME, queryOptions);
		
		// then
		assertEquals(2, fetchedCards.size());
		assertEquals("baz", fetchedCards.get(0).getCode());
		assertEquals("zzz", fetchedCards.get(1).getCode());
	}
	
	@Test
	public void fetchFilteredCardsWithLessThanOperator() throws Exception {
		// given
		JSONObject filterObject = buildAttributeFilter(ATTRIBUTE_NAME, FilterOperator.LESS_THAN, Integer.valueOf(2));
		QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);
		
		// when
		List<CMCard> fetchedCards = dataAccessLogic.fetchCards(CLASS_NAME, queryOptions);
		
		// then
		assertEquals(1, fetchedCards.size());
		assertEquals("foo", fetchedCards.get(0).getCode());
	}
	
	@Test
	public void fetchFilteredCardsWithBetweenOperator() throws Exception {
		// given
		JSONObject filterObject = buildAttributeFilter(ATTRIBUTE_NAME, FilterOperator.BETWEEN, Integer.valueOf(2), Integer.valueOf(4));
		QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);
		
		// when
		List<CMCard> fetchedCards = dataAccessLogic.fetchCards(CLASS_NAME, queryOptions);
		
		// then
		assertEquals(1, fetchedCards.size());
	}
	
	@Test
	public void fetchFilteredCardsWithNullOperator() throws Exception {
		// given
		final CMCard nullValueCard = dbDataView().newCard(createdClass) //
				.setCode("code_of_null_card") //
				.setDescription("desc_of_null_card") //
				.set(ATTRIBUTE_NAME, null) //
				.save();
		JSONObject filterObject = buildAttributeFilter(ATTRIBUTE_NAME, FilterOperator.NULL);
		QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);
		
		// when
		List<CMCard> fetchedCards = dataAccessLogic.fetchCards(CLASS_NAME, queryOptions);
		
		// then
		assertEquals(1, fetchedCards.size());
		assertEquals("code_of_null_card",fetchedCards.get(0).getCode());
	}
	
	@Test
	public void fetchFilteredCardsWithNotNullOperator() throws Exception {
		// given
		JSONObject filterObject = buildAttributeFilter(ATTRIBUTE_NAME, FilterOperator.NOT_NULL);
		QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);
		
		// when
		List<CMCard> fetchedCards = dataAccessLogic.fetchCards(CLASS_NAME, queryOptions);
		
		// then
		assertEquals(4, fetchedCards.size());
	}

	@After
	public void tearDown() {
		dbDriver().clear(createdClass);
		dbDriver().deleteClass(createdClass);
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

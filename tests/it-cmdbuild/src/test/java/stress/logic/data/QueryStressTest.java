package stress.logic.data;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.junit.Assert.*;

import java.util.List;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import utils.DBFixture;

public class QueryStressTest extends DBFixture {

	private DataAccessLogic dataAccessLogic;
	private DBClass stressTestClass;
	private static final String CLASS_NAME = "stress_test_class";
	private static final int NUMBER_OF_CARDS = 300;

	@Override
	protected DBDriver createTestDriver() {
		return super.createBaseDriver();
	}

	@Before
	public void createDataDefinitionLogic() throws Exception {
		dataAccessLogic = new DataAccessLogic(dbDataView());
		DBDriver pgDriver = dbDriver();
		stressTestClass = pgDriver.findClassByName(CLASS_NAME);
		if (stressTestClass == null) {
			stressTestClass = dbDataView().createClass(newClass(CLASS_NAME, null));
		}
		storeBigAmountOfCardsIfNeeded();
	}

	@Test(timeout = 200)
	public void evaluatePaginationPerformanceInQueries() throws Exception {
		// given
		QueryOptions queryOptions = createQueryOptions(100, 0, null, null);

		// when
		List<CMCard> cards = dataAccessLogic.fetchCards(CLASS_NAME, queryOptions);
		
		//then
		assertEquals(cards.size(), 100);
	}
	
	@Test(timeout = 200)
	public void evaluatePaginationPerformanceWithSortingAndFilters() throws Exception {
		// given
		JSONArray sortersArray = new JSONArray();
		sortersArray.put(new JSONObject("{property: Code, direction: ASC}"));
		JSONObject filter = new JSONObject("{filter: {simple: {attribute: Code, operator: equal, value: ['100']}}}");
		QueryOptions queryOptions = createQueryOptions(150, 0, sortersArray, filter);
		
		// when
		List<CMCard> cards = dataAccessLogic.fetchCards(CLASS_NAME, queryOptions);
		
		//then
		assertEquals(cards.size(), 1);
	}

	private void storeBigAmountOfCardsIfNeeded() {
		CMQueryResult result = dbDataView().select(anyAttribute(stressTestClass)).from(stressTestClass) //
				.run();
		if (result.totalSize() < NUMBER_OF_CARDS) {
			for (int i = result.totalSize(); i < NUMBER_OF_CARDS; i++) {
				dbDataView().newCard(stressTestClass) //
						.setCode("" + i) //
						.setDescription("desc_" + i) //
						.save();
			}
		}
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
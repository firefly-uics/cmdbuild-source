package integration.logic.data.filter;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;

/**
 * In this class there are tests that filter cards for the attribute with type
 * integer.
 */
public class BooleanAttributeFilteredCardsTest extends FilteredCardsFixture {

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
	@Override
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
		final DBAttribute createdAttribute = addAttributeToClass(ATTRIBUTE_NAME, new BooleanAttributeType(),
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

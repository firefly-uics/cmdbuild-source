package integration.logic.data.filter;

import static utils.IntergrationTestUtils.newAttribute;
import static utils.IntergrationTestUtils.newClass;

import java.util.Map;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mappers.json.Constants.FilterOperator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;

import utils.IntegrationTestBase;

import com.google.common.collect.Lists;

public abstract class FilteredCardsFixture extends IntegrationTestBase {

	private static final String CLASS_NAME = "test_class";
	protected DataAccessLogic dataAccessLogic;
	protected DBClass createdClass;

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
		createdClass = createClass(CLASS_NAME);
		initializeDatabaseData();
	}

	/**
	 * It must be overridden by classes that extends this fixture in order to
	 * initialize the database with known attributes and cards
	 */
	protected abstract void initializeDatabaseData();

	@After
	public void tearDown() {
		dbDriver().clear(createdClass);
		dbDriver().deleteClass(createdClass);
	}

	protected QueryOptions createQueryOptions(final int limit, final int offset, final JSONArray sorters,
			final JSONObject filter) {
		return QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(filter) //
				.build();
	}

	protected DBClass createClass(final String name) {
		return dbDataView().createClass(newClass(name));
	}

	protected DBAttribute addAttributeToClass(final String name, final CMAttributeType type, final DBClass klass) {
		return dbDataView().createAttribute(newAttribute(name, type, klass));
	}

	protected void insertCardWithValues(final DBClass klass, final Map<String, Object> attributeNameToValue) {
		final DBCard cardToBeCreated = dbDataView().newCard(klass);
		for (final String key : attributeNameToValue.keySet()) {
			cardToBeCreated.set(key, attributeNameToValue.get(key));
		}
		cardToBeCreated.save();
	}

	protected JSONObject buildAttributeFilter(final String attributeName, final FilterOperator operator,
			final Object... values) throws JSONException {
		String valuesString = "";
		final Object[] valuesArray = Lists.newArrayList(values).toArray();
		for (int i = 0; i < valuesArray.length; i++) {
			valuesString = valuesString + valuesArray[i].toString();
			if (i < valuesArray.length - 1) {
				valuesString = valuesString + ",";
			}
		}
		final String s = "{attribute: {simple: {attribute: " + attributeName + ", operator: " + operator.toString()
				+ ", value:[" + valuesString + "]}}}";
		return new JSONObject(s);
	}

}

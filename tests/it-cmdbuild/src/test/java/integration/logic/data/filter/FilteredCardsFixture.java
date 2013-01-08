package integration.logic.data.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.DBDataView.DBClassDefinition;
import org.cmdbuild.logic.mappers.json.Constants.FilterOperator;
import org.json.JSONException;
import org.json.JSONObject;

import utils.IntegrationTestBase;

import com.google.common.collect.Lists;

public class FilteredCardsFixture extends IntegrationTestBase {

	/**
	 * A list for allowed operators for a defined attribute type. There is one
	 * subclass for each attribute type. FILL THIS LIST IN EACH SUBCLASS!
	 */
	protected static List<FilterOperator> allowedOperators = Lists.newArrayList();

	protected DBClass createClass(final String name, final DBClass parent) {
		final DBClassDefinition definition = mock(DBClassDefinition.class);
		when(definition.getName()).thenReturn(name);
		when(definition.getParent()).thenReturn(parent);
		when(definition.isHoldingHistory()).thenReturn(true);
		return dbDataView().createClass(definition);
	}

	protected DBAttribute addAttributeToClass(final String name, final CMAttributeType type, final DBClass klass) {
		final CMAttributeDefinition attrDef = mock(CMAttributeDefinition.class);
		when(attrDef.getName()).thenReturn(name);
		when(attrDef.getOwner()).thenReturn(klass);
		when((CMAttributeType) attrDef.getType()).thenReturn(type);
		when(attrDef.getMode()).thenReturn(Mode.WRITE);
		return dbDataView().createAttribute(attrDef);
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
		return new JSONObject("{attribute: {simple: {attribute: " + attributeName + ", operator: "
				+ operator.toString() + ", value:[" + valuesString + "]}}}");
	}

}

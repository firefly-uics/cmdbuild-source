package org.cmdbuild.shark.toolagent;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.shark.util.CmdbuildUtils;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

/**
 * Select a reference based on the passed parameters A ReferenceType instance
 * will be returned.
 * 
 * Possible parameters: IN String className IN String code <-- will
 * automatically filter on the "Code" attribute OUT ReferenceType
 * 
 * IN String className IN String attributeName IN String attributeValue <-- as
 * for now, only String attributes are permitted... OUT ReferenceType
 * 
 * @author cheng
 * 
 */
public class SelectReferenceToolAgent extends AbstractCmdbuildWSToolAgent {

	@Override
	protected void invokeWebService(final Private stub, final AppParameter[] params, final String toolInfoID)
			throws Exception {
		final String className = (String) params[1].the_value;

		final List<Attribute> attributeList = new ArrayList<Attribute>();
		final Attribute id = new Attribute();
		id.setName("Id");
		attributeList.add(id);
		final Attribute idclass = new Attribute();
		idclass.setName("IdClass");
		attributeList.add(idclass);
		final Attribute description = new Attribute();
		description.setName("Description");
		attributeList.add(description);

		final Query query = new Query();
		final Filter filter = new Filter();
		filter.setOperator("EQUALS");
		query.setFilter(filter);

		if (params.length == 4) {
			final String code = (String) params[2].the_value;
			filter.setName("Code");
			filter.getValue().add(code);
		} else if (params.length == 5) {
			final String attrName = (String) params[2].the_value;
			final String attrValue = (String) params[3].the_value;
			filter.setName(attrName);
			filter.getValue().add(attrValue);
		}

		final List<Card> cards = stub.getCardList(className, attributeList, query, null, null, null, null, null)
				.getCards();

		ReferenceType out = null;
		if (cards != null && cards.size() > 0) {
			final Card userCard = cards.get(0);
			out = CmdbuildUtils.createReferenceFromWSCard(userCard);
		}
		setOutputValue(params, out);
	}

	private void setOutputValue(final AppParameter[] params, final ReferenceType outputValue) {
		params[params.length - 1].the_value = outputValue;
	}
}

package org.cmdbuild.shark.toolagent;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.shark.util.CmdbuildUtils;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;

/**
 * The only AppParameter required is a ReferenceType OUT
 * 
 */
public class GetCurrentGroupReferenceToolAgent extends AbstractCmdbuildWSToolAgent {

	@Override
	protected void invokeWebService(final Private stub, final AppParameter[] params, final String toolInfoID)
			throws Exception {

		final String partName = CmdbuildUtils.getCurrentGroupName(shandle);

		final Query query = new Query();
		final Filter filter = new Filter();
		filter.setName("Code");
		filter.getValue().add(partName);
		filter.setOperator("EQUALS");
		query.setFilter(filter);

		final List<Attribute> sel = new ArrayList<Attribute>();
		final Attribute id = new Attribute();
		id.setName("Id");
		sel.add(id);
		final Attribute idclass = new Attribute();
		idclass.setName("IdClass");
		sel.add(idclass);
		final Attribute description = new Attribute();
		description.setName("Description");
		sel.add(description);

		final List<Card> cards = stub.getCardList("Role", sel, query, null, null, null, null, null).getCards();

		ReferenceType out;
		if (cards != null && cards.size() > 0) {
			final Card groupCard = cards.get(0);
			out = CmdbuildUtils.createReferenceFromWSCard(groupCard);
		} else {
			out = null;
		}
		params[1].the_value = out;
	}
}

package org.cmdbuild.logic;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause.Operator;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.GetRelationHistory;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;

/**
 * Business Logic Layer for Data Access
 */
public class DataAccessLogic {

	private final CMDataView view;

	public DataAccessLogic(final CMDataView view) {
		this.view = view;
	}

	public GetRelationListResponse getRelationList(final Card srcCard, final DomainWithSource dom) {
		return new GetRelationList(view).exec(srcCard, dom);
	}

	public GetRelationHistoryResponse getRelationHistory(final Card srcCard) {
		return new GetRelationHistory(view).exec(srcCard);
	}

	public CMCard getCard(final String className, final Object cardId) {
		final CMClass cardType = view.findClassByName(className);
		final CMQueryResult result = view.select(
				attribute(cardType, Constants.DESCRIPTION_ATTRIBUTE))
			.from(cardType)
			.where(attribute(cardType, Constants.ID_ATTRIBUTE), Operator.EQUALS, cardId)
			.run();
		if (result.isEmpty()) {
			return null;
		} else {
			return result.iterator().next().getCard(cardType);
		}
	}
}

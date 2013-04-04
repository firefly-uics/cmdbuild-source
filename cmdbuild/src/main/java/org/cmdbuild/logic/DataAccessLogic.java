package org.cmdbuild.logic;

import java.util.List;
import java.util.Map;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.legacywrappers.CardWrapper;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationHistory;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.services.auth.UserContext;

/**
 * Business Logic Layer for Data Access
 */
public class DataAccessLogic {

	private final CMDataView view;

	public DataAccessLogic(final CMDataView view) {
		this.view = view;
	}

	public Map<Object, List<RelationInfo>> relationsBySource(final String sourceTypeName, final DomainWithSource dom) {
		return new GetRelationList(view).list(sourceTypeName, dom);
	}

	public GetRelationListResponse getRelationList(final Card srcCard, final DomainWithSource dom) {
		return new GetRelationList(view).exec(srcCard, dom);
	}

	public GetRelationHistoryResponse getRelationHistory(final Card srcCard) {
		return new GetRelationHistory(view).exec(srcCard);
	}

	public Iterable<? extends CMDomain> findAllDomains() {
		return this.view.findAllDomains();
	}

	@Legacy("IMPORTANT! FIX THE NEW DAO AND FIX BECAUSE IT USES THE SYSTEM USER!")
	public CMCard getCard(final String className, final Object cardId) {
		try {
			int id = Integer.parseInt(cardId.toString()); // very expensive but almost never called
			final ICard card = UserContext.systemContext().tables().get(className).cards().get(id);
			return new CardWrapper(card);
		} catch (Exception e) {
			return null;
		}
		/* The new DAO layer does not query subclasses! ****************
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
		**************************************************************** */
	}
}

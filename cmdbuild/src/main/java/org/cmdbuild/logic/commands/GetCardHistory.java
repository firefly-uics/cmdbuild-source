package org.cmdbuild.logic.commands;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.ClassHistory.history;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.LogicDTO.Card;

import com.google.common.collect.Lists;

public class GetCardHistory {

	private final CMDataView view;
	private CMClass historyClass;

	public GetCardHistory(final CMDataView view) {
		this.view = view;
	}

	public GetCardHistoryResponse exec(final Card card) {
		Validate.notNull(card);
		historyClass = history(view.findClass(card.className));
		final CMQueryResult historyCardsResult = view.select(anyAttribute(historyClass)) //
				.from(historyClass) //
				.where(condition(attribute(historyClass, "CurrentId"), eq(card.cardId))) //
				.run();
		return createResponse(historyCardsResult);
	}

	private GetCardHistoryResponse createResponse(final CMQueryResult historyCardsResult) {
		final GetCardHistoryResponse response = new GetCardHistoryResponse();
		for (final CMQueryRow row : historyCardsResult) {
			final CMCard historyCard = row.getCard(historyClass);
			response.addCard(historyCard);
		}
		return response;
	}

	public static class GetCardHistoryResponse implements Iterable<CMCard> {

		private final List<CMCard> historyCards;

		private GetCardHistoryResponse() {
			historyCards = Lists.newArrayList();
		}

		private void addCard(final CMCard card) {
			historyCards.add(card);
		}

		@Override
		public Iterator<CMCard> iterator() {
			return historyCards.iterator();
		}
	}
}

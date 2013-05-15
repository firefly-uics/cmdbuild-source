package org.cmdbuild.logic.commands;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.ClassHistory.history;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.CardEntryFiller;
import org.cmdbuild.logic.data.access.CardStorableConverter;
import org.cmdbuild.logic.data.access.resolver.CardSerializer;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver;
import org.cmdbuild.model.data.Card;

import com.google.common.collect.Lists;

public class GetCardHistory {

	private final CMDataView view;
	private CMClass historyClass;

	public GetCardHistory(final CMDataView view) {
		this.view = view;
	}

	public GetCardHistoryResponse exec(final Card card) {
		Validate.notNull(card);
		historyClass = history(view.findClass(card.getClassName()));
		final CMQueryResult historyCardsResult = view.select(anyAttribute(historyClass)) //
				.from(historyClass) //
				.where(condition(attribute(historyClass, "CurrentId"), eq(card.getId()))) //
				.run();
		return createResponse(historyCardsResult);
	}

	private GetCardHistoryResponse createResponse(final Iterable<CMQueryRow> rows) {
		final GetCardHistoryResponse response = new GetCardHistoryResponse();
		final List<CMCard> cards = Lists.newArrayList();
		for (final CMQueryRow row : rows) {
			final CMCard card = row.getCard(historyClass);
			cards.add(card);
		}

		// danger: don't change the following line...
		final CMClass innerClass = view.findClass(historyClass.getId());
		final Iterable<CMCard> cardsWithForeingReferences = ForeignReferenceResolver.<CMCard> newInstance() //
				.withSystemDataView(applicationContext().getBean(DBDataView.class)) //
				.withEntryType(innerClass) //
				.withEntries(cards) //
				.withEntryFiller(new CardEntryFiller()) //
				.withLookupStore(applicationContext().getBean(LookupStore.class)) //
				.withSerializer(new CardSerializer<CMCard>()) //
				.build() //
				.resolve();

		for (final CMCard card : cardsWithForeingReferences) {
			response.addCard(CardStorableConverter.of(card).convert(card));
		}
		return response;
	}

	public static class GetCardHistoryResponse implements Iterable<Card> {

		private final List<Card> historyCards;

		private GetCardHistoryResponse() {
			historyCards = Lists.newArrayList();
		}

		private void addCard(final Card card) {
			historyCards.add(card);
		}

		@Override
		public Iterator<Card> iterator() {
			return historyCards.iterator();
		}

	}

}

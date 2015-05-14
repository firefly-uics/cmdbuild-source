package org.cmdbuild.logic.commands;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.ClassHistory.history;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.access.CardEntryFiller;
import org.cmdbuild.logic.data.access.CardStorableConverter;
import org.cmdbuild.logic.data.access.resolver.CardSerializer;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver;
import org.cmdbuild.model.data.Card;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class GetCardHistory {

	private final CMDataView dataView;

	private CMClass historyClass;

	public GetCardHistory(final CMDataView view) {
		this.dataView = view;
	}

	public Iterable<Card> exec(final Card card, final boolean allAttributes) {
		Validate.notNull(card);
		final CMClass target = dataView.findClass(card.getClassName());
		historyClass = history(target);
		final QueryAliasAttribute attributes = allAttributes ? anyAttribute(historyClass) : attribute(historyClass,
				target.getCodeAttributeName());
		final CMQueryResult historyCardsResult = dataView.select(attributes) //
				.from(historyClass) //
				.where(condition(attribute(historyClass, "CurrentId"), eq(card.getId()))) //
				.run();
		return createResponse(historyCardsResult);
	}

	private Iterable<Card> createResponse(final Iterable<CMQueryRow> rows) {
		final List<CMCard> cards = Lists.newArrayList();
		for (final CMQueryRow row : rows) {
			final CMCard card = row.getCard(historyClass);
			cards.add(card);
		}

		final Iterable<CMCard> cardsWithForeingReferences = ForeignReferenceResolver.<CMCard> newInstance() //
				.withEntries(cards) //
				.withEntryFiller(new CardEntryFiller()) //
				.withSerializer(new CardSerializer<CMCard>()) //
				.build() //
				.resolve();

		return from(cardsWithForeingReferences) //
				.transform(new Function<CMCard, Card>() {

					@Override
					public Card apply(final CMCard input) {
						return CardStorableConverter.of(input).convert(input);
					}

				});
	}

}

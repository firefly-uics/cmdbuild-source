package org.cmdbuild.api.fluent.ws;

import static org.cmdbuild.api.utils.AttributeUtils.attributesFor;

import org.cmdbuild.api.fluent.AbstractCard;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;

public class WsFluentApiExecutor implements FluentApiExecutor {

	private final Private proxy;

	public WsFluentApiExecutor(final Private proxy) {
		this.proxy = proxy;
	}

	public CardDescriptor create(final NewCard newCard) {
		final Card card = cardFor(newCard);
		final int id = proxy.createCard(card);
		return new CardDescriptor(newCard.getClassName(), id);
	}

	public void update(final ExistingCard existingCard) {
		final Card card = cardFor(existingCard);
		card.setId(existingCard.getId());
		proxy.updateCard(card);
	}

	public void delete(final ExistingCard existingCard) {
		proxy.deleteCard(existingCard.getClassName(), existingCard.getId());
	}

	private Card cardFor(final AbstractCard card) {
		final Card wsCard = new Card();
		wsCard.setClassName(card.getClassName());
		wsCard.getAttributeList().addAll(attributesFor(card.getAttributes()));
		return wsCard;
	}

}

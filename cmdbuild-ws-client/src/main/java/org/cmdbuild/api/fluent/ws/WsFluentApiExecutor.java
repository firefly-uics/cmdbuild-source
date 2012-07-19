package org.cmdbuild.api.fluent.ws;

import static org.cmdbuild.api.utils.SoapUtils.attributesFor;
import static org.cmdbuild.api.utils.SoapUtils.cardFrom;
import static org.cmdbuild.api.utils.SoapUtils.soapCardFor;
import static org.cmdbuild.api.utils.SoapUtils.soapRelationFor;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.ExistingRelation;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.api.fluent.NewRelation;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Relation;

public class WsFluentApiExecutor implements FluentApiExecutor {

	private final Private proxy;

	public WsFluentApiExecutor(final Private proxy) {
		this.proxy = proxy;
	}

	public CardDescriptor create(final NewCard newCard) {
		final org.cmdbuild.services.soap.Card card = soapCardFor(newCard);
		final int id = proxy.createCard(card);
		return new CardDescriptor(newCard.getClassName(), id);
	}

	public void update(final ExistingCard existingCard) {
		final org.cmdbuild.services.soap.Card card = soapCardFor(existingCard);
		card.setId(existingCard.getId());
		proxy.updateCard(card);
	}

	public void delete(final ExistingCard existingCard) {
		proxy.deleteCard(existingCard.getClassName(), existingCard.getId());
	}

	public Card fetch(final ExistingCard existingCard) {
		final org.cmdbuild.services.soap.Card soapCard = proxy.getCard( //
				existingCard.getClassName(), //
				existingCard.getId(), //
				attributesFor(existingCard.getAttributes()));
		return cardFrom(soapCard);
	}

	public void create(final NewRelation newRelation) {
		final Relation relation = soapRelationFor(newRelation);
		proxy.createRelation(relation);
	}

	public void delete(final ExistingRelation existingRelation) {
		final Relation relation = soapRelationFor(existingRelation);
		proxy.deleteRelation(relation);
	}

}

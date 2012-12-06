package org.cmdbuild.services.soap.types;

import org.cmdbuild.elements.interfaces.ICard;


public class CardExt extends Card {

	private String description;

	public CardExt() {
	}

	public CardExt(final ICard card, final ValueSerializer valueSerializer) {
		super(card, valueSerializer);
	}

	public CardExt(final ICard card, final Attribute[] attrs, final ValueSerializer valueSerializer) {
		super(card, attrs, valueSerializer);
	}

	public String getClassDescription() {
		return description;
	}

	public void setClassDescription(final String description) {
		this.description = description;
	}

	protected void setup(final ICard card) {
		super.setup(card);
		this.setClassDescription(card.getSchema().getDescription());
	}
}

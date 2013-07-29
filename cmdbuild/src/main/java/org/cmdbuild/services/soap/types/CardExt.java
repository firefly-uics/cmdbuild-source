package org.cmdbuild.services.soap.types;

public class CardExt extends Card {

	private String description;

	public CardExt() {
	}

	public CardExt(final org.cmdbuild.model.data.Card card) {
		this(card, new ValueSerializer(card));
	}

	public CardExt(final org.cmdbuild.model.data.Card card, final ValueSerializer valueSerializer) {
		super(card, valueSerializer);
	}

	public CardExt(final org.cmdbuild.model.data.Card card, final Attribute[] attrs,
			final ValueSerializer valueSerializer) {
		super(card, attrs, valueSerializer);
	}

	public String getClassDescription() {
		return description;
	}

	public void setClassDescription(final String description) {
		this.description = description;
	}

	protected void setup(final org.cmdbuild.model.data.Card card) {
		super.setup(card);
		this.setClassDescription(card.getClassDescription());
	}
}

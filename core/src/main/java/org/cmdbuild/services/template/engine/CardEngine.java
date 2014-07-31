package org.cmdbuild.services.template.engine;

import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.engine.Engine;
import org.cmdbuild.dao.entry.CMCard;

public class CardEngine implements Engine {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<CardEngine> {

		private CMCard card;

		private Builder() {
			// use factory method
		}

		@Override
		public CardEngine build() {
			validate();
			return new CardEngine(this);
		}

		private void validate() {
			Validate.notNull(card, "missing card");
		}

		public Builder withCard(final CMCard card) {
			this.card = card;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final CMCard card;

	private CardEngine(final Builder builder) {
		this.card = builder.card;
	}

	@Override
	public Object eval(final String expression) {
		if (ID_ATTRIBUTE.equalsIgnoreCase(expression)) {
			return card.getId();
		}
		return card.get(expression);
	}

}

package org.cmdbuild.service.rest.serialization;

import java.util.Map.Entry;

import org.cmdbuild.model.data.Card;

public class FromCardToCardDetail extends FromSomeKindOfCardToMap<Card> {

	public static Builder<Card> newInstance() {
		return new Builder<Card>() {

			@Override
			protected FromSomeKindOfCardToMap<Card> doBuild() {
				return new FromCardToCardDetail(this);
			}

		};
	}

	private FromCardToCardDetail(final Builder<Card> builder) {
		super(builder);
	}

	@Override
	protected String classNameOf(final Card input) {
		return input.getClassName();
	}

	@Override
	protected Long idOf(final Card input) {
		return input.getId();
	}

	@Override
	protected boolean idAndClassnameRequired() {
		return true;
	}

	@Override
	protected Iterable<Entry<String, Object>> valuesOf(final Card input) {
		return input.getAttributes().entrySet();
	}

}

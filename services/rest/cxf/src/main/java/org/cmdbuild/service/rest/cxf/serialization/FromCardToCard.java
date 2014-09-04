package org.cmdbuild.service.rest.cxf.serialization;

import java.util.Map.Entry;

import org.cmdbuild.model.data.Card;

public class FromCardToCard extends ToCardFunction<Card> {

	public static Builder<Card> newInstance() {
		return new Builder<Card>() {

			@Override
			protected ToCardFunction<Card> doBuild() {
				return new FromCardToCard(this);
			}

		};
	}

	private FromCardToCard(final Builder<Card> builder) {
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
	protected Iterable<Entry<String, Object>> valuesOf(final Card input) {
		return input.getAttributes().entrySet();
	}

}

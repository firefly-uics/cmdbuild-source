package org.cmdbuild.service.rest.cxf.serialization;

import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMCard;

public class FromCMCardToCard extends ToCardFunction<CMCard> {

	public static Builder<CMCard> newInstance() {
		return new Builder<CMCard>() {

			@Override
			protected ToCardFunction<CMCard> doBuild() {
				return new FromCMCardToCard(this);
			};

		};
	}

	private FromCMCardToCard(final Builder<CMCard> builder) {
		super(builder);
	}

	@Override
	protected String classNameOf(final CMCard input) {
		return input.getType().getName();
	}

	@Override
	protected Long idOf(final CMCard input) {
		return input.getId();
	}

	@Override
	protected Iterable<Entry<String, Object>> valuesOf(final CMCard input) {
		return input.getAllValues();
	}

}

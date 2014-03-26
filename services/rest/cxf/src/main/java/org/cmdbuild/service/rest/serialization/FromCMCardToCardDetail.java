package org.cmdbuild.service.rest.serialization;

import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMCard;

public class FromCMCardToCardDetail extends FromSomeKindOfCardToMap<CMCard> {

	public static Builder<CMCard> newInstance() {
		return new Builder<CMCard>() {

			@Override
			protected FromSomeKindOfCardToMap<CMCard> doBuild() {
				return new FromCMCardToCardDetail(this);
			};

		};
	}

	private FromCMCardToCardDetail(final Builder<CMCard> builder) {
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
	protected boolean idAndClassnameRequired() {
		return false;
	}

	@Override
	protected Iterable<Entry<String, Object>> valuesOf(final CMCard input) {
		return input.getAllValues();
	}

}

package org.cmdbuild.data.store.dao;

import static org.cmdbuild.data.store.dao.DataViewStore.DEFAULT_IDENTIFIER_ATTRIBUTE_NAME;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.logic.data.Utils;
import org.slf4j.Logger;

public abstract class BaseStorableConverter<T extends Storable> implements StorableConverter<T> {

	protected static Logger logger = DataViewStore.logger;

	private static String SYSTEM_USER = "system"; // FIXME

	@Override
	public String getIdentifierAttributeName() {
		return DEFAULT_IDENTIFIER_ATTRIBUTE_NAME;
	}

	@Override
	public Storable storableOf(final CMCard card) {
		return new Storable() {

			@Override
			public String getIdentifier() {
				final String attributeName = getIdentifierAttributeName();
				final String value;
				if (DEFAULT_IDENTIFIER_ATTRIBUTE_NAME.equals(attributeName)) {
					value = Long.toString(card.getId());
				} else {
					value = card.get(getIdentifierAttributeName(), String.class);
				}
				return value;
			}

		};
	}

	@Override
	public String getUser(final T storable) {
		return SYSTEM_USER;
	};

	// TODO use static methods directly instead
	protected String readStringAttribute(final CMCard card, final String attributeName) {
		return Utils.readString(card, attributeName);
	}

	// TODO use static methods directly instead
	protected Long readLongAttribute(final CMCard card, final String attributeName) {
		return Utils.readLong(card, attributeName);
	}

}
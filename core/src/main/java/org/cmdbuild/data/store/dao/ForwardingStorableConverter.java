package org.cmdbuild.data.store.dao;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.Storable;

public class ForwardingStorableConverter<T extends Storable> implements StorableConverter<T> {

	private final StorableConverter<T> inner;

	protected ForwardingStorableConverter(final StorableConverter<T> storableConverter) {
		this.inner = storableConverter;
	}

	@Override
	public String getClassName() {
		return inner.getClassName();
	}

	@Override
	public String getIdentifierAttributeName() {
		return inner.getIdentifierAttributeName();
	}

	@Override
	public Storable storableOf(final CMCard card) {
		return inner.storableOf(card);
	}

	@Override
	public T convert(final CMCard card) {
		return inner.convert(card);
	}

	@Override
	public Map<String, Object> getValues(final T storable) {
		return inner.getValues(storable);
	}

	@Override
	public String getUser(final T storable) {
		return inner.getUser(storable);
	}

}
package org.cmdbuild.services.sync.store;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingStore extends ForwardingObject implements Store {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingStore() {
	}

	@Override
	protected abstract Store delegate();

	@Override
	public void create(final Entry<? extends Type> entry) {
		delegate().create(entry);
	}

	@Override
	public Iterable<Entry<?>> readAll() {
		return delegate().readAll();
	}

	@Override
	public void update(final Entry<? extends Type> entry) {
		delegate().update(entry);
	}

	@Override
	public void delete(final Entry<? extends Type> entry) {
		delegate().delete(entry);
	}

}

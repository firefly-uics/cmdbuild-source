package org.cmdbuild.data.store;

import java.util.List;

public class ForwardingStore<T extends Storable> implements Store<T> {

	private final Store<T> inner;

	public ForwardingStore(final Store<T> inner) {
		this.inner = inner;
	}

	@Override
	public Storable create(final T storable) {
		return inner.create(storable);
	}

	@Override
	public T read(final Storable storable) {
		return inner.read(storable);
	}

	@Override
	public void update(final T storable) {
		inner.update(storable);
	}

	@Override
	public void delete(final Storable storable) {
		inner.delete(storable);

	}

	@Override
	public List<T> list() {
		return inner.list();
	}

	@Override
	public List<T> list(final Groupable groupable) {
		return inner.list(groupable);
	}

}

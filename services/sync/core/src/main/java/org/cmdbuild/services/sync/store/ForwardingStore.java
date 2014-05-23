package org.cmdbuild.services.sync.store;

public abstract class ForwardingStore implements Store {

	private final Store delegate;

	protected ForwardingStore(final Store store) {
		this.delegate = store;
	}

	@Override
	public void create(final Entry<? extends Type> entry) {
		delegate.create(entry);
	}

	@Override
	public Iterable<Entry<?>> readAll() {
		return delegate.readAll();
	}

	@Override
	public void update(final Entry<? extends Type> entry) {
		delegate.update(entry);
	}

	@Override
	public void delete(final Entry<? extends Type> entry) {
		delegate.delete(entry);
	}

}

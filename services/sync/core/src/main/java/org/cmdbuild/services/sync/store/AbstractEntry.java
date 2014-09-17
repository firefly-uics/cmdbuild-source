package org.cmdbuild.services.sync.store;

abstract class AbstractEntry<T extends Type> implements Entry<T> {

	@Override
	public final boolean equals(final Object obj) {
		return doEquals(obj);
	}

	protected abstract boolean doEquals(final Object obj);

	@Override
	public final int hashCode() {
		return doHashCode();
	}

	protected abstract int doHashCode();

	@Override
	public String toString() {
		return doToString();
	}

	protected abstract String doToString();

}

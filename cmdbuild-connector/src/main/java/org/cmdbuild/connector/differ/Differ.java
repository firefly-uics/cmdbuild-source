package org.cmdbuild.connector.differ;

public interface Differ<T, V> {

	public void addListener(final DifferListener<V> listener);

	public void removeListener(final DifferListener<V> listener);

	public void diff() throws DifferException;

}

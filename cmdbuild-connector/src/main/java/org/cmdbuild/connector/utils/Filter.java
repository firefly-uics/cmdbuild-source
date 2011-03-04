package org.cmdbuild.connector.utils;

public interface Filter<T> {

	public boolean accept(final T element);

}
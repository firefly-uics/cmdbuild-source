package org.cmdbuild.common.utils;

import java.util.Iterator;

public class PaginatedElements<T> implements Iterable<T> {

	private final Iterable<T> elements;
	private final int totalSize;

	public PaginatedElements(final Iterable<T> elements, final int totalSize) {
		this.totalSize = totalSize;
		this.elements = elements;
	}

	@Override
	public Iterator<T> iterator() {
		return elements.iterator();
	}

	public Iterable<T> paginatedElements() {
		return elements;
	}

	public int totalSize() {
		return totalSize;
	}

}

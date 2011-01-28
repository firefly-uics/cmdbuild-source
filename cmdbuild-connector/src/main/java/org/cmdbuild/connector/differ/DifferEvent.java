package org.cmdbuild.connector.differ;

import java.util.EventObject;

public class DifferEvent<T> extends EventObject {

	private static final long serialVersionUID = 1L;

	private final T value;

	public DifferEvent(final Object source, final T value) {
		super(source);
		this.value = value;
	}

	public T getValue() {
		return value;
	}

}

package org.cmdbuild.connector.parser;

import java.util.EventObject;

import org.apache.commons.lang.Validate;

public class ParserEvent<T> extends EventObject {

	private static final long serialVersionUID = 1L;

	private final T value;

	public ParserEvent(final Parser source, final T value) {
		super(source);
		Validate.notNull(value, "null value");
		this.value = value;
	}

	public T getValue() {
		return value;
	}

}

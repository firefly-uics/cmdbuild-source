package org.cmdbuild.connector.data;

import org.apache.commons.lang.Validate;

public class ConnectorCardAttribute implements Comparable<ConnectorCardAttribute> {

	private final String name;
	private final String value;
	private final Key reference;

	public ConnectorCardAttribute(final String name, final String value) {
		Validate.notNull(name, "null name");
		Validate.notEmpty(name, "empty name");
		this.name = name;
		this.value = value;
		this.reference = null;
	}

	public ConnectorCardAttribute(final String name, final Key reference) {
		Validate.notNull(name, "null name");
		Validate.notEmpty(name, "empty name");
		Validate.notNull(reference, "null reference");
		this.name = name;
		this.value = null;
		this.reference = reference;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public Key getReference() {
		return reference;
	}

	public boolean isReference() {
		return (reference != null);
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) {
			return false;
		}

		if (object instanceof ConnectorCardAttribute) {
			final ConnectorCardAttribute attribute = ConnectorCardAttribute.class.cast(object);
			return (compareTo(attribute) == 0);
		}

		return false;
	}

	@Override
	public int compareTo(final ConnectorCardAttribute attribute) {
		return name.compareTo(attribute.name);
	}

	@Override
	public String toString() {
		if (isReference()) {
			return String.format("[name:%s, reference:%s]", name, reference);
		} else {
			return String.format("[name:%s, value:%s]", name, value);
		}
	}

}

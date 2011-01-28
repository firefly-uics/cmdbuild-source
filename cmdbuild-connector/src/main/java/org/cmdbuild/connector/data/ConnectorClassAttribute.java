package org.cmdbuild.connector.data;

import org.apache.commons.lang.Validate;

public class ConnectorClassAttribute implements Comparable<ConnectorClassAttribute> {

	private final String name;
	private final boolean isKey;
	private final ConnectorClass referencedClass;

	public ConnectorClassAttribute(final String name) {
		this(name, false, null);
	}

	public ConnectorClassAttribute(final String name, final boolean isKey) {
		this(name, isKey, null);
	}

	public ConnectorClassAttribute(final String name, final ConnectorClass referencedClass) {
		this(name, false, referencedClass);
	}

	public ConnectorClassAttribute(final String name, final boolean isKey, final ConnectorClass referencedClass) {
		Validate.notNull(name, "null name");
		Validate.notEmpty(name, "empty name");
		this.name = name;
		this.isKey = isKey;
		this.referencedClass = referencedClass;
	}

	public String getName() {
		return name;
	}

	public boolean isKey() {
		return isKey;
	}

	public boolean isReference() {
		return (referencedClass != null);
	}

	public ConnectorClass getReferencedClass() {
		return referencedClass;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) {
			return false;
		}

		if (object instanceof ConnectorClassAttribute) {
			final ConnectorClassAttribute attribute = ConnectorClassAttribute.class.cast(object);
			return (compareTo(attribute) == 0);
		}

		return false;
	}

	@Override
	public int compareTo(final ConnectorClassAttribute attribute) {
		return name.compareTo(attribute.name);
	}

	@Override
	public String toString() {
		if (isReference()) {
			return String.format("[name:%s, isKey:%b, reference:%s]", name, isKey, referencedClass);
		} else {
			return String.format("[name:%s, isKey:%b]", name, isKey);
		}
	}

}

package org.cmdbuild.connector.data;

import org.apache.commons.lang.Validate;

public class ConnectorDomainAttribute implements Comparable<ConnectorDomainAttribute> {

	public final String name;

	public ConnectorDomainAttribute(final String name) {
		Validate.notNull(name, "null name");
		Validate.notEmpty(name, "empty name");
		this.name = name;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) {
			return false;
		}

		if (object instanceof ConnectorDomainAttribute) {
			final ConnectorDomainAttribute attribute = ConnectorDomainAttribute.class.cast(object);
			return (compareTo(attribute) == 0);
		}

		return false;
	}

	@Override
	public int compareTo(final ConnectorDomainAttribute attribute) {
		return name.compareTo(attribute.name);
	}

	@Override
	public String toString() {
		return String.format("[name:%s]", name);
	}

}

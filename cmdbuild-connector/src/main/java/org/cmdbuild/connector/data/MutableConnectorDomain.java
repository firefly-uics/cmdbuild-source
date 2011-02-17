package org.cmdbuild.connector.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;

public class MutableConnectorDomain implements ConnectorDomain {

	private final String name;
	private final List<ConnectorClass> classes;
	private final Set<ConnectorDomainAttribute> attributes;

	public MutableConnectorDomain(final String name, final List<ConnectorClass> classes) {
		this(name, classes, new HashSet<ConnectorDomainAttribute>());
	}

	public MutableConnectorDomain(final String name, final List<ConnectorClass> classes,
			final Set<ConnectorDomainAttribute> attributes) {
		Validate.notNull(name, "null name");
		Validate.notEmpty(name, "empty name");
		Validate.notNull(classes, "null classes");
		Validate.isTrue(classes.size() >= 2, "not enough classes");
		Validate.notNull(attributes, "null arguments");
		this.name = name;
		this.classes = classes;
		this.attributes = new HashSet<ConnectorDomainAttribute>(attributes);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<ConnectorClass> getConnectorClasses() {
		return classes;
	}

	@Override
	public Set<ConnectorDomainAttribute> getAttributes() {
		return attributes;
	}

	@Override
	public int compareTo(final ConnectorDomain connectorDomain) {
		return name.compareTo(connectorDomain.getName());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) {
			return false;
		}

		if (object instanceof MutableConnectorDomain) {
			final MutableConnectorDomain connectorDomain = MutableConnectorDomain.class.cast(object);
			return (compareTo(connectorDomain) == 0);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return classes.hashCode();
	}

	@Override
	public String toString() {
		return String.format("[name:%s, attributes:%s]", name, attributes);
	}

}

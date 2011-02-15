package org.cmdbuild.connector.data;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.Validate;
import org.cmdbuild.connector.utils.Filter;

public class MutableConnectorClass implements ConnectorClass {

	private final String name;
	private final SortedSet<ConnectorClassAttribute> attributes;
	private final String cqlQuery;

	public MutableConnectorClass(final String name, final SortedSet<ConnectorClassAttribute> attributes) {
		this(name, attributes, null);
	}

	public MutableConnectorClass(final String name, final SortedSet<ConnectorClassAttribute> attributes,
			final String cqlQuery) {
		Validate.notNull(name, "null name");
		Validate.notEmpty(name, "empty name");
		Validate.notNull(attributes, "null attributes");
		Validate.notEmpty(attributes, "missing attributes");

		int keys = 0;
		for (final ConnectorClassAttribute attribute : attributes) {
			if (attribute.isKey()) {
				keys++;
			}
		}

		Validate.isTrue(keys > 0, "missing key attributes");

		this.name = name;
		this.attributes = new TreeSet<ConnectorClassAttribute>(attributes);
		this.cqlQuery = cqlQuery;
	}

	@Override
	public String getName() {
		return name;
	}

	private SortedSet<ConnectorClassAttribute> getAttributes(final Filter<ConnectorClassAttribute> filter) {
		final SortedSet<ConnectorClassAttribute> set = new TreeSet<ConnectorClassAttribute>();
		for (final ConnectorClassAttribute attribute : attributes) {
			if (filter.accept(attribute)) {
				set.add(attribute);
			}
		}
		return set;
	}

	@Override
	public SortedSet<ConnectorClassAttribute> getAttributes() {
		return getAttributes(new Filter<ConnectorClassAttribute>() {
			@Override
			public boolean accept(final ConnectorClassAttribute attribute) {
				return true;
			}
		});
	}

	@Override
	public SortedSet<ConnectorClassAttribute> getKeyAttributes() {
		return getAttributes(new Filter<ConnectorClassAttribute>() {
			@Override
			public boolean accept(final ConnectorClassAttribute attribute) {
				return attribute.isKey();
			}
		});
	}

	@Override
	public String getCqlQueryString() {
		return cqlQuery;
	}

	@Override
	public boolean hasAttribute(final String name) {
		for (final ConnectorClassAttribute attribute : attributes) {
			if (attribute.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ConnectorClassAttribute getAttribute(final String name) {
		for (final ConnectorClassAttribute attribute : attributes) {
			if (attribute.getName().equals(name)) {
				return attribute;
			}
		}
		return null;
	}

	@Override
	public int compareTo(final ConnectorClass connectorClass) {
		return name.compareTo(connectorClass.getName());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) {
			return false;
		}

		if (object instanceof MutableConnectorClass) {
			final MutableConnectorClass connectorClass = MutableConnectorClass.class.cast(object);
			return (compareTo(connectorClass) == 0);
		}

		return false;
	}

	@Override
	public String toString() {
		return String.format("[name:%s, attributes:%s]", name, attributes);
	}

}

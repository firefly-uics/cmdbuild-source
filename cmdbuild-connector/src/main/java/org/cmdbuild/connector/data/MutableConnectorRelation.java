package org.cmdbuild.connector.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

public class MutableConnectorRelation implements ConnectorRelation {

	private final ConnectorDomain domain;
	private final List<Key> keys;

	public MutableConnectorRelation(final ConnectorDomain domain, final Key... keys) {
		this(domain, (keys == null) ? new ArrayList<Key>() : Arrays.asList(keys));
	}

	public MutableConnectorRelation(final ConnectorDomain domain, final List<Key> keys) {
		Validate.notNull(domain, "null domain");
		Validate.notNull(keys, "null cards");
		Validate.notEmpty(keys, "empty cards");
		Validate.isTrue(domain.getConnectorClasses().size() == keys.size(), "domain and keys mismatch");
		this.domain = domain;
		this.keys = new ArrayList<Key>(keys);
	}

	@Override
	public ConnectorDomain getConnectorDomain() {
		return domain;
	}

	@Override
	public List<Key> getKeys() {
		return keys;
	}

	@Override
	public int compareTo(final ConnectorRelation relation) {
		int i = 0;
		for (final Key key : relation.getKeys()) {
			final int compare = key.compareTo(keys.get(i++));
			if (compare != 0) {
				return compare;
			}
		}
		return 0;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) {
			return false;
		}

		if (object instanceof MutableConnectorRelation) {
			final MutableConnectorRelation connectorRelation = MutableConnectorRelation.class.cast(object);
			if (domain.equals(connectorRelation.domain)) {
				return (compareTo(connectorRelation) == 0);
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return keys.hashCode();
	}

	@Override
	public String toString() {
		return String.format("[domain:%s, cards:%s]", domain, keys);
	}

}

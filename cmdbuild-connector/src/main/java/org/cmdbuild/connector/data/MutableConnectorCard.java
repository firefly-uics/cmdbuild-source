package org.cmdbuild.connector.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.cmdbuild.connector.logger.Log;

public class MutableConnectorCard implements ConnectorCard {

	private static final Logger logger = Log.CONNECTOR;

	private final ConnectorClass connectorClass;
	private final Map<String, ConnectorCardAttribute> attributes;
	private Key key;
	private Integer id;

	public MutableConnectorCard(final ConnectorClass connectorClass) {
		Validate.notNull(connectorClass, "null class");
		this.connectorClass = connectorClass;
		this.attributes = new HashMap<String, ConnectorCardAttribute>();
	}

	@Override
	public ConnectorClass getConnectorClass() {
		return connectorClass;
	}

	@Override
	public Collection<ConnectorCardAttribute> getAttributes() {
		final Collection<ConnectorCardAttribute> attributes = new ArrayList<ConnectorCardAttribute>();
		for (final String name : getAttributeNames()) {
			final ConnectorCardAttribute attribute = getAttribute(name);
			attributes.add(attribute);
		}
		return attributes;
	}

	@Override
	public Set<String> getAttributeNames() {
		return attributes.keySet();
	}

	@Override
	public boolean hasAttribute(final String name) {
		return attributes.containsKey(name);
	}

	@Override
	public ConnectorCardAttribute getAttribute(final String name) {
		if (!hasAttribute(name)) {
			return new ConnectorCardAttribute(name, (String) null);
		}
		return attributes.get(name);
	}

	public void setAttribute(final String name, final String value) {
		final ConnectorCardAttribute attribute = new ConnectorCardAttribute(name, value);
		setAttribute(attribute);
	}

	public void setAttribute(final String name, final Key key) {
		final ConnectorCardAttribute attribute = new ConnectorCardAttribute(name, key);
		setAttribute(attribute);
	}

	private void setAttribute(final ConnectorCardAttribute attribute) {
		logger.debug("setting attribute " + attribute);
		attributes.put(attribute.getName(), attribute);
	}

	public void setId(final int id) {
		logger.debug("setting id " + id);
		this.id = id;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public Key getKey() {
		if (key == null) {
			final List<KeyValue> keyValues = new ArrayList<KeyValue>();
			for (final ConnectorClassAttribute keyAttribute : connectorClass.getKeyAttributes()) {
				final String keyAttributeName = keyAttribute.getName();
				final ConnectorCardAttribute cardAttribute = attributes.get(keyAttributeName);

				Validate.notNull(cardAttribute, "null key attribute '" + keyAttributeName + "'");

				if (cardAttribute.isReference()) {
					final Key reference = cardAttribute.getReference();
					Validate.notNull(reference, "null reference for key's attribute '" + keyAttributeName + "'");
				} else {
					final String value = cardAttribute.getValue();
					Validate.notNull(value, "null value for key's attribute '" + keyAttributeName + "'");
					Validate.notEmpty(value, "empty value for key's attribute '" + keyAttributeName + "'");
				}

				final KeyValue keyValue = getKeyValue(cardAttribute);
				keyValues.add(keyValue);
			}
			key = new Key(keyValues);
		}
		return key;
	}

	private static KeyValue getKeyValue(final ConnectorCardAttribute cardAttribute) {
		if (cardAttribute.isReference()) {
			final Key key = cardAttribute.getReference();
			return Key.createKeyValue(key);
		} else {
			final String value = cardAttribute.getValue();
			return Key.createKeyValue(value);
		}
	}

	@Override
	public int compareTo(final ConnectorCard card) {
		return getKey().compareTo(card.getKey());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) {
			return false;
		}

		if (object instanceof MutableConnectorCard) {
			final MutableConnectorCard connectorCard = MutableConnectorCard.class.cast(object);
			if (connectorClass.equals(connectorCard.connectorClass)) {
				return (compareTo(connectorCard) == 0);
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public String toString() {
		return String.format("[class:%s, attributes:%s]", connectorClass, attributes);
	}

}

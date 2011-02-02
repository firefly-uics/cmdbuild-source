package org.cmdbuild.connector.data;

import java.util.Collection;
import java.util.Set;

public interface ConnectorCard extends Comparable<ConnectorCard> {

	public ConnectorClass getConnectorClass();

	public Collection<ConnectorCardAttribute> getAttributes();

	public Set<String> getAttributeNames();

	public boolean hasAttribute(final String name);

	public ConnectorCardAttribute getAttribute(final String name);

	public Key getKey();

	public Integer getId();

}
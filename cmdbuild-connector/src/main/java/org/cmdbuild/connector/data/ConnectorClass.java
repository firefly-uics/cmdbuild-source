package org.cmdbuild.connector.data;

import java.util.SortedSet;

public interface ConnectorClass extends Comparable<ConnectorClass> {

	public String getName();

	public SortedSet<ConnectorClassAttribute> getAttributes();

	public boolean hasAttribute(final String name);

	public ConnectorClassAttribute getAttribute(final String name);

	public SortedSet<ConnectorClassAttribute> getKeyAttributes();

	public String getCqlQueryString();

}

package org.cmdbuild.connector.data;

import java.util.List;

public interface ConnectorRelation extends Comparable<ConnectorRelation> {

	public ConnectorDomain getConnectorDomain();

	public List<Key> getKeys();

}

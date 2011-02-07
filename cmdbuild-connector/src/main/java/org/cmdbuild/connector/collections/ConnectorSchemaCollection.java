package org.cmdbuild.connector.collections;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.connector.data.ConnectorClass;
import org.cmdbuild.connector.data.ConnectorDomain;

public class ConnectorSchemaCollection {

	private final List<ConnectorClass> connectorClasses;
	private final List<ConnectorDomain> connectorDomains;

	public ConnectorSchemaCollection() {
		connectorClasses = new ArrayList<ConnectorClass>();
		connectorDomains = new ArrayList<ConnectorDomain>();
	}

	public void addConnectorClass(final ConnectorClass connectorClass) {
		connectorClasses.add(connectorClass);
	}

	public void addConnectorDomain(final ConnectorDomain connectorDomain) {
		connectorDomains.add(connectorDomain);
	}

	public List<ConnectorClass> getConnectorClasses() {
		return connectorClasses;
	}

	public List<ConnectorDomain> getConnectorDomains() {
		return connectorDomains;
	}

}

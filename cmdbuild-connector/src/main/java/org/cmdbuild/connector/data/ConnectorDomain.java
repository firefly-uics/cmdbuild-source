package org.cmdbuild.connector.data;

import java.util.List;
import java.util.Set;

public interface ConnectorDomain extends Comparable<ConnectorDomain> {

	public String getName();

	public List<ConnectorClass> getConnectorClasses();

	public Set<ConnectorDomainAttribute> getAttributes();

}

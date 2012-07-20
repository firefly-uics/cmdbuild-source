package org.cmdbuild.elements.proxy;

import java.util.Iterator;

import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.DomainQuery;

public class DomainQueryForwarder implements DomainQuery {
	protected DomainQuery domainQuery;

	protected DomainQueryForwarder(DomainQuery domainQuery) {
		this.domainQuery = domainQuery;
	}

	public DomainQuery inherited() { domainQuery.inherited(); return this; }

	public String getTableName() { return domainQuery.getTableName(); }
	public boolean isInherited() { return domainQuery.isInherited(); }
	public Iterator<IDomain> iterator() { return domainQuery.iterator(); }
}

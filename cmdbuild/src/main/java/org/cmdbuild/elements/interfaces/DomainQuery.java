package org.cmdbuild.elements.interfaces;

import java.util.Iterator;

public interface DomainQuery extends Iterable<IDomain> {

	public DomainQuery inherited();

	public String getTableName();
	public boolean isInherited();
	public Iterator<IDomain> iterator();
}

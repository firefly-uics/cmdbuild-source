package org.cmdbuild.elements;

import java.util.Iterator;

import org.cmdbuild.dao.backend.postgresql.CMBackend;
import org.cmdbuild.elements.interfaces.DomainQuery;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;


public class DomainQueryImpl implements DomainQuery {

	private static final CMBackend backend = new CMBackend();
	private ITable table;
	private boolean inherited;

    public DomainQueryImpl(ITable table) {
    	this.table = table;
    	this.inherited = false;
    }

	public DomainQuery inherited() {
		this.inherited = true;
		return this;
	}

	public ITable getTable() {
		return table;
	}

	public String getTableName() {
		return table.getName();
	}

	public boolean isInherited() {
		return inherited;
	}

	public Iterator<IDomain> iterator() {
		return backend.getDomainList(this);
	}
}

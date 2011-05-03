package org.cmdbuild.elements;

import java.util.Iterator;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.interfaces.DomainQuery;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.springframework.beans.factory.annotation.Autowired;


public class DomainQueryImpl implements DomainQuery {

	@Autowired
	private CMBackend backend = CMBackend.INSTANCE;

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

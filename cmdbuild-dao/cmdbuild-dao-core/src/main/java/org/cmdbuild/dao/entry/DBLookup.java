package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMLookupType;

public class DBLookup implements CMLookup {

	private Object id;

	public DBLookup(final Object id) {
		this.id = id;
	}

	@Override
	public CMLookupType getType() {
		return null;
	}

	@Override
	public Object getId() {
		return id;
	}

}

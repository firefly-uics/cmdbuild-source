package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMLookupType;

public class DBLookup implements CMLookup {

	private final CMLookupType type;
	private final Object id;

	public DBLookup(final CMLookupType type, final Object id) {
		this.type = type;
		this.id = id;
	}

	@Override
	public CMLookupType getType() {
		return type;
	}

	@Override
	public Object getId() {
		return id;
	}

	@Override
	public String getCode() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Override
	public String getDescription() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
}

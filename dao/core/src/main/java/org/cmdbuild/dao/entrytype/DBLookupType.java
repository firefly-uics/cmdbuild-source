package org.cmdbuild.dao.entrytype;

public class DBLookupType implements CMLookupType {

	final String name;

	public DBLookupType(final String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CMLookupType getParent() {
		// TODO
		return null;
	}

}

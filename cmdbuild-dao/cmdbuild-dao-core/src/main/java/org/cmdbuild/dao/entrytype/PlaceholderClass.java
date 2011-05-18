package org.cmdbuild.dao.entrytype;


public abstract class PlaceholderClass implements CMClass {

	@Override
	public Object getId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CMClass getParent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<? extends CMClass> getChildren() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<? extends CMAttribute> getAttributes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DBAttribute getAttribute(String name) {
		throw new UnsupportedOperationException();
	}
}

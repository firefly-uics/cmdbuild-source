package org.cmdbuild.dao.entrytype;


public abstract class PlaceholderClass extends PlaceholderEntryType implements CMClass {

	@Override
	public CMClass getParent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<? extends CMClass> getChildren() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSuperclass() {
		throw new UnsupportedOperationException();
	}
}

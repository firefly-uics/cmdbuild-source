package org.cmdbuild.dao.entrytype;

import java.util.Set;


public abstract class PlaceholderClass extends PlaceholderEntryType implements CMClass {

	@Override
	public String getName() {
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
	public boolean isAncestorOf(CMClass cmClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSuperclass() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<? extends CMClass> getLeaves() {
		throw new UnsupportedOperationException();
	}
}

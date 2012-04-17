package org.cmdbuild.dao.entrytype;


public abstract class PlaceholderEntryType implements CMEntryType {

	@Override
	public Object getId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDescription() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<? extends CMAttribute> getAttributes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<? extends CMAttribute> getAllAttributes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DBAttribute getAttribute(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isActive() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSystem() {
		throw new UnsupportedOperationException();
	}

	public final String getPrivilegeId() {
		throw new UnsupportedOperationException();
	}
}

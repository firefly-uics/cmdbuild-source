package org.cmdbuild.dao.entrytype;

public abstract class PlaceholderEntryType implements CMEntryType {

	@Override
	public Long getId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDescription() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<? extends CMAttribute> getActiveAttributes() {
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
	public CMAttribute getAttribute(final String name) {
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

	@Override
	public boolean isSystemButUsable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isBaseClass() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final String getPrivilegeId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getKeyAttributeName() {
		throw new UnsupportedOperationException();
	}
}

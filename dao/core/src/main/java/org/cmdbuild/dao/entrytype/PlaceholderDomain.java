package org.cmdbuild.dao.entrytype;

public abstract class PlaceholderDomain extends PlaceholderEntryType implements CMDomain {

	@Override
	public final void accept(final CMEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public CMClass getClass1() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CMClass getClass2() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDescription1() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDescription2() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean holdsHistory() {
		throw new UnsupportedOperationException();
	}
}

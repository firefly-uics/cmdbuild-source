package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMClass;

public class ForwardingCard extends ForwardingEntry implements CMCard {

	private final CMCard inner;

	public ForwardingCard(final CMCard inner) {
		super(inner);
		this.inner = inner;
	}

	@Override
	public CMClass getType() {
		return inner.getType();
	}

	@Override
	public Object getCode() {
		return inner.getCode();
	}

	@Override
	public Object getDescription() {
		return inner.getDescription();
	}

}

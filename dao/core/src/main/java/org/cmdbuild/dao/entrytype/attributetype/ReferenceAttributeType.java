package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.reference.CardReference;

public class ReferenceAttributeType extends AbstractAttributeType<CardReference> {

	public final String domain;

	public ReferenceAttributeType(final String domain) {
		this.domain = domain;
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected CardReference convertNotNullValue(final Object value) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}

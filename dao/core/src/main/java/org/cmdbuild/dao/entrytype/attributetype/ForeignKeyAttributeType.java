package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.reference.CardReference;

public class ForeignKeyAttributeType extends AbstractAttributeType<CardReference> {

	public ForeignKeyAttributeType() {
		// TODO Target class needs to be provided
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public CardReference convertNotNullValue(final Object value) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}

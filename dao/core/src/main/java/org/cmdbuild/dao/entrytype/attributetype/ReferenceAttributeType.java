package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.reference.CardReference;


public class ReferenceAttributeType extends AbstractAttributeType<CardReference> {

	public ReferenceAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected CardReference convertNotNullValue(Object value) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}

package org.cmdbuild.dao.entrytype.attributetype;


public class ReferenceAttributeType extends AbstractCardAttributeType {

	public ReferenceAttributeType() {
		// Reference domain needs to be provided
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String getClassName() {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}

package org.cmdbuild.dao.entrytype.attributetype;


public class ForeignKeyAttributeType extends AbstractCardAttributeType {

	public ForeignKeyAttributeType() {
		// TODO Target class needs to be provided
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

package org.cmdbuild.dao.entrytype.attributetype;


public class DateAttributeType extends AbstractDateAttributeType {

	public DateAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	// TODO Do the conversion properly
}

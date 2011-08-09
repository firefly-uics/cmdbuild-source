package org.cmdbuild.dao.entrytype.attributetype;


public class DateTimeAttributeType extends AbstractDateAttributeType {

	public DateTimeAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	// TODO Do the conversion properly
}

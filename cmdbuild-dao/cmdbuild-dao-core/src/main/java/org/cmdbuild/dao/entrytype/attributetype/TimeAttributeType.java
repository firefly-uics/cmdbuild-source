package org.cmdbuild.dao.entrytype.attributetype;


public class TimeAttributeType extends AbstractDateAttributeType {

	public TimeAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	// TODO Do the conversion properly
}

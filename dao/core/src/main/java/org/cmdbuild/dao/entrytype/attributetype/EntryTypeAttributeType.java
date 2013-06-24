package org.cmdbuild.dao.entrytype.attributetype;

public class EntryTypeAttributeType extends AbstractReferenceAttributeType {

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

}

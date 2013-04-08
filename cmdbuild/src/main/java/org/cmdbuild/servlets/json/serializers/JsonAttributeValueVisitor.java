package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;

public class JsonAttributeValueVisitor extends AbstractAttributeValueVisitor {

	public JsonAttributeValueVisitor(final CMAttributeType<?> type, final Object value) {
		super(type, value);
	}

	@Override
	public void visit(final EntryTypeAttributeType attributeType) {
		convertedValue = value;
	}

	@Override
	public void visit(final LookupAttributeType attributeType) {
		convertedValue = value;
	}

	@Override
	public void visit(final ReferenceAttributeType attributeType) {
		convertedValue = value;
	}

}

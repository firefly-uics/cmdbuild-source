package org.cmdbuild.servlets.json.serializers;

import java.util.HashMap;

import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;

public class JsonAttributeValueVisitor extends AbstractAttributeValueVisitor {

	public JsonAttributeValueVisitor(final CMAttributeType<?> type, final Object value) {
		super(type, value);
	}

	@Override
	@SuppressWarnings("serial")
	public void visit(final LookupAttributeType attributeType) {
		if (value instanceof CMLookup) {
			final CMLookup lookup = (CMLookup) value;

			convertedValue = new HashMap<String, Object>() {
				{
					put("id", lookup.getId());
					put("description", lookup.getDescription());
				}
			};
		} else {
			convertedValue = value;
		}
	}

	@Override
	public void visit(final ReferenceAttributeType attributeType) {
		convertedValue = value;
	}

}

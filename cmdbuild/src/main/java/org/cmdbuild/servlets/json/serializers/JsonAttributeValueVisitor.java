package org.cmdbuild.servlets.json.serializers;

import java.util.HashMap;

import org.cmdbuild.dao.entry.CMLookup;
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
		// FIXME
		convertedValue = value;
	}

	@Override
	@SuppressWarnings("serial")
	public void visit(final LookupAttributeType attributeType) {
		if (value instanceof CMLookup) {
			final CMLookup lookup = (CMLookup) value;
			convertedValue = new HashMap<String, Object>() {
				{
					put("id", lookup.getId());
					put("description", getChainedDescription(lookup));
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

	private String getChainedDescription(final CMLookup lookup) {
		String description = lookup.getDescription();
		final String concatFormat = "%s - %s";
		CMLookup parent = lookup.getParent();
		while (parent != null) {
			description = String.format(concatFormat, parent.getDescription(), description);
			parent = parent.getParent();
		}
		return description;
	}

}

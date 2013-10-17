package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;

import java.util.Map;

import org.cmdbuild.dao.entry.CardReference;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;

import com.google.common.collect.Maps;

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
		if (value instanceof CardReference) {
			final CardReference cardReference = CardReference.class.cast(value);
			convertedValue = mapOf(cardReference);
		} else {
			convertedValue = value;
		}
	}

	@Override
	public void visit(final ReferenceAttributeType attributeType) {
		if (value instanceof CardReference) {
			final CardReference cardReference = CardReference.class.cast(value);
			convertedValue = mapOf(cardReference);
		} else {
			convertedValue = value;
		}
	}

	private Object mapOf(CardReference cardReference) {
		final Map<String, Object> map = Maps.newHashMap();
		map.put(ID, cardReference.getId());
		map.put(DESCRIPTION, cardReference.getDescription());
		return map;
	}

}

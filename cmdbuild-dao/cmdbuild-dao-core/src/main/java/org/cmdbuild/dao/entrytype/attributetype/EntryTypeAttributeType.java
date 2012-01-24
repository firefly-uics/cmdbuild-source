package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.reference.CMReference;
import org.cmdbuild.dao.reference.EntryTypeReference;

public class EntryTypeAttributeType implements CMAttributeType<EntryTypeReference> {

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public EntryTypeReference convertNotNullValue(Object value) {
		if (value instanceof CMReference) {
			if (value instanceof EntryTypeReference) {
				return (EntryTypeReference) value;
			}
			value = ((CMReference)value).getId();
		}
		return EntryTypeReference.newInstance(value);
	}

}

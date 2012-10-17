package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.reference.CMReference;
import org.cmdbuild.dao.reference.EntryTypeReference;

public class EntryTypeAttributeType extends AbstractAttributeType<EntryTypeReference> {

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public EntryTypeReference convertNotNullValue(Object value) {
		if (value instanceof CMReference) {
			if (value instanceof EntryTypeReference) {
				return EntryTypeReference.class.cast(value);
			}
			final Long id = CMReference.class.cast(value).getId();
			return EntryTypeReference.newInstance(id);
		}
		throw new IllegalArgumentException(String.format("value is not a reference ('%s')", value.getClass()));
	}

}

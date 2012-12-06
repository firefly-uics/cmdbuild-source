package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.reference.EntryTypeReference;

public class EntryTypeAttributeType extends AbstractAttributeType<EntryTypeReference> {

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public EntryTypeReference convertNotNullValue(final Object value) {
		if (value instanceof EntryTypeReference) {
			return EntryTypeReference.class.cast(value);
		} else if (value instanceof Number) {
			final long l = Number.class.cast(value).longValue();
			return EntryTypeReference.newInstance(l);
		} else {
			throw new IllegalArgumentException(String.format("value is not a reference ('%s')", value.getClass()));
		}
	}

}

package org.cmdbuild.dao.entrytype.attributetype;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.dao.entry.CardReference;

public abstract class AbstractReferenceAttributeType extends AbstractAttributeType<CardReference> {

	@Override
	protected CardReference convertNotNullValue(final Object value) {
		if (value instanceof CardReference) {
			return CardReference.class.cast(value);
		}
		if (value instanceof Number) {
			return new CardReference(Number.class.cast(value).longValue(), StringUtils.EMPTY);
		} else if (value instanceof String) {
			final Long converted;
			if (StringUtils.isBlank(String.class.cast(value))) {
				converted = null;
			} else {
				converted = Long.parseLong(String.class.cast(value));
			}
			return new CardReference(converted, StringUtils.EMPTY);
		} else {
			throw illegalValue(value);
		}
	}

}

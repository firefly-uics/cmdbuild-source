package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.reference.CMReference;
import org.cmdbuild.dao.reference.CardReference;


public abstract class AbstractCardAttributeType implements CMAttributeType<CardReference> {

	@Override
	public CardReference convertNotNullValue(Object value) {
		if (value instanceof CMReference) {
			if (value instanceof CardReference) {
				return (CardReference) value;
			}
			value = ((CMReference)value).getId();
		}
		return CardReference.newInstance(getClassName(), value);
	}

	public abstract String getClassName();
}

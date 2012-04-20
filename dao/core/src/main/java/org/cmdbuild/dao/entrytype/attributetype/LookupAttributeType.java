package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.reference.CMReference;
import org.cmdbuild.dao.reference.LookupReference;


public class LookupAttributeType implements CMAttributeType<LookupReference> {

	public final String lookupTypeName;

	public LookupAttributeType(final String lookupTypeName) {
		this.lookupTypeName = lookupTypeName;
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public LookupReference convertNotNullValue(Object value) {
		if (value instanceof CMReference) {
			if (value instanceof LookupReference) {
				return (LookupReference) value;
			}
			value = ((CMReference)value).getId();
		}
		return LookupReference.newInstance(lookupTypeName, value);
	}
}

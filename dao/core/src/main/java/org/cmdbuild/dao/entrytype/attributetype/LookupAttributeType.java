package org.cmdbuild.dao.entrytype.attributetype;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.dao.entrytype.DBLookupType;

public class LookupAttributeType extends AbstractAttributeType<Long> {

	private final DBLookupType lookupType;
	private final transient String toString;

	public LookupAttributeType(final String lookupTypeName) {
		this.lookupType = new DBLookupType(lookupTypeName);
		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public String getLookupTypeName() {
		return lookupType.getName();
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected Long convertNotNullValue(final Object value) {
		if (value instanceof Number) {
			return Number.class.cast(value).longValue();
		} else if (value instanceof String) {
			return Long.parseLong(String.class.cast(value));
		} else {
			throw illegalValue(value);
		}
	}

	@Override
	public String toString() {
		return toString;
	}

}

package org.cmdbuild.dao.entrytype.attributetype;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.dao.entry.CardReference;

public class LookupAttributeType extends AbstractReferenceAttributeType {

	private final String lookupTypeName;
	private final transient String toString;

	public LookupAttributeType(final String lookupTypeName) {
		this.lookupTypeName = lookupTypeName;
		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public String getLookupTypeName() {
		return lookupTypeName;
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected CardReference convertNotNullValue(final Object value) {
		if (value instanceof CardReference) {
			return CardReference.class.cast(value);
		}
		if (value instanceof Number) {
			return new CardReference(Number.class.cast(value).longValue(), StringUtils.EMPTY);
		} else if (value instanceof String) {
			final String s = String.class.cast(value);
			return isNotBlank(s) ? new CardReference(Long.parseLong(s), StringUtils.EMPTY) : null;
		} else {
			throw illegalValue(value);
		}
	}

	@Override
	public String toString() {
		return toString;
	}

}

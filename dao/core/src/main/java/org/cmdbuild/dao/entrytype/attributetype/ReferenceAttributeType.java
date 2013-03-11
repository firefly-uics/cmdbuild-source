package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;

public class ReferenceAttributeType extends AbstractAttributeType<Long> {

	public final CMIdentifier domain;

	/**
	 * @deprecated use {@link #ReferenceAttributeType(CMIdentifier)} instead.
	 */
	@Deprecated
	public ReferenceAttributeType(final String domain) {
		this.domain = new CMIdentifier() {

			@Override
			public String getLocalName() {
				return domain;
			}

			@Override
			public String getNamespace() {
				return CMIdentifier.DEFAULT_NAMESPACE;
			}

		};
	}

	public ReferenceAttributeType(final CMDomain domain) {
		this(domain.getIdentifier());
	}

	public ReferenceAttributeType(final CMIdentifier identifier) {
		this.domain = identifier;
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

}

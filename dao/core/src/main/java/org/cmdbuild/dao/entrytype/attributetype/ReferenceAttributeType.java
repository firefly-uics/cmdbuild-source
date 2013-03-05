package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.reference.CardReference;

public class ReferenceAttributeType extends AbstractAttributeType<CardReference> {

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
	protected CardReference convertNotNullValue(final Object value) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}

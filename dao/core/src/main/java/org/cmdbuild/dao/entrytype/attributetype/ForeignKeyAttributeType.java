package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.reference.CardReference;

public class ForeignKeyAttributeType extends AbstractAttributeType<CardReference> {

	private final String destinationClassName;

	/**
	 * FIXME: it would be better to have a CMClass instead of className only
	 */

	public ForeignKeyAttributeType(final String destinationClassName) {
		this.destinationClassName = destinationClassName;
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	public String getForeignKeyDestinationClassName() {
		return destinationClassName;
	}

	@Override
	public CardReference convertNotNullValue(final Object value) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public String toString() {
		return "FOREIGNKEY";
	}
}

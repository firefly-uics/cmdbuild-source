package org.cmdbuild.dao.reference;

public class EntryTypeReference extends AbstractReference {

	private EntryTypeReference(final Long id) {
		super(id);
	}

	@Override
	public void accept(final CMReferenceVisitor visitor) {
		visitor.visit(this);
	}

	public static EntryTypeReference newInstance(final Long id) {
		return new EntryTypeReference(id);
	}

}

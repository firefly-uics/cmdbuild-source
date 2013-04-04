package org.cmdbuild.dao.reference;


public class EntryTypeReference extends AbstractReference {

	public static EntryTypeReference newInstance(final Long id) {
		return new EntryTypeReference(id);
	}

	private EntryTypeReference(final Long id) {
		super(id);
	}

	@Override
	public void accept(CMReferenceVisitor visitor) {
		visitor.visit(this);
	}
}

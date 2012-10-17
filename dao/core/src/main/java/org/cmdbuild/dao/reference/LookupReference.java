package org.cmdbuild.dao.reference;

public class LookupReference extends AbstractReference {

	private final String lookupTypeName;

	private LookupReference(final String typeName, final Long id) {
		super(id);
		this.lookupTypeName = typeName;
	}

	public String getLookupTypeName() {
		return lookupTypeName;
	}

	@Override
	public void accept(CMReferenceVisitor visitor) {
		visitor.visit(this);
	}

	public static LookupReference newInstance(final String lookupTypeName, final Long lookupId) {
		return new LookupReference(lookupTypeName, lookupId);
	}

}

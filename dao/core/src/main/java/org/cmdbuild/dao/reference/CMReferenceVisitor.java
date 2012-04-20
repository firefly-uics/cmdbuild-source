package org.cmdbuild.dao.reference;

public interface CMReferenceVisitor {

	void visit(CardReference cardReference);
	void visit(EntryTypeReference entryTypeReference);
	void visit(LookupReference lookupReference);

}

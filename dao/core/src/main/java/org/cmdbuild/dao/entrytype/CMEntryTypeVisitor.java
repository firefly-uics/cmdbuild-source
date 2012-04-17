package org.cmdbuild.dao.entrytype;

public interface CMEntryTypeVisitor {

	void visit(CMDomain type);
	void visit(CMClass type);

}

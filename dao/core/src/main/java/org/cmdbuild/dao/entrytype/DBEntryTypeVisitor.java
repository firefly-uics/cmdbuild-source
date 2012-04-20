package org.cmdbuild.dao.entrytype;

/**
 * This visitor is used by the {@link UserDataView} that handles DB types
 * and not the CM interfaces.
 */
public interface DBEntryTypeVisitor {

	void visit(DBDomain type);
	void visit(DBClass type);

}

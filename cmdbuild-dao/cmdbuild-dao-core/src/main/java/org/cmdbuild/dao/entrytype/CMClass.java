package org.cmdbuild.dao.entrytype;

public interface CMClass extends CMEntryType {

	public CMClass getParent();
	public Iterable<? extends CMClass> getChildren();

	public interface CMClassDefinition {
		
	}
}

package org.cmdbuild.dao.entrytype;


public interface CMClass extends CMEntryType {

	interface CMClassDefinition {
		
	}

	CMClass getParent();
	Iterable<? extends CMClass> getChildren();
	Iterable<? extends CMClass> getLeaves();
	boolean isAncestorOf(CMClass cmClass);
	boolean isSuperclass();

}

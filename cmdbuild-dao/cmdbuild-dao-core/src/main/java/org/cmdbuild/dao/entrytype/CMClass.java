package org.cmdbuild.dao.entrytype;

import java.util.Set;

public interface CMClass extends CMEntryType {

	public CMClass getParent();
	public Iterable<? extends CMClass> getChildren();
	public Set<? extends CMClass> getLeaves();
	public boolean isAncestorOf(CMClass cmClass);
	public boolean isSuperclass();

	public interface CMClassDefinition {
		
	}
}

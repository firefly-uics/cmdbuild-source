package org.cmdbuild.dao.entrytype;

public interface CMEntryType {

	Object getId();
	String getName();
	String getDescription();
	boolean isActive();
	boolean isSystem();

	Iterable<? extends CMAttribute> getAttributes();
	DBAttribute getAttribute(String name);
}

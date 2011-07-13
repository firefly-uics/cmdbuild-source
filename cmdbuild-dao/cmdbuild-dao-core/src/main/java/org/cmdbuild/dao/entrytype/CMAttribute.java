package org.cmdbuild.dao.entrytype;


public interface CMAttribute {

	public CMEntryType getOwner();
	public String getName();

	boolean isSystem(); // TODO Remove it! We should list only user-defined attributes!
}

package org.cmdbuild.dao.entrytype;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;


public interface CMAttribute {

	CMEntryType getOwner();
	CMAttributeType<?> getType();

	String getName();
	String getDescription();

	boolean isSystem(); // TODO Remove it! We should list only user-defined attributes!
}

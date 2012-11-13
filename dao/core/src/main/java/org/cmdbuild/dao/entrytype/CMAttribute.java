package org.cmdbuild.dao.entrytype;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public interface CMAttribute extends Deactivable {

	CMEntryType getOwner();

	CMAttributeType<?> getType();

	String getName();

	String getDescription();
}

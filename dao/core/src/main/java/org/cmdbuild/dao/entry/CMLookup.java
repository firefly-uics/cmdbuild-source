package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMLookupType;

public interface CMLookup {

	CMLookupType getType();

	Object getId();
}

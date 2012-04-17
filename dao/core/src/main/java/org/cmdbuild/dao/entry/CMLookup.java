package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMLookupType;

public interface CMLookup {

	public CMLookupType getType();

	public Object getId();
}

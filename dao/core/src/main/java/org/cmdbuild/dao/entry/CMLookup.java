package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMLookupType;

public interface CMLookup {

	public CMLookupType getType();

	public Object getId();

	/**
	 * Returns a hopefully unique string identifier.
	 * 
	 * @return string identifier
	 */
	public String getCode();

	/**
	 * Returns a human-readable string representation.
	 * 
	 * @return description
	 */
	public String getDescription();
}

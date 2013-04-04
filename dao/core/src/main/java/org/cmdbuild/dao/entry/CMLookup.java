package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMLookupType;

public interface CMLookup {

	CMLookupType getType();

	Long getId();

	CMLookup getParent();

	/**
	 * Returns a hopefully unique string identifier.
	 * 
	 * @return string identifier
	 */
	String getCode();

	/**
	 * Returns a human-readable string representation.
	 * 
	 * @return description
	 */
	String getDescription();
}

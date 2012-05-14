package org.cmdbuild.dao.entrytype;

import org.cmdbuild.auth.CMPrivilegedObject;
import org.cmdbuild.dao.CMTypeObject;

public interface CMEntryType extends Deactivable, CMTypeObject, CMPrivilegedObject {

	void accept(CMEntryTypeVisitor visitor);

	String getDescription();

	boolean isSystem();

	/**
	 * Indicates if it holds historic data
	 * 
	 * @return if it holds historic data
	 */
	boolean holdsHistory();

	/**
	 * Returns a sorted list of active attributes for this entry type.
	 * 
	 * @return attributes in the correct display order
	 */
	Iterable<? extends CMAttribute> getAttributes();

	/**
	 * Returns a sorted list of all (active and inactive) attributes for this
	 * entry type.
	 * 
	 * @return attributes in the correct display order
	 */
	Iterable<? extends CMAttribute> getAllAttributes();

	CMAttribute getAttribute(String name);
}

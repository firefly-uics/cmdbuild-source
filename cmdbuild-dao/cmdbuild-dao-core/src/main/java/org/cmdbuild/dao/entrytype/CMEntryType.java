package org.cmdbuild.dao.entrytype;

import org.cmdbuild.auth.acl.CMPrivilegedObject;

public interface CMEntryType extends Deactivable, CMPrivilegedObject {

	void accept(CMEntryTypeVisitor visitor);

	Object getId();

	String getName();

	String getDescription();

	boolean isSystem();

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

	String getKeyAttributeName();
}

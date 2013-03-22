package org.cmdbuild.dao.entrytype;

import org.cmdbuild.auth.acl.CMPrivilegedObject;

public interface CMEntryType extends Deactivable, CMPrivilegedObject {

	interface CMEntryTypeDefinition {

		/**
		 * Returns the entry type identifier.
		 * 
		 * @return the entry type identifier.
		 */
		CMIdentifier getIdentifier();

		/**
		 * Returns the entry type id.
		 * 
		 * @return the entry type id, {@code null} if missing.
		 */
		Long getId();

	}

	void accept(CMEntryTypeVisitor visitor);

	Long getId();

	/**
	 * @deprecated use {@link #getIdentifier()} instead.
	 */
	@Deprecated
	String getName();

	CMIdentifier getIdentifier();

	String getDescription();

	boolean isSystem();

	boolean isBaseClass();

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

	String getKeyAttributeName();

}

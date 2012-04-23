package org.cmdbuild.auth.acl;

import java.util.Set;

public interface CMGroup extends PrivilegeSet {

	/**
	 * This identifier will be useful in the refactoring of the old messy
	 * code. It should be removed when it is not needed, and the name should
	 * always be used instead.
	 * 
	 * @return unique identifier
	 */
	Long getId();

	/**
	 * 
	 * @return unique human-readable identifier
	 */
	String getName();

	String getDescription();

	/**
	 * Returns a set of disabled modules for this group.
	 * 
	 * FIXME: It should be handled with privileges, but it would require too
	 * much effort right now.
	 * 
	 * @return the disabled modules
	 */
	Set<String> getDisabledModules();

	/**
	 * Returns the class name that should be selected on the UI load 
	 * 
	 * @return class id or null
	 */
	Long getStartingClassId();

	/**
	 * Two CMGroups are equal if their name is equal
	 *
	 * @param obj
	 * @return if the two groups are equal
	 */
	@Override
	boolean equals(final Object obj);
}

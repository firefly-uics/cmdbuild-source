package org.cmdbuild.auth.acl;

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
	 * Two CMGroups are equal if their name is equal
	 *
	 * @param obj
	 * @return if the two groups are equal
	 */
	@Override
	boolean equals(final Object obj);
}

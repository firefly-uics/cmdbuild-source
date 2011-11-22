package org.cmdbuild.auth.acl;

public interface CMGroup extends PrivilegeSet {

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

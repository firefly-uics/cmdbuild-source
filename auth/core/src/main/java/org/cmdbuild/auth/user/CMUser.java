package org.cmdbuild.auth.user;

import java.util.Set;
import org.cmdbuild.auth.acl.CMGroup;

public interface CMUser {

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
	Set<CMGroup> getGroups();

	/**
	 * Returns the name of the default group for this user, used to try and
	 * select the preferred group in the
	 * {@link org.cmdbuild.auth.AuthenticatedUser}
	 * 
	 * @return default group name or null if not set
	 */
	String getDefaultGroupName();

	/**
	 * Two CMUsers are equal if their name is equal
	 * 
	 * @param obj
	 * @return if the two users are equal
	 */
	@Override
	boolean equals(final Object obj);
}

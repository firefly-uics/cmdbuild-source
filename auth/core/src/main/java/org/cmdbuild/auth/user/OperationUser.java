package org.cmdbuild.auth.user;

import org.cmdbuild.auth.acl.CMSecurityManager;

/**
 * Defines informations about the user that is executing operations
 * and its current privileges.
 */
public interface OperationUser extends CMSecurityManager {

	/**
	 * Returns an identifier for the user that is executing the current
	 * operation.
	 * 
	 * @return user identifier
	 */
	String getOperationUsername();

	/**
	 * Returns a single group name to be used for operations that need
	 * a single group among those to which the user belongs.
	 * 
	 * @return the name of the preferred group
	 */
	String getPreferredGroupName();
}

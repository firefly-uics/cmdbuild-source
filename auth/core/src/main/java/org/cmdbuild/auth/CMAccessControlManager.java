package org.cmdbuild.auth;


public interface CMAccessControlManager {

	/*
	 * Access privileges
	 */

	boolean hasReadAccess(CMPrivilegedObject privilegedObject);

	/*
	 * Other privileges
	 */

	boolean hasDatabaseDesignerPrivileges();
	boolean hasWorkflowDesignerPrivileges();

	/**
	 * Returns if the user has administrator privileges.
	 * 
	 * Administrators are those users that can change the system
	 * configuration, manage users, groups, their menus and ACLs.
	 * 
	 * @return
	 */
	boolean hasAdministratorPrivileges();

	/**
	 * Reports currently use SQL for queries, so there is no way to
	 * give safe access to user data only. It has to fall back to
	 * {@link hasAdministratorPrivileges()}.
	 * 
	 * @return {@link hasAdministratorPrivileges()}
	 */
	boolean hasReportDesignerPrivileges();
}

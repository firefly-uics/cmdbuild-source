package org.cmdbuild.auth.user;

import org.cmdbuild.auth.acl.CMSecurityManager;

/**
 * Defines useful informations about the user and current privileges
 */
public interface OperationUser extends CMSecurityManager {

	String getUsername();
}

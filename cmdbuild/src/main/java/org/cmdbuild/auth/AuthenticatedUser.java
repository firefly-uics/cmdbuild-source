package org.cmdbuild.auth;

import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;

public interface AuthenticatedUser extends CMUser, OperationUser {

	boolean isValid();
	boolean isAnonymous();

	void filterPrivileges(final String groupName);
	void selectGroup(final String groupName);

	boolean canChangePassword();
	boolean changePassword(final String oldPassword, final String newPassword);

	/**
	 * Impersonates another user, if possible. This method should be called
	 * by the AuthenticationService.
	 *
	 * @param user user to impersonate
	 */
	void impersonate(CMUser user);

}

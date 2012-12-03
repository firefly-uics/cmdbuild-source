package org.cmdbuild.auth;

import java.util.List;

import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.DefaultAuthenticationService.ClientAuthenticatorResponse;
import org.cmdbuild.auth.DefaultAuthenticationService.PasswordCallback;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;

public interface AuthenticationService {

	public void setPasswordAuthenticators(final PasswordAuthenticator... passwordAuthenticators);

	public void setClientRequestAuthenticators(final ClientRequestAuthenticator... clientRequestAuthenticators);

	public void setUserFetchers(final UserFetcher... userFetchers);
	
	public void setGroupFetcher(final GroupFetcher groupFetcher);

	public void setUserStore(final UserStore userStore);

	/**
	 * Actively checks the user credentials and returns the authenticated user
	 * on success.
	 * 
	 * @param login
	 * @param password
	 *            unencrypted password
	 * @return the user that was authenticated
	 */
	public AuthenticatedUser authenticate(final Login login, final String password);

	/**
	 * Extracts the unencrypted password for the user and sets it in the
	 * 
	 * @param passwordCallback
	 *            for further processing.
	 * 
	 * @param login
	 * @param passwordCallback
	 *            object where to set the unencrypted password
	 * @return the user to be authenticated as if the authentication succeeded
	 */
	public AuthenticatedUser authenticate(final Login login, final PasswordCallback passwordCallback);

	/**
	 * Tries to authenticate the user with a ClientRequestAuthenticator
	 * 
	 * @param request
	 *            object representing a client request
	 * @return response object with the authenticated user or a redirect URL
	 */
	public ClientAuthenticatorResponse authenticate(final ClientRequest request);

	/**
	 * Impersonate another user if the currently authenticated user has the
	 * right privileges.
	 * 
	 * @param login
	 * @return the authenticated user
	 */
	public OperationUser impersonate(final Login login);

	/**
	 * Get the currently authenticated user. It can be anonymous but it will
	 * never be null.
	 * 
	 * @return the authenticated user
	 */
	public OperationUser getOperationUser();

	public List<CMUser> fetchUsersByGroupId(Long groupId);

	/**
	 * Given a user identifier, it returns the user with that id
	 * 
	 * @param userId
	 * @return the user with id = userId, null if there is no user with that id
	 */
	public CMUser fetchUserById(Long userId);

	/**
	 * Given a username, it returns the user with that username
	 * 
	 * @param username
	 * @return the user with the provided username, null if there is no user
	 *         with that username
	 */
	public CMUser fetchUserByUsername(String username);
	
	/**
	 * 
	 * @return a collection of all groups stored in the database
	 */
	public Iterable<CMGroup> fetchAllGroups();

}

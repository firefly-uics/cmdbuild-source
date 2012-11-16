package org.cmdbuild.auth;

import java.util.List;

import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.DefaultAuthenticationService.ClientAuthenticatorResponse;
import org.cmdbuild.auth.DefaultAuthenticationService.PasswordCallback;
import org.cmdbuild.auth.user.CMUser;

public interface AuthenticationService {

	public void setPasswordAuthenticators(final PasswordAuthenticator... passwordAuthenticators);

	public void setClientRequestAuthenticators(final ClientRequestAuthenticator... clientRequestAuthenticators);

	public void setUserFetchers(final UserFetcher... userFetchers);

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
	public AuthenticatedUser impersonate(final Login login);

	/**
	 * Get the currently authenticated user. It can be anonymous but it will
	 * never be null.
	 * 
	 * @return the authenticated user
	 */
	public AuthenticatedUser getAuthenticatedUser();

	public List<CMUser> fetchUsersByGroupId(Long groupId);

	public CMUser fetchUserById(Long userId);

}

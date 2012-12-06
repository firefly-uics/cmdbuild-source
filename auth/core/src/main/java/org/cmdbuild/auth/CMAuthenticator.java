package org.cmdbuild.auth;

public interface CMAuthenticator {

	/**
	 * Returns the name of the authenticator, to be referenced by name.
	 *
	 * @return the name of the authenticator
	 */
	String getName();
}

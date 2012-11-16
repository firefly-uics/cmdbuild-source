package org.cmdbuild.auth;

public interface UserStore {

	/**
	 * Returns the authenticated user from the session. It must always be a
	 * valid (not null) user: "Use Null Objects, Luke!"
	 * 
	 * @return the authenticated user for this request
	 */
	AuthenticatedUser getUser();

	/**
	 * Sets the authenticated user in this session.
	 * 
	 * @param user
	 */
	void setUser(AuthenticatedUser user);
}

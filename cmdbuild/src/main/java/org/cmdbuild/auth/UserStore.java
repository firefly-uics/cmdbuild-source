package org.cmdbuild.auth;

public interface UserStore {

	AuthenticatedUser getUser();
	void setUser(AuthenticatedUser user);
}

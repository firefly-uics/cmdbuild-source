package org.cmdbuild.auth;

import org.cmdbuild.auth.user.AuthenticatedUser;

public interface UserStore {

	AuthenticatedUser getUser();
	void setUser(AuthenticatedUser user);
}

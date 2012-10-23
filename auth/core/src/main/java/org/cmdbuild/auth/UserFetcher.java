package org.cmdbuild.auth;

import org.cmdbuild.auth.user.CMUser;

public interface UserFetcher {

	CMUser fetchUser(Login login);
}

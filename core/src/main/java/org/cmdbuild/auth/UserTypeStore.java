package org.cmdbuild.auth;

import org.cmdbuild.services.auth.UserType;

public interface UserTypeStore {

	UserType getType();

	void setType(UserType type);

}

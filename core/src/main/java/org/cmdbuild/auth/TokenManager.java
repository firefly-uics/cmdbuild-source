package org.cmdbuild.auth;

import org.cmdbuild.auth.user.OperationUser;

public interface TokenManager extends ObservableUserStore.Observer {

	String getToken(OperationUser value);

	OperationUser getUser(String value);

}
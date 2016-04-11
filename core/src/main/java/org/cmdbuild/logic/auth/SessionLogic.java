package org.cmdbuild.logic.auth;

import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;

public interface SessionLogic extends AuthenticationLogic {

	boolean isValidUser();

	ClientAuthenticationResponse login(ClientRequest request);

}

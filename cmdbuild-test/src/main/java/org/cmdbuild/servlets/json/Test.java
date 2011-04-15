package org.cmdbuild.servlets.json;

import org.cmdbuild.services.auth.AuthenticationFacade;
import org.cmdbuild.servlets.utils.Parameter;


public class Test extends JSONBase {

	@JSONExported
	@Unauthorized
	public String login(
			@Parameter("username") String username) {
		AuthenticationFacade.loginAs(username);
		return "Logged in as " + username;
	}
}

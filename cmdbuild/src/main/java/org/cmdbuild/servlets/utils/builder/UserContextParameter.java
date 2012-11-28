package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;

public class UserContextParameter extends AbstractParameterBuilder<UserContext> {

	public UserContext build(HttpServletRequest r) throws AuthException, ORMException, NotFoundException {
		return new SessionVars().getCurrentUserContext();
	}
}

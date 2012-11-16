package org.cmdbuild.servlets.utils.builder.factory;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.utils.builder.AbstractParameterBuilder;

public class DomainFactoryParameter extends AbstractParameterBuilder<DomainFactory> {

	@Override
	public DomainFactory build(final HttpServletRequest r) throws AuthException, ORMException, NotFoundException {
		final UserContext userCtx = new SessionVars().getCurrentUserContext();
		return userCtx.domains();
	}
}

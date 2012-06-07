package org.cmdbuild.servlets.utils.builder.factory;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.utils.builder.AbstractParameterBuilder;

public class TableFactoryParameter extends AbstractParameterBuilder<ITableFactory> {

	public ITableFactory build(HttpServletRequest r) throws AuthException, ORMException, NotFoundException {
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		return userCtx.tables();
	}
}

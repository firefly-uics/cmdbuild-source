package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;

public class DomainParameter extends AbstractParameterBuilder<IDomain> {

	public IDomain build(HttpServletRequest r) throws AuthException, ORMException,
			NotFoundException {
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		DomainFactory df = userCtx.domains();
		int domainId = parameter(Integer.TYPE, "idDomain", r);

		if (domainId > 0) {
			return df.get(domainId);
		} else {
			//TODO remove the double check. Find where is used idDomain and remove it
			domainId = parameter(Integer.TYPE, "id", r);
			if (domainId > 0) {
				return df.get(domainId);
			} else {
				return df.create();
			}
		}
	}
}

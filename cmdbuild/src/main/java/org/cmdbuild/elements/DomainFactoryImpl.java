package org.cmdbuild.elements;

import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.DomainQuery;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.DomainQueryProxy;
import org.cmdbuild.elements.proxy.DomainProxy;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.services.SchemaCache;
import org.cmdbuild.services.auth.UserContext;

public class DomainFactoryImpl implements DomainFactory {
	UserContext userCtx;

	public DomainFactoryImpl(UserContext userCtx) {
		this.userCtx = userCtx;
	}

	/**
	 * Creates a proxed domain, with auth checks
	 */
	public IDomain create() {
		userCtx.privileges().assureAdminPrivilege();
		IDomain realDomain = new DomainImpl(); 
		return new DomainProxy(realDomain, userCtx);
	}

	/**
	 * Returns an existing proxed domain, with auth checks
	 */
	public IDomain get(int domainId) throws NotFoundException {
		IDomain realDomain = SchemaCache.getInstance().getDomain(domainId);
		return new DomainProxy(realDomain, userCtx);
	}

	/**
	 * Returns an existing proxed domain, with auth checks
	 */
	public IDomain get(String domainName) throws NotFoundException {
		IDomain realDomain = SchemaCache.getInstance().getDomain(domainName);
		userCtx.privileges().assureReadPrivilege(realDomain);
		return new DomainProxy(realDomain, userCtx);
	}

	/**
	 * Returns a proxed domain query, that does auth checks
	 */
	public DomainQuery list(ITable table) {
		DomainQuery domainQuery = new DomainQueryImpl(table);
		return new DomainQueryProxy(domainQuery, userCtx);
	}
}

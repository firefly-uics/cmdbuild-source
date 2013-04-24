package org.cmdbuild.elements.proxy;

import java.util.Iterator;

import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.DomainQuery;
import org.cmdbuild.elements.proxy.iterator.ProxyIterator;
import org.cmdbuild.services.auth.UserContext;

public class DomainQueryProxy extends DomainQueryForwarder {
	private UserContext userCtx;

	public DomainQueryProxy(DomainQuery domainQuery, UserContext userCtx) {
		super(domainQuery);
		this.userCtx = userCtx;
	}

	@Override
	public Iterator<IDomain> iterator() {
		return new ProxyIterator<IDomain>(super.iterator()) {
			protected boolean isValid(IDomain d) {
				return userCtx.privileges().hasReadPrivilege(d);
			}
			protected IDomain createProxy(IDomain d) {
				return new DomainProxy(d, userCtx);
			}
		};
	}
}

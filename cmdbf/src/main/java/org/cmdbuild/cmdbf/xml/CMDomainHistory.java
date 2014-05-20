package org.cmdbuild.cmdbf.xml;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.ForwardingDomain;

public class CMDomainHistory extends ForwardingDomain {

	private CMDomain delegate;

	public CMDomainHistory(final CMDomain delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	public CMDomain getBaseType() {
		return delegate;
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

}

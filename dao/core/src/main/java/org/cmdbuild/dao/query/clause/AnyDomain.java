package org.cmdbuild.dao.query.clause;

import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBIdentifier;
import org.cmdbuild.dao.entrytype.ForwardingDomain;

public class AnyDomain extends ForwardingDomain {

	public static CMDomain anyDomain() {
		return new AnyDomain();
	}

	private AnyDomain() {
		super(UnsupportedProxyFactory.of(CMDomain.class).create());
	}

	@Override
	public CMIdentifier getIdentifier() {
		return new DBIdentifier("*");
	}

	@Override
	public String getName() {
		return getIdentifier().getLocalName();
	}

}

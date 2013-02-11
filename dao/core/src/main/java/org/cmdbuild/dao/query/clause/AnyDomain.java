package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBIdentifier;
import org.cmdbuild.dao.entrytype.PlaceholderDomain;

public class AnyDomain extends PlaceholderDomain {

	private AnyDomain() {
	}

	public static CMDomain anyDomain() {
		return new AnyDomain();
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

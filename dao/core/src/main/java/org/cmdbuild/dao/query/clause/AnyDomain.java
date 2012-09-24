package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.PlaceholderDomain;

public class AnyDomain extends PlaceholderDomain {

	private AnyDomain() {
	}

	public static CMDomain anyDomain() {
		return new AnyDomain();
	}

	@Override
	public String getName() {
		return "*";
	}
}

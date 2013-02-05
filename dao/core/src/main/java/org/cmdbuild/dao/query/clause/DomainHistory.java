package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.PlaceholderDomain;

public class DomainHistory extends PlaceholderDomain {

	private final CMDomain domain;

	private DomainHistory(final CMDomain domain) {
		this.domain = domain;
	}

	public static CMDomain history(final CMDomain domain) {
		return new DomainHistory(domain);
	}

	@Override
	public CMIdentifier getIdentifier() {
		return domain.getIdentifier();
	}

	@Override
	public String getName() {
		return domain.getName() + " HISTORY";
	}

	public CMDomain getDomain() {
		return domain;
	}
}

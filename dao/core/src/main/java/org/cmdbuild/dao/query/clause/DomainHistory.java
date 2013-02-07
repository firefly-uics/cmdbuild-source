package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.PlaceholderDomain;

public class DomainHistory extends PlaceholderDomain {

	private final CMDomain current;

	private DomainHistory(final CMDomain current) {
		this.current = current;
	}

	public static CMDomain history(final CMDomain current) {
		return new DomainHistory(current);
	}

	@Override
	public CMIdentifier getIdentifier() {
		return current.getIdentifier();
	}

	@Override
	public String getName() {
		return current.getName() + " HISTORY";
	}

	public CMDomain getCurrent() {
		return current;
	}
}

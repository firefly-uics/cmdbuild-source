package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.CMDomain;

/**
 * Adds to the CMDomain the information about the attribute used as a source in the query.
 */
public class QueryDomain {

	final CMDomain domain;
	final boolean direction;

	public QueryDomain(final CMDomain domain, final boolean direction) {
		this.domain = domain;
		this.direction = direction;
	}

	public CMDomain getDomain() {
		return domain;
	}

	public boolean getDirection() {
		return direction;
	}

	public String getDescription() {
		throw new UnsupportedOperationException("Not implemented yet: should discriminate by the direction");
	}
}

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
		if (direction) {
			return domain.getDescription1();
		} else {
			return domain.getDescription2();
		}
	}

	/*
	 * Object overrides
	 */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (direction ? 1231 : 1237);
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryDomain other = (QueryDomain) obj;
		if (direction != other.direction)
			return false;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		return true;
	}	
}

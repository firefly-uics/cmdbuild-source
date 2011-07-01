package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;

/**
 * Adds to the CMDomain the information about the attribute used as a source in the query.
 */
public class QueryDomain {

	/**
	 * Domains are between two classes only, but we want to design it for domains between more than two classes
	 */
	public enum Source {
		_1 {
			@Override
			public boolean getDirection() {
				return true;
			}

			@Override
			public String getDomainDescription(final CMDomain domain) {
				return domain.getDescription1();
			}

			@Override
			public CMClass getSourceClass(final CMDomain domain) {
				return domain.getClass1();
			}

			@Override
			public CMClass getTargetClass(final CMDomain domain) {
				return domain.getClass2();
			}
		},
		_2 {
			@Override
			public boolean getDirection() {
				return false;
			}

			@Override
			public String getDomainDescription(final CMDomain domain) {
				return domain.getDescription2();
			}

			@Override
			public CMClass getSourceClass(final CMDomain domain) {
				return domain.getClass2();
			}

			@Override
			public CMClass getTargetClass(final CMDomain domain) {
				return domain.getClass1();
			}
		};

		public abstract boolean getDirection();
		public abstract String getDomainDescription(CMDomain domain);
		public abstract CMClass getSourceClass(final CMDomain domain);
		public abstract CMClass getTargetClass(final CMDomain domain);
	}

	final CMDomain domain;
	final Source querySource;

	public QueryDomain(final CMDomain domain, final String querySource) {
		this(domain, Source.valueOf(querySource));
	}

	public QueryDomain(final CMDomain domain, final Source querySource) {
		this.domain = domain;
		this.querySource = querySource;
	}

	public CMDomain getDomain() {
		return domain;
	}

	public String getQuerySource() {
		return querySource.name();
	}

	public CMClass getSourceClass() {
		return querySource.getSourceClass(domain);
	}

	public CMClass getTargetClass() {
		return querySource.getTargetClass(domain);
	}

	/**
	 * @deprecated Use {@link getQuerySource()} instead
	 */
	public boolean getDirection() {
		return querySource.getDirection();
	}

	public String getDescription() {
		return querySource.getDomainDescription(domain);
	}

	/*
	 * Object overrides
	 */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((querySource == null) ? 0 : querySource.hashCode());
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
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (querySource != other.querySource)
			return false;
		return true;
	}
}

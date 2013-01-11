package org.cmdbuild.logic.mapping;

import org.cmdbuild.dao.query.clause.where.WhereClause;

public interface FilterMapper {

	final class JoinElement {

		public final String domain;
		public final String source;

		private JoinElement(final String domain, final String source) {
			this.domain = domain;
			this.source = source;
		}

		public static JoinElement newInstance(final String domain, final String source) {
			return new JoinElement(domain, source);
		}

	}

	WhereClause whereClauses();

	Iterable<JoinElement> joinElements();

}

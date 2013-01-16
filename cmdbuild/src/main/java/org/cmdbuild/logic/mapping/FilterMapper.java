package org.cmdbuild.logic.mapping;

import org.cmdbuild.dao.query.clause.where.WhereClause;

public interface FilterMapper {

	final class JoinElement {

		public final String domain;
		public final String source;
		public final String destination;

		private JoinElement(final String domain, final String source, final String destination) {
			this.domain = domain;
			this.source = source;
			this.destination = destination;
		}

		public static JoinElement newInstance(final String domain, final String source, final String destination) {
			return new JoinElement(domain, source, destination);
		}

	}

	WhereClause whereClauses();

	Iterable<JoinElement> joinElements();

}

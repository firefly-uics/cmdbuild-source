package org.cmdbuild.dao.query.clause.where;

import java.util.List;

public abstract class CompositeWhereClause implements WhereClause {

	private final List<WhereClause> clauses;

	public CompositeWhereClause(final List<WhereClause> clauses) {
		this.clauses = clauses;
	}

	public List<WhereClause> getClauses() {
		return clauses;
	}

}

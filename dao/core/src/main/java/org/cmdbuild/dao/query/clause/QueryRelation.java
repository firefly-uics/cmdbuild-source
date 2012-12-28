package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entry.DBRelation;

public class QueryRelation {

	final DBRelation relation;
	final String querySource;

	private QueryRelation(final DBRelation relation, final String querySource) {
		this.relation = relation;
		this.querySource = querySource;
	}

	public DBRelation getRelation() {
		return relation;
	}

	public QueryDomain getQueryDomain() {
		return new QueryDomain(relation.getType(), querySource);
	}

	public static QueryRelation newInstance(final DBRelation relation, final String querySource) {
		return new QueryRelation(relation, querySource);
	}
}

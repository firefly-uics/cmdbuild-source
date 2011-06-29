package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entry.DBRelation;

public class QueryRelation {

	final DBRelation relation;
	final boolean direction;

	private QueryRelation(final DBRelation relation, final boolean direction) {
		this.relation = relation;
		this.direction = direction;
	}

	public DBRelation getRelation() {
		return relation;
	}

	public QueryDomain getQueryDomain() {
		return new QueryDomain(relation.getType(), direction);
	}

	public static QueryRelation create(DBRelation relation, boolean direction) {
		return new QueryRelation(relation, direction);
	}

	// TODO
}

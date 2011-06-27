package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.DBDomain;

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

	public static QueryRelation create(DBDriver driver, DBDomain domain, boolean direction) {
		final DBRelation relation = DBRelation.create(driver, domain);
		return new QueryRelation(relation, direction);
	}

	// TODO
}

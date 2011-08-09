package org.cmdbuild.dao.query.clause.where;

public interface WhereClause {

	public void accept(final WhereClauseVisitor visitor);
}

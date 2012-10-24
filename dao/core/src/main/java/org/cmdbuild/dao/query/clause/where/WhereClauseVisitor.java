package org.cmdbuild.dao.query.clause.where;

public interface WhereClauseVisitor {

	public void visit(final SimpleWhereClause whereClause);

	public void visit(final EmptyWhereClause whereClause);

}

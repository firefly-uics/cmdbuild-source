package org.cmdbuild.dao.query.clause.where;

public interface WhereClauseVisitor {

	void visit(NotWhereClause whereClause);

	void visit(SimpleWhereClause whereClause);

	void visit(AndWhereClause whereClause);

	void visit(OrWhereClause whereClause);

	void visit(EmptyWhereClause whereClause);

	void visit(TrueWhereClause whereClause);

	void visit(FalseWhereClause whereClause);

}

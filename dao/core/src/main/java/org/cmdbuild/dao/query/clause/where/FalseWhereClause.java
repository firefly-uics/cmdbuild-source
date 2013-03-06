package org.cmdbuild.dao.query.clause.where;

public class FalseWhereClause implements WhereClause {

	@Override
	public void accept(WhereClauseVisitor visitor) {
		visitor.visit(this);
	}

}

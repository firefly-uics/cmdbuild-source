package org.cmdbuild.dao.query.clause.where;

public class FalseWhereClause implements WhereClause {

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);
	}

}

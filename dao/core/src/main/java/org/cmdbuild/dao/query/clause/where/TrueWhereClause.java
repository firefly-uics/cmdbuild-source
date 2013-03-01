package org.cmdbuild.dao.query.clause.where;

public class TrueWhereClause implements WhereClause {

	@Override
	public void accept(WhereClauseVisitor visitor) {
		visitor.visit(this);
	}

}

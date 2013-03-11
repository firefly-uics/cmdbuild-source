package org.cmdbuild.dao.query.clause.where;

public class TrueWhereClause implements WhereClause {

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);
	}

}

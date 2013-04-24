package org.cmdbuild.dao.query.clause.where;

public class TrueWhereClause implements WhereClause {

	private static final TrueWhereClause INSTANCE = new TrueWhereClause();

	public static TrueWhereClause getInstance() {
		return INSTANCE;
	}

	public static TrueWhereClause trueWhereClause() {
		return INSTANCE;
	}

	private TrueWhereClause() {
		// prevents instantiation
	}

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);
	}

}

package org.cmdbuild.dao.query.clause.where;

public class FalseWhereClause implements WhereClause {
	
	private static final FalseWhereClause INSTANCE = new FalseWhereClause();

	public static FalseWhereClause getInstance() {
		return INSTANCE;
	}

	public static FalseWhereClause falseWhereClause() {
		return INSTANCE;
	}

	private FalseWhereClause() {
		// prevents instantiation
	}

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);
	}

}

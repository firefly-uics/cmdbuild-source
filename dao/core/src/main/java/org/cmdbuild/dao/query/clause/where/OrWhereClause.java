package org.cmdbuild.dao.query.clause.where;

import static java.util.Arrays.asList;

import java.util.List;

import com.google.common.collect.Lists;

public class OrWhereClause extends CompositeWhereClause {

	private OrWhereClause(final List<WhereClause> clauses) {
		super(clauses);
	}

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);

	}

	public static WhereClause or(final WhereClause first, final WhereClause second, final WhereClause... others) {
		final List<WhereClause> clauses = Lists.newArrayList();
		clauses.add(first);
		clauses.add(second);
		clauses.addAll(asList(others));
		return new OrWhereClause(clauses);
	}

}

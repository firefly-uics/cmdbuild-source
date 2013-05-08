package org.cmdbuild.dao.query.clause.where;

import static java.util.Arrays.asList;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

public class AndWhereClause extends CompositeWhereClause {

	private AndWhereClause(final List<WhereClause> clauses) {
		super(clauses);
	}

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);

	}

	public static WhereClause and(final WhereClause first, final WhereClause second, final WhereClause... others) {
		final List<WhereClause> clauses = Lists.newArrayList();
		clauses.add(first);
		clauses.add(second);
		clauses.addAll(asList(others));
		return new AndWhereClause(clauses);
	}

	public static WhereClause and(List<WhereClause> conditions) {
		final List<WhereClause> clauses = new LinkedList<WhereClause>();
		clauses.addAll(conditions);

		// Force the list to contains at least
		// two where clauses
		final int numberOfConditions = clauses.size();
		if (numberOfConditions == 0) {
			clauses.add(TrueWhereClause.trueWhereClause());
			clauses.add(TrueWhereClause.trueWhereClause());
		} else if (numberOfConditions == 1) {
			clauses.add(TrueWhereClause.trueWhereClause());
		}

		return new AndWhereClause(conditions);
	}
}

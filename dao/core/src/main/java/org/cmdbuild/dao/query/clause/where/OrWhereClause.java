package org.cmdbuild.dao.query.clause.where;

import static java.util.Arrays.asList;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

public class OrWhereClause extends CompositeWhereClause {

	private OrWhereClause(final List<? extends WhereClause> clauses) {
		super(clauses);
	}

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);

	}

	public static WhereClause or(final WhereClause first, final WhereClause second, final WhereClause... others) {
		final List<WhereClause> clauses = Lists.newArrayList(first, second);
		clauses.addAll(asList(others));
		return or(clauses);
	}

	/**
	 * Creates a new {@link OrWhereClause} from the specified
	 * {@link WhereClause}s.<br>
	 * 
	 * The following considerations are performed:<br>
	 * <ul>
	 * <li>0 where clauses - throws exception</li>
	 * <li>1 where clause - clause</li>
	 * <li>2 or more where clauses - (clause1 OR clause2 OR ...)</li>
	 * </ul>
	 * 
	 * @param whereClauses
	 * 
	 * @return a newly created {@link OrWhereClause}.
	 * 
	 * @throws IllegalArgumentException
	 *             if there are no where clauses.
	 */
	public static WhereClause or(final Iterable<? extends WhereClause> whereClauses) {
		final WhereClause whereClause;
		final Iterator<? extends WhereClause> iterator = whereClauses.iterator();
		if (iterator.hasNext()) {
			final WhereClause firstWhereClause = iterator.next();
			if (iterator.hasNext()) {
				final WhereClause secondWhereClause = iterator.next();
				final List<WhereClause> clauses = Lists.newArrayList(firstWhereClause, secondWhereClause);
				while (iterator.hasNext()) {
					clauses.add(iterator.next());
				}
				whereClause = new OrWhereClause(clauses);
			} else {
				whereClause = firstWhereClause;
			}
		} else {
			throw new IllegalArgumentException("there must be at least one where clause");
		}
		return whereClause;
	}

}

package org.cmdbuild.dao.driver.postgres.query;

import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_EQ;
import static org.cmdbuild.dao.driver.postgres.Const.STATUS_ACTIVE_VALUE;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.Utils;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClauseVisitor;

public class WherePartCreator extends PartCreator implements WhereClauseVisitor {

	public WherePartCreator(final QuerySpecs query) {
		super();
		query.getWhereClause().accept(this);
		// FIXME: append the status IF NOT a history query
		if (query.getFromType().holdsHistory()) {
			and(attributeFilter(attribute(query.getFromAlias(), SystemAttributes.Status.getDBName()), OPERATOR_EQ,
					STATUS_ACTIVE_VALUE));
		}
	}

	private WherePartCreator append(final String string) {
		if (sb.length() == 0) {
			sb.append("WHERE");
		}
		sb.append(" ").append(string);
		return this;
	}

	private void and(final String string) {
		if (sb.length() > 0) {
			append("AND");
		}
		append(string);
	}

	@Override
	public void visit(final SimpleWhereClause whereClause) {
		append(attributeFilter(whereClause.getAttribute(), OPERATOR_EQ, whereClause.getValue())); // FIXME
																									// OPERATOR
	}

	private String attributeFilter(final QueryAliasAttribute attribute, final String operator, final Object value) {
		final String lhs = Utils.quoteAttribute(attribute.getEntryTypeAlias(), attribute.getName());
		return String.format("%s%s%s", lhs, operator, param(value));
	}

	@Override
	public void visit(final EmptyWhereClause whereClause) {
		if (sb.length() != 0) {
			throw new IllegalArgumentException("Cannot use an empty clause along with other where clauses");
		}
	}

}

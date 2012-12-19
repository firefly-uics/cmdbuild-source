package org.cmdbuild.dao.driver.postgres.query;

import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_EQ;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_GT;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_LT;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.util.List;

import org.cmdbuild.dao.CardStatus;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.Utils;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.AndWhereClause;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.GreatherThanOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.NotWhereClause;
import org.cmdbuild.dao.query.clause.where.OperatorAndValueVisitor;
import org.cmdbuild.dao.query.clause.where.OrWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClauseVisitor;

public class WherePartCreator extends PartCreator implements WhereClauseVisitor {

	public WherePartCreator(final QuerySpecs query) {
		super();
		query.getWhereClause().accept(this);
		// FIXME: append the status IF NOT a history query
		if (query.getFromType().holdsHistory()) {
			and(attributeFilter(attribute(query.getFromAlias(), SystemAttributes.Status.getDBName()), OPERATOR_EQ,
					CardStatus.ACTIVE.value()));
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

	private void or(final String string) {
		if (sb.length() > 0) {
			append("OR");
		}
		append(string);
	}

	@Override
	public void visit(final SimpleWhereClause whereClause) {
		whereClause.getOperator().accept(new OperatorAndValueVisitor() {

			@Override
			public void visit(final EqualsOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), OPERATOR_EQ, operatorAndValue.getValue()));
			}

			@Override
			public void visit(final GreatherThanOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), OPERATOR_GT, operatorAndValue.getValue()));
			}

			@Override
			public void visit(final LessThanOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), OPERATOR_LT, operatorAndValue.getValue()));
			}

		});
	}

	@Override
	public void visit(final AndWhereClause whereClause) {
		append("(");
		// TODO do it better
		final List<WhereClause> clauses = whereClause.getClauses();
		for (int i = 0; i < clauses.size(); i++) {
			if (i > 0) {
				and(" ");
			}
			clauses.get(i).accept(this);
		}
		append(")");
	}

	@Override
	public void visit(final OrWhereClause whereClause) {
		append("(");
		// TODO do it better
		final List<WhereClause> clauses = whereClause.getClauses();
		for (int i = 0; i < clauses.size(); i++) {
			if (i > 0) {
				or(" ");
			}
			clauses.get(i).accept(this);
		}
		append(")");
	}

	@Override
	public void visit(final NotWhereClause whereClause) {
		append("NOT (");
		whereClause.getClauses().get(0).accept(this);
		append(")");
	}

	@Override
	public void visit(final EmptyWhereClause whereClause) {
		if (sb.length() != 0) {
			throw new IllegalArgumentException("Cannot use an empty clause along with other where clauses");
		}
	}

	private String attributeFilter(final QueryAliasAttribute attribute, final String operator, final Object value) {
		final String lhs = Utils.quoteAttribute(attribute.getEntryTypeAlias(), attribute.getName());
		return String.format("%s%s%s", lhs, operator, param(value));
	}

}

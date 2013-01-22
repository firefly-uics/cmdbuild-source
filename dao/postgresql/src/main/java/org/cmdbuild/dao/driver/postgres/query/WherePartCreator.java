package org.cmdbuild.dao.driver.postgres.query;

import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_EQ;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_GT;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_IN;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_LIKE;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_LT;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_NULL;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.util.List;

import org.cmdbuild.dao.CardStatus;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.SqlType;
import org.cmdbuild.dao.driver.postgres.Utils;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.AndWhereClause;
import org.cmdbuild.dao.query.clause.where.BeginsWithOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.EndsWithOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.GreaterThanOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.InOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.NotWhereClause;
import org.cmdbuild.dao.query.clause.where.NullOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.OperatorAndValueVisitor;
import org.cmdbuild.dao.query.clause.where.OrWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClauseVisitor;

public class WherePartCreator extends PartCreator implements WhereClauseVisitor {

	private final QuerySpecs query;

	public WherePartCreator(final QuerySpecs query) {
		super();
		this.query = query;
		query.getWhereClause().accept(this);
		// FIXME: append the status IF NOT a history query
		if (query.getFromType().holdsHistory()) {
			and(attributeFilter(attribute(query.getFromAlias(), SystemAttributes.Status.getDBName()), null,
					OPERATOR_EQ, CardStatus.ACTIVE.value()));
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
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_EQ,
						operatorAndValue.getValue()));
			}

			@Override
			public void visit(final GreaterThanOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_GT,
						operatorAndValue.getValue()));
			}

			@Override
			public void visit(final LessThanOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_LT,
						operatorAndValue.getValue()));
			}

			@Override
			public void visit(final ContainsOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_LIKE,
						"%" + operatorAndValue.getValue() + "%"));
			}

			@Override
			public void visit(final BeginsWithOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_LIKE,
						operatorAndValue.getValue() + "%"));
			}

			@Override
			public void visit(final EndsWithOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_LIKE,
						"%" + operatorAndValue.getValue()));
			}

			@Override
			public void visit(final NullOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_NULL,
						operatorAndValue.getValue()));
			}

			@Override
			public void visit(final InOperatorAndValue operatorAndValue) {
				final List<Object> inValues = operatorAndValue.getValue();
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_IN,
						inValues));
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

	private String attributeFilter(final QueryAliasAttribute attribute, final String attributeNameCast,
			final String operator, final Object value) {
		final Object sqlValue = sqlValueOf(attribute, value);
		String attributeName = Utils.quoteAttribute(attribute.getEntryTypeAlias(), attribute.getName());
		final boolean isAttributeNameCastSpecified = attributeNameCast != null;
		if (isAttributeNameCastSpecified) {
			attributeName = attributeName + "::" + attributeNameCast;
			return String.format("%s %s %s", attributeName, operator, value != null ? param(sqlValue, null) : "");
		}
		return String.format("%s %s %s", attributeName, operator,
				value != null ? param(sqlValue, sqlTypeOf(attribute).sqlCast()) : "");
	}

	private Object sqlValueOf(final QueryAliasAttribute attribute, final Object value) {
		return sqlTypeOf(attribute).javaToSqlValue(value);
	}

	private SqlType sqlTypeOf(final QueryAliasAttribute attribute) {
		return SqlType.getSqlType(typeOf(attribute));
	}

	private CMAttributeType<?> typeOf(final QueryAliasAttribute attribute) {
		final String key = attribute.getName();
		final CMAttribute _attribute = query.getFromType().getAttribute(key);
		return (_attribute == null) ? new UndefinedAttributeType() : _attribute.getType();
	}

}

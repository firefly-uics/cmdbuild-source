package org.cmdbuild.dao.driver.postgres.query;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.join;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.RowNumber;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.RowsCount;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForSystemAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForUserAttribute;
import static org.cmdbuild.dao.query.clause.alias.NameAlias.as;
import static org.cmdbuild.dao.query.clause.where.EmptyWhereClause.emptyWhereClause;

import java.util.List;

import org.cmdbuild.dao.driver.postgres.quote.AliasQuoter;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.NullOperatorAndValueVisitor;
import org.cmdbuild.dao.query.clause.where.NullWhereClauseVisitor;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public class QueryCreator {

	private static final String LF = "\n";

	private static final String PARTS_SEPARATOR = " " + LF;
	private static final String ORDER_BY = "ORDER BY";
	private static final String ORDER_BY_CLAUSE = "%s %s";

	private final StringBuilder sb;
	private final QuerySpecs querySpecs;
	private final List<Object> params;

	private SelectAttributesExpressions selectAttributesExpressions;
	private ColumnMapper columnMapper;

	public QueryCreator(final QuerySpecs querySpecs) {
		this.sb = new StringBuilder();
		this.querySpecs = querySpecs;
		this.params = newArrayList();
		buildQuery();
	}

	private void buildQuery() {
		selectAttributesExpressions = new SelectAttributesExpressions();
		columnMapper = new ColumnMapper(querySpecs, selectAttributesExpressions);
		columnMapper.addAllAttributes(querySpecs.getAttributes());

		appendSelect();
		appendFrom();
		appendDirectJoin();
		appendJoin();
		appendWhere();
		appendNumberingAndOrder();
		appendLimitAndOffset();
		appendConditionOnNumberedQuery();
	}

	private void appendSelect() {
		appendPart(new SelectPartCreator(querySpecs, columnMapper, selectAttributesExpressions));
	}

	private void appendFrom() {
		appendPart(new FromPartCreator(querySpecs));
	}

	private void appendDirectJoin() {
		appendPart(new DirectJoinPartCreator(querySpecs));
	}

	private void appendJoin() {
		appendPart(new JoinCreator(querySpecs.getFromClause().getAlias(), querySpecs.getJoins(), columnMapper));
	}

	private void appendWhere() {
		appendPart(new WherePartCreator(querySpecs));
	}

	private void appendNumberingAndOrder() {
		final String actual = sb.toString();
		sb.setLength(0);

		final List<String> selectAttributes = newArrayList();

		// any attribute of default query
		selectAttributes.add("*");

		// count
		if (querySpecs.count()) {
			selectAttributes.add(format("(SELECT count(*) FROM (%s) AS main) AS %s", //
					actual, //
					nameForSystemAttribute(querySpecs.getFromClause().getAlias(), RowsCount)));

			// parameters must be doubled
			params.addAll(params);
		}

		// row number (if possible)
		final String orderByExpression = join(expressionsForOrdering(), SelectPartCreator.ATTRIBUTES_SEPARATOR);
		if (!orderByExpression.isEmpty()) {
			selectAttributes.add(format("row_number() OVER (%s %s) AS %s", //
					ORDER_BY, //
					orderByExpression, //
					nameForSystemAttribute(querySpecs.getFromClause().getAlias(), RowNumber)));
		}

		sb.append(format("SELECT %s FROM (%s) AS main", //
				join(selectAttributes, SelectPartCreator.ATTRIBUTES_SEPARATOR), //
				actual));

		if (querySpecs.numbered()) {
			appendConditionOnNumberedQuery();
		}
	}

	/**
	 * Returns a list of all ordering expression in the format where each
	 * expression has the format: <br>
	 * <br>
	 * {@code "attribute [ASC|DESC]}" <br>
	 * <br>
	 * If no attributes are specified then {@code Id} is added (if possible).
	 * 
	 * @return a list of ordering expressions (if any).
	 */
	private List<String> expressionsForOrdering() {
		final List<String> expressions = newArrayList();
		for (final OrderByClause clause : querySpecs.getOrderByClauses()) {
			final QueryAliasAttribute attribute = clause.getAttribute();
			expressions.add(format(ORDER_BY_CLAUSE, //
					AliasQuoter.quote(as(nameForUserAttribute(attribute.getEntryTypeAlias(), attribute.getName()))), //
					clause.getDirection()));
		}
		// must not add default orderings for function calls
		if (!(querySpecs.getFromClause().getType() instanceof CMFunctionCall)) {
			expressions.add(AliasQuoter.quote(as(nameForSystemAttribute(querySpecs.getFromClause().getAlias(), Id))));
		}

		return expressions;
	}

	private void appendConditionOnNumberedQuery() {
		final WhereClause whereClause = (querySpecs.getConditionOnNumberedQuery() == null) ? emptyWhereClause()
				: querySpecs.getConditionOnNumberedQuery();
		whereClause.accept(new NullWhereClauseVisitor() {
			@Override
			public void visit(final SimpleWhereClause whereClause) {
				whereClause.getOperator().accept(new NullOperatorAndValueVisitor() {
					@Override
					public void visit(final EqualsOperatorAndValue operatorAndValue) {
						final QueryAliasAttribute attribute = whereClause.getAttribute();
						final String quotedName = AliasQuoter.quote(as(nameForSystemAttribute(
								attribute.getEntryTypeAlias(), Id)));
						final String actual = sb.toString();
						sb.setLength(0);
						sb.append(format("SELECT * FROM (%s) AS numbered WHERE %s = %s", //
								actual, //
								quotedName, //
								operatorAndValue.getValue()));
					}
				});
			}
		});
	}

	private void appendLimitAndOffset() {
		appendPart(new LimitPartCreator(querySpecs));
		appendPart(new OffsetPartCreator(querySpecs));
	}

	private void appendPart(final PartCreator partCreator) {
		final String part = partCreator.getPart();
		if (isNotEmpty(part)) {
			sb.append(PARTS_SEPARATOR).append(part);
			params.addAll(partCreator.getParams());
		}
	}

	public String getQuery() {
		return sb.toString();
	}

	public Object[] getParams() {
		return params.toArray();
	}

	public ColumnMapper getColumnMapper() {
		return columnMapper;
	}

}

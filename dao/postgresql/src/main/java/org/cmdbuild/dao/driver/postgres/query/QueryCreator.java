package org.cmdbuild.dao.driver.postgres.query;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.join;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.BeginDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId1;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId2;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainQuerySource;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.EndDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.IdClass;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.RowNumber;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.RowsCount;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.User;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForSystemAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForUserAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAttribute;
import static org.cmdbuild.dao.query.clause.alias.NameAlias.as;

import java.util.List;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.quote.AliasQuoter;
import org.cmdbuild.dao.driver.postgres.quote.EntryTypeQuoter;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.DirectJoinClause;
import org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;

public class QueryCreator {

	private static final String SELECT = "SELECT ";
	private static final String DISTINCT_ON = "DISTINCT ON";
	private static final String ATTRIBUTES_SEPARATOR = ",";
	private static final String PARTS_SEPARATOR = " ";
	private static final String ORDER_BY = "ORDER BY";
	private static final String ORDER_BY_CLAUSE = "%s %s";

	private final StringBuilder sb;
	private final QuerySpecs querySpecs;
	private final List<Object> params;

	private SelectAttributesExpressions selectAttributesExpressions;
	private final JoinHolder joinHolder = new DefaultJoinHolder();
	private ColumnMapper columnMapper;

	public QueryCreator(final QuerySpecs querySpecs) {
		this.sb = new StringBuilder();
		this.querySpecs = querySpecs;
		this.params = newArrayList();
		buildQuery();
	}

	private void buildQuery() {
		selectAttributesExpressions = new SelectAttributesExpressions();
		columnMapper = new ColumnMapper(querySpecs, selectAttributesExpressions, joinHolder);
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
		for (final Alias alias : columnMapper.getClassAliases()) {
			addToSelect(alias, IdClass);
			addToSelect(alias, Id);
			addToSelect(alias, User);
			addToSelect(alias, BeginDate);
			if (querySpecs.getFromClause().isHistory()) {
				/**
				 * aliases for join clauses are not added here (e.g. the EndDate
				 * attribute is not present in a referenced table / lookup table
				 * when there is one or more direct join)
				 */
				if (alias.toString().equals(querySpecs.getFromClause().getType().getName())) {
					addToSelect(alias, EndDate);
				}
			}
		}

		for (final Alias alias : columnMapper.getDomainAliases()) {
			addToSelect(alias, DomainId);
			addToSelect(alias, DomainQuerySource);
			addToSelect(alias, Id);
			addToSelect(alias, User);
			addToSelect(alias, BeginDate);
			addToSelect(alias, EndDate);
			addToSelect(alias, DomainId1);
			addToSelect(alias, DomainId2);
		}

		sb.append(SELECT) //
				.append(distinct()) //
				.append(join(selectAttributesExpressions.getExpressions().iterator(), ATTRIBUTES_SEPARATOR));
	}

	private void addToSelect(final Alias typeAlias, final SystemAttributes systemAttribute) {
		selectAttributesExpressions.add( //
				typeAlias, //
				systemAttribute.getDBName(), //
				systemAttribute.getCastSuffix(), //
				as(nameForSystemAttribute(typeAlias, systemAttribute)));
	}

	private String distinct() {
		return querySpecs.distinct() ? //
		format("%s (%s) ", //
				DISTINCT_ON, //
				AliasQuoter.quote(as(nameForSystemAttribute(querySpecs.getFromClause().getAlias(), Id)))) //
				: EMPTY;
	}

	private void appendFrom() {
		final PartCreator fromPartCreator = new FromPartCreator(querySpecs);
		appendPart(fromPartCreator);
	}

	private void appendDirectJoin() {
		for (final DirectJoinClause directJoin : querySpecs.getDirectJoins()) {
			final String left = directJoin.isLeft() ? " LEFT" : EMPTY;
			sb.append(left + " JOIN ");
			sb.append(EntryTypeQuoter.quote(directJoin.getTargetClass()) + " AS "
					+ AliasQuoter.quote(directJoin.getTargetClassAlias()));
			sb.append(" ON " + quoteAttribute(directJoin.getTargetAttribute().getEntryTypeAlias(), //
					directJoin.getTargetAttribute().getName()));
			sb.append(" = " + quoteAttribute(directJoin.getSourceAttribute().getEntryTypeAlias(), //
					directJoin.getSourceAttribute().getName()));
			sb.append(" ");
		}
	}

	private void appendJoin() {
		for (final JoinHolder.JoinElement element : joinHolder.getElements()) {
			final String fromId = quoteAttribute(element.getFrom(), Id);
			final String toId = quoteAttribute(element.getTo(), Id);
			if (!fromId.equals(toId)) {
				sb.append(format(" LEFT JOIN %s ON %s = %s", AliasQuoter.quote(element.getFrom()), fromId, toId));
			}
		}
		final PartCreator joinCreator = new JoinCreator(querySpecs.getFromClause().getAlias(), querySpecs.getJoins(),
				columnMapper);
		appendPart(joinCreator);
	}

	private void appendWhere() {
		final PartCreator wherePartCreator = new WherePartCreator(querySpecs);
		appendPart(wherePartCreator);
	}

	private void appendPart(final PartCreator partCreator) {
		final String part = partCreator.getPart();
		if (isNotEmpty(part)) {
			sb.append(PARTS_SEPARATOR).append(part);
			params.addAll(partCreator.getParams());
		}
	}

	private void appendNumberingAndOrder() {
		final String actual = sb.toString();
		sb.setLength(0);

		final List<String> selectAttributes = newArrayList();
		// any attribute of default query
		selectAttributes.add("*");
		// count
		selectAttributes.add(format("(SELECT count(*) FROM (%s) AS main) AS %s", //
				actual, //
				nameForSystemAttribute(querySpecs.getFromClause().getAlias(), RowsCount)));
		// row number (if possible)
		final String orderByExpression = join(expressionsForOrdering(), ATTRIBUTES_SEPARATOR);
		if (!orderByExpression.isEmpty()) {
			selectAttributes.add(format("row_number() OVER (%s %s) AS %s", //
					ORDER_BY, //
					orderByExpression, //
					nameForSystemAttribute(querySpecs.getFromClause().getAlias(), RowNumber)));
		}

		sb.append(format("SELECT %s FROM (%s) AS main", //
				join(selectAttributes, ATTRIBUTES_SEPARATOR), //
				actual));

		// parameters must be doubled
		params.addAll(params);

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
		if (expressions.isEmpty() && !(querySpecs.getFromClause().getType() instanceof CMFunctionCall)) {
			expressions.add(AliasQuoter.quote(as(nameForSystemAttribute(querySpecs.getFromClause().getAlias(), Id))));
		}
		return expressions;
	}

	private void appendLimitAndOffset() {
		final Long limitValue = querySpecs.getLimit();
		final String limit = (limitValue == null || limitValue == 0) ? "ALL" : querySpecs.getLimit().toString();
		sb.append(format(" LIMIT %s", limit));

		final Long offsetValue = querySpecs.getOffset();
		final String offset = (offsetValue == null) ? Long.toString(0L) : querySpecs.getOffset().toString();
		sb.append(format(" OFFSET %s", offset));
	}

	private void appendConditionOnNumberedQuery() {
		if (querySpecs.getConditionOnNumberedQuery() instanceof SimpleWhereClause) {
			final SimpleWhereClause whereClause = (SimpleWhereClause) querySpecs.getConditionOnNumberedQuery();
			final QueryAliasAttribute attribute = whereClause.getAttribute();
			final String quotedName = AliasQuoter.quote(as(nameForSystemAttribute(attribute.getEntryTypeAlias(), Id)));
			if (whereClause.getOperator() instanceof EqualsOperatorAndValue) {
				final EqualsOperatorAndValue operatorAndValue = (EqualsOperatorAndValue) whereClause.getOperator();
				final String actual = sb.toString();
				sb.setLength(0);
				sb.append(format("SELECT * FROM (%s) AS numbered WHERE %s = %s", //
						actual, //
						quotedName, //
						operatorAndValue.getValue()));
			}
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

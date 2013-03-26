package org.cmdbuild.dao.driver.postgres.query;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.join;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.BeginDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainQuerySource;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.EndDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.IdClass;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Row;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.User;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForSystemAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForUserAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAttribute;
import static org.cmdbuild.dao.query.clause.alias.NameAlias.as;

import java.util.List;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.quote.AliasQuoter;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
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
		appendJoin();
		appendWhere();
		appendNumberingAndOrder();
		appendConditionOnNumberedQuery();
	}

	private void appendSelect() {
		for (final Alias alias : columnMapper.getClassAliases()) {
			addToSelect(alias, IdClass);
			addToSelect(alias, Id);
			addToSelect(alias, User);
			addToSelect(alias, BeginDate);
			if (querySpecs.getFromClause().isHistory()) {
				addToSelect(alias, EndDate);
			}
			/*
			 * The from clause does not have an EndDate value
			 * columnMapper.addSystemSelectAttribute(getSelectString(a,
			 * SystemAttributes.EndDate));
			 */
		}

		for (final Alias alias : columnMapper.getDomainAliases()) {
			addToSelect(alias, DomainId);
			addToSelect(alias, DomainQuerySource);
			addToSelect(alias, Id);
			addToSelect(alias, User);
			addToSelect(alias, BeginDate);
			addToSelect(alias, EndDate);
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

	private void appendConditionOnNumberedQuery() {
		final String actual = sb.toString();
		if (querySpecs.getConditionOnNumberedQuery() instanceof SimpleWhereClause) {
			sb.setLength(0);
			final SimpleWhereClause swc = (SimpleWhereClause) querySpecs.getConditionOnNumberedQuery();
			final QueryAliasAttribute attribute = swc.getAttribute();
			final String quotedName = AliasQuoter.quote(as(nameForSystemAttribute(attribute.getEntryTypeAlias(), Id)));
			if (swc.getOperator() instanceof EqualsOperatorAndValue) {
				final EqualsOperatorAndValue ov = (EqualsOperatorAndValue) swc.getOperator();
				sb.append(format("SELECT * FROM (%s) AS numbered WHERE %s=%s", actual, quotedName, ov.getValue()));
			}
		}
	}

	private void appendPart(final PartCreator partCreator) {
		final String part = partCreator.getPart();
		if (isNotEmpty(part)) {
			sb.append(PARTS_SEPARATOR).append(part);
			params.addAll(partCreator.getParams());
		}
	}

	private void appendNumberingAndOrder() {
		final List<String> expressions = newArrayList();

		for (final OrderByClause clause : querySpecs.getOrderByClauses()) {
			final QueryAliasAttribute attribute = clause.getAttribute();
			expressions.add(format(ORDER_BY_CLAUSE, //
					AliasQuoter.quote(as(nameForUserAttribute(attribute.getEntryTypeAlias(), attribute.getName()))), //
					clause.getDirection()));
		}

		if (querySpecs.numbered() || !expressions.isEmpty()) {
			final String selectAttributes;
			if (querySpecs.numbered()) {
				final String orderings;
				if (expressions.isEmpty()) {
					orderings = AliasQuoter
							.quote(as(nameForSystemAttribute(querySpecs.getFromClause().getAlias(), Id)));
				} else {
					orderings = join(expressions, ATTRIBUTES_SEPARATOR);
				}
				selectAttributes = format("*, row_number() OVER (%s %s) AS %s", ORDER_BY, //
						orderings, //
						nameForSystemAttribute(querySpecs.getFromClause().getAlias(), Row));
			} else {
				selectAttributes = "*";
			}

			final String actual = sb.toString();
			sb.setLength(0);
			final String numberedInner = sb.append(format("SELECT %s FROM (%s) AS main", selectAttributes, actual))
					.toString();
			if (!expressions.isEmpty()) {
				sb.append(format(" %s %s", ORDER_BY, join(expressions, ATTRIBUTES_SEPARATOR)));
			}
			if (querySpecs.getConditionOnNumberedQuery() instanceof SimpleWhereClause && querySpecs.numbered()) {
				// appendConditionOnNumberedQuery();
				final SimpleWhereClause swc = (SimpleWhereClause) querySpecs.getConditionOnNumberedQuery();
				final QueryAliasAttribute attribute = swc.getAttribute();
				final String quotedName = AliasQuoter
						.quote(as(nameForSystemAttribute(attribute.getEntryTypeAlias(), Id)));
				if (swc.getOperator() instanceof EqualsOperatorAndValue) {
					final EqualsOperatorAndValue ov = (EqualsOperatorAndValue) swc.getOperator();
					sb.setLength(0);
					sb.append(format("SELECT * FROM (%s) AS numbered WHERE %s=%s", numberedInner, quotedName,
							ov.getValue()));
				}
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

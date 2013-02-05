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
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.User;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.tableoid;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForSystemAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForUserAttribute;
import static org.cmdbuild.dao.query.clause.alias.NameAlias.as;

import java.util.List;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.quote.AliasQuoter;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;

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
	private ColumnMapper columnMapper;

	public QueryCreator(final QuerySpecs query) {
		this.sb = new StringBuilder();
		this.querySpecs = query;
		this.params = newArrayList();
		buildQuery();
	}

	private void buildQuery() {
		selectAttributesExpressions = new SelectAttributesExpressions();
		columnMapper = new ColumnMapper(querySpecs, selectAttributesExpressions);
		columnMapper.addAllAttributes(querySpecs.getAttributes());

		appendSelect();
		appendFrom();
		appendJoin();
		appendWhere();
		appendOrderBy();
	}

	private void appendSelect() {
		/*
		 * FIXME
		 * 
		 * Anyway tableoid can't be used because of the history table UNLESS WE
		 * USE A SELECT FOR THE FROM ALSO (that fixes the EndDate problem also)
		 */
		for (final Alias alias : columnMapper.getClassAliases()) {
			addToSelect(alias, tableoid);
			addToSelect(alias, Id);
			addToSelect(alias, User);
			addToSelect(alias, BeginDate);
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
				AliasQuoter.quote(as(nameForSystemAttribute(querySpecs.getFromAlias(), Id)))) //
				: EMPTY;
	}

	private void appendFrom() {
		final PartCreator fromPartCreator = new FromPartCreator(querySpecs);
		appendPart(fromPartCreator);
	}

	private void appendJoin() {
		final PartCreator joinCreator = new JoinCreator(querySpecs.getFromAlias(), querySpecs.getJoins(), columnMapper);
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

	private void appendOrderBy() {
		final List<String> expressions = newArrayList();

		for (final OrderByClause clause : querySpecs.getOrderByClauses()) {
			final QueryAliasAttribute attribute = clause.getAttribute();
			expressions.add(format(ORDER_BY_CLAUSE, //
					AliasQuoter.quote(as(nameForUserAttribute(attribute.getEntryTypeAlias(), attribute.getName()))), //
					clause.getDirection()));
		}

		if (!expressions.isEmpty()) {
			final String actual = sb.toString();
			sb.setLength(0);
			sb.append(format("SELECT * FROM (%s) AS main %s %s", //
					actual, ORDER_BY, join(expressions, ATTRIBUTES_SEPARATOR)));
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

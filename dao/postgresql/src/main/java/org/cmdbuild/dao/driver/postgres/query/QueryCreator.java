package org.cmdbuild.dao.driver.postgres.query;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.join;
import static org.cmdbuild.dao.driver.postgres.Utils.aliasForSystemAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAlias;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;

import com.google.common.collect.Lists;

public class QueryCreator {

	private static final String SELECT = "SELECT ";
	private static final String DISTINCT_ON = "DISTINCT ON";
	private static final String ATTRIBUTES_SEPARATOR = ",";
	private static final String PARTS_SEPARATOR = " ";
	private static final String ORDER_BY = "ORDER BY";

	private final StringBuilder sb;
	private final QuerySpecs querySpecs;
	private final List<Object> params;
	private final ColumnMapper columnMapper;

	public QueryCreator(final QuerySpecs query) {
		this.sb = new StringBuilder();
		this.querySpecs = query;
		this.columnMapper = new ColumnMapper(query);
		this.params = new ArrayList<Object>();
		buildQuery();
	}

	private void buildQuery() {
		appendSelect();
		appendFrom();
		appendJoin();
		appendWhere();
		appendOrderBy();
	}

	private void appendSelect() {
		sb.append(SELECT) //
				.append(distinct()) //
				.append(quoteAttributes(querySpecs.getAttributes()));
	}

	private String distinct() {
		return querySpecs.distinct() ? //
		format("%s (%s) ", //
				DISTINCT_ON, //
				quoteAlias(aliasForSystemAttribute(querySpecs.getFromAlias(), SystemAttributes.Id))) //
				: EMPTY;
	}

	private String quoteAttributes(final Iterable<QueryAliasAttribute> attributes) {
		columnMapper.addAllUserAttributesForSelect(attributes);

		/*
		 * FIXME
		 * 
		 * Anyway tableoid can't be used because of the history table UNLESS WE
		 * USE A SELECT FOR THE FROM ALSO (that fixes the EndDate problem also)
		 */
		for (final Alias alias : columnMapper.getClassAliases()) {
			columnMapper.addSystemAttributeForSelect(alias, SystemAttributes.ClassId);
			columnMapper.addSystemAttributeForSelect(alias, SystemAttributes.Id);
			columnMapper.addSystemAttributeForSelect(alias, SystemAttributes.User);
			columnMapper.addSystemAttributeForSelect(alias, SystemAttributes.BeginDate);
			/*
			 * The from clause does not have an EndDate value
			 * columnMapper.addSystemSelectAttribute(getSelectString(a,
			 * SystemAttributes.EndDate));
			 */
		}

		for (final Alias alias : columnMapper.getDomainAliases()) {
			columnMapper.addSystemAttributeForSelect(alias, SystemAttributes.DomainId);
			columnMapper.addSystemAttributeForSelect(alias, SystemAttributes.DomainQuerySource);
			columnMapper.addSystemAttributeForSelect(alias, SystemAttributes.Id);
			columnMapper.addSystemAttributeForSelect(alias, SystemAttributes.User);
			columnMapper.addSystemAttributeForSelect(alias, SystemAttributes.BeginDate);
			columnMapper.addSystemAttributeForSelect(alias, SystemAttributes.EndDate);
		}

		return StringUtils.join(columnMapper.getAttributeExpressionsForSelect().iterator(), //
				ATTRIBUTES_SEPARATOR);
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
		if (StringUtils.isNotEmpty(part)) {
			sb.append(PARTS_SEPARATOR).append(part);
			params.addAll(partCreator.getParams());
		}
	}

	private void appendOrderBy() {
		final List<OrderByClause> clauses = Lists.newArrayList(querySpecs.getOrderByClauses());

		if (querySpecs.distinct()) {
			clauses.add(0, new OrderByClause( //
					attribute(querySpecs.getFromType(), SystemAttributes.Id.getDBName()), //
					Direction.ASC));
		}

		if (querySpecs.getOrderByClauses().isEmpty()) {
			return;
		}

		final List<String> orderings = Lists.newArrayList();
		for (final OrderByClause clause : clauses) {
			orderings.add(format("%s %s", //
					quoteAttribute(clause.getAttribute()), //
					clause.getDirection()));
		}

		sb.append(format(" %s %s", ORDER_BY, join(orderings, ATTRIBUTES_SEPARATOR)));
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

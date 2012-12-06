package org.cmdbuild.dao.driver.postgres.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;

import com.google.common.collect.Iterables;

public class QueryCreator {

	private static final String SELECT = "SELECT ";
	private static final String SELECT_ATTRIBUTES_SEPARATOR = ",";
	private static final String PARTS_SEPARATOR = " ";

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
	}

	private void appendSelect() {
		sb.append(SELECT).append(quoteAttributes(querySpecs.getAttributes()));
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

		return StringUtils.join( //
				Iterables.toArray(columnMapper.getAttributeExpressionsForSelect(), String.class), //
				SELECT_ATTRIBUTES_SEPARATOR);
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
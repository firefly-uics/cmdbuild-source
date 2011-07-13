package org.cmdbuild.dao.driver.postgres.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.Utils;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.ClassAlias;

public class QueryCreator {

	private final StringBuilder sb;
	private final QuerySpecs query;
	private final List<Object> params;
	private final ColumnMapper columnMapper;

	public QueryCreator(final QuerySpecs query) {
		this.sb = new StringBuilder();
		this.query = query;
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
		sb.append("SELECT ").append(quoteAttributes(query.getAttributes()));
	}

	private String quoteAttributes(final Iterable<QueryAliasAttribute> attributes) {
		addUserSelectAttributes(attributes);
		addSystemSelectAttributes(attributes);
		return StringUtils.join(columnMapper.getSelectAttributes(), ",");
	}

	private void addUserSelectAttributes(final Iterable<QueryAliasAttribute> attributes) {
		for (QueryAliasAttribute a : attributes) {
			columnMapper.addUserSelectAttribute(a);
		}
	}

	private void addSystemSelectAttributes(final Iterable<QueryAliasAttribute> attributes) {
		// FIXME! Anyway tableoid can't be used because of the history table UNLESS
		// WE USE A SELECT FOR THE FROM ALSO (that fixes the EndDate problem also)

		for (Alias a : columnMapper.getClassAliases()) {
			columnMapper.addSystemSelectAttribute(a, SystemAttributes.ClassId);
			columnMapper.addSystemSelectAttribute(a, SystemAttributes.Id);
			columnMapper.addSystemSelectAttribute(a, SystemAttributes.BeginDate);
			// The from clause does not have an EndDate value
			//columnMapper.addSystemSelectAttribute(getSelectString(a, SystemAttributes.EndDate));
		}

		for (Alias a : columnMapper.getDomainAliases()) {
			columnMapper.addSystemSelectAttribute(a, SystemAttributes.DomainId);
			columnMapper.addSystemSelectAttribute(a, SystemAttributes.DomainQuerySource);
			columnMapper.addSystemSelectAttribute(a, SystemAttributes.Id);
			columnMapper.addSystemSelectAttribute(a, SystemAttributes.BeginDate);
			columnMapper.addSystemSelectAttribute(a, SystemAttributes.EndDate);
		}
	}

	private void appendFrom() {
		final ClassAlias from = query.getDBFrom();
		sb.append(" FROM ONLY ").append(Utils.quoteType(from.getType())).append(" AS ").append(Utils.quoteAlias(from.getAlias()));
	}

	private void appendJoin() {
		final PartCreator joinCreator = new JoinCreator(query.getDBFrom().getAlias(), query.getJoins(), columnMapper);
		appendPart(joinCreator);
	}

	private void appendWhere() {
		final PartCreator wherePartCreator = new WherePartCreator(query.getDBFrom().getAlias(),
				query.getWhereClause());
		appendPart(wherePartCreator);
	}

	private void appendPart(final PartCreator partCreator) {
		sb.append(" ").append(partCreator.getPart());
		params.addAll(partCreator.getParams());
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
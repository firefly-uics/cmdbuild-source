package org.cmdbuild.cql.compiler.impl;

import java.util.HashSet;
import java.util.Set;

import org.cmdbuild.cql.CQLBuilderListener.FieldInputValue;
import org.cmdbuild.cql.compiler.From;
import org.cmdbuild.cql.compiler.GroupBy;
import org.cmdbuild.cql.compiler.Limit;
import org.cmdbuild.cql.compiler.Offset;
import org.cmdbuild.cql.compiler.OrderBy;
import org.cmdbuild.cql.compiler.Query;
import org.cmdbuild.cql.compiler.Select;
import org.cmdbuild.cql.compiler.Where;

public class QueryImpl extends CQLElementImpl implements Query, Limit, Offset {
	private FromImpl from;
	private SelectImpl select;
	private WhereImpl where;

	private OrderByImpl orderBy;
	private GroupByImpl groupBy;

	private int literalOffset;
	private String variableOffset = null;

	private int literalLimit;
	private String variableLimit = null;

	public FromImpl getFrom() {
		return from;
	}

	public GroupByImpl getGroupBy() {
		return groupBy;
	}

	public OrderByImpl getOrderBy() {
		return orderBy;
	}

	public Limit getLimit() {
		return this;
	}

	public Offset getOffset() {
		return this;
	}

	public SelectImpl getSelect() {
		return select;
	}

	public WhereImpl getWhere() {
		return where;
	}

	public void setFrom(final From from) {
		this.from = (FromImpl) from;
	}

	public void setGroupBy(final GroupBy groupBy) {
		this.groupBy = (GroupByImpl) groupBy;
	}

	public void setOrderBy(final OrderBy orderBy) {
		this.orderBy = (OrderByImpl) orderBy;
	}

	public void setSelect(final Select select) {
		this.select = (SelectImpl) select;
	}

	public void setWhere(final Where where) {
		this.where = (WhereImpl) where;
	}

	public void setLimit(final int limit) {
		this.literalLimit = limit;
	}

	public void setLimit(final FieldInputValue limit) {
		setLimit(-1);
		this.variableLimit = limit.getVariableName();
	}

	public void setOffset(final int offset) {
		this.literalOffset = offset;
	}

	public void setOffset(final FieldInputValue offset) {
		setOffset(-1);
		this.variableOffset = offset.getVariableName();
	}

	public Object getOffsetValue() {
		return (this.variableOffset == null) ? this.literalOffset : this.variableOffset;
	}

	public Object getLimitValue() {
		return (this.variableLimit == null) ? this.literalLimit : this.variableLimit;
	}

	public void setLimit(final Limit limit) {
	}

	public void setOffet(final Offset offset) {
	}

	public void check() {
		getFrom().check();
	}

	private final Set<FieldInputValue> variableValues = new HashSet<FieldInputValue>();

	public Set<FieldInputValue> getVariables() {
		return variableValues;
	}

	public void addVariable(final FieldInputValue variable) {
		variableValues.add(variable);
	}
}

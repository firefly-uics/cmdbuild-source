package org.cmdbuild.dao.query.clause.where;

import org.cmdbuild.dao.query.clause.QueryAliasAttribute;

public class SimpleWhereClause implements WhereClause {

	public enum Operator {
		EQUALS;
	}

	private final QueryAliasAttribute attribute;
	private final Operator operator;
	private final Object value;

	public SimpleWhereClause(final QueryAliasAttribute attribute, final Operator operator, final Object value) {
		super();
		this.attribute = attribute;
		this.operator = operator;
		this.value = value;
	}

	public QueryAliasAttribute getAttribute() {
		return attribute;
	}

	public Operator getOperator() {
		return operator;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);
	}

}

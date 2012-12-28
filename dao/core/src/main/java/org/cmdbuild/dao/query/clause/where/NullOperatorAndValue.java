package org.cmdbuild.dao.query.clause.where;

public class NullOperatorAndValue implements OperatorAndValue {

	private final Object value;

	private NullOperatorAndValue() {
		this.value = null;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public void accept(final OperatorAndValueVisitor visitor) {
		visitor.visit(this);
	}

	public static OperatorAndValue isNull() {
		return new NullOperatorAndValue();
	}

}

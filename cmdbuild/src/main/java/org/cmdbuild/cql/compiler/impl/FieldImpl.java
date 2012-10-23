package org.cmdbuild.cql.compiler.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cmdbuild.cql.CQLBuilderListener.FieldOperator;
import org.cmdbuild.cql.CQLBuilderListener.FieldValueType;
import org.cmdbuild.cql.compiler.where.Field;
import org.cmdbuild.cql.compiler.where.fieldid.FieldId;

public class FieldImpl extends WhereElementImpl implements Field {
	FieldId id;
	FieldOperator operator;
	List<FieldValue> values = new ArrayList<FieldValue>();
	
	public FieldId getId() {
		return id;
	}
	public FieldOperator getOperator() {
		return operator;
	}

	public Collection<FieldValue> getValues() {
		return values;
	}

	public void nextValue(Object value) {
		values.get(values.size()-1).setValue(value);
	}

	public void nextValueType(FieldValueType type) {
		values.add(new FieldValue(type));
	}

	public void setId(FieldId id) {
		this.id = id;
	}

	public void setOperator(FieldOperator operator) {
		this.operator = operator;
	}

}

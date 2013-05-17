package org.cmdbuild.cql.compiler.impl;

import org.cmdbuild.cql.compiler.select.FieldSelect;

public class FieldSelectImpl extends CQLElementImpl implements FieldSelect {

	String as;
	String name;
	
	public String getAs() {
		return as;
	}
	public String getName() {
		return name;
	}

	public void setAs(String attributeAs) {
		this.as = attributeAs;
	}

	public void setName(String attributeName) {
		this.name = attributeName;
	}
	
}

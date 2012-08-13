package org.cmdbuild.cql.compiler.impl;

import org.cmdbuild.cql.compiler.select.FunctionSelect;

@SuppressWarnings("unchecked")
public class FunctionSelectImpl extends SelectElementImpl implements
		FunctionSelect {
	
	String name;
	String as;

	public String getAs() {
		return as;
	}
	public String getName() {
		return name;
	}

	public void setAs(String functionAs) {
		this.as = functionAs;
	}

	public void setName(String functionName) {
		this.name = functionName;
	}

}

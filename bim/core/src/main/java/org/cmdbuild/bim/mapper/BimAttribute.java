package org.cmdbuild.bim.mapper;

import org.cmdbuild.bim.model.Attribute;

public class BimAttribute implements Attribute {

	private String name;
	private String value;

	public BimAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public BimAttribute() {}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value){
		this.value = value;
	}

}

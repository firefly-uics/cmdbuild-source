package org.cmdbuild.bim.mapper;

import org.cmdbuild.bim.model.Attribute;

public class DefaultAttribute implements Attribute {

	private final String name;
	private String value;

	public DefaultAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isValid() {
		return true;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public void setValue(String value){
		this.value = value;
	}
	
	@Override
	public String toString(){
		return name + ": " + value;
	}

}

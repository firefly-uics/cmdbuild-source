package org.cmdbuild.bim.model.implementation;

import org.cmdbuild.bim.model.EntityDefinition;

public class SimpleAttributeDefinition extends AttributeDefinitionImpl {

	public SimpleAttributeDefinition(String attributeName) {
		super(attributeName);
	}
	
	private String value = "";

	@Override
	public EntityDefinition getReference() {
		return EntityDefinition.NULL_ENTITYDEFINITION;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}

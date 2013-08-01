package org.cmdbuild.bim.model.implementation;

import org.cmdbuild.bim.model.AttributeDefinition;

public abstract class AttributeDefinitionImpl implements AttributeDefinition {

	private String name;
	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public AttributeDefinitionImpl() {
	};

	public AttributeDefinitionImpl(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}

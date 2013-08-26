package org.cmdbuild.bim.model.implementation;

import java.util.List;

import org.cmdbuild.bim.model.AttributeDefinition;
import org.cmdbuild.bim.model.EntityDefinition;

public class EntityDefinitionExportImpl implements EntityDefinition {

	private String typeName;
	private String label;
	private String shape;


	public EntityDefinitionExportImpl(String name) {
		this.typeName = name;
		label = "";
		shape = "";
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getShape() {
		return shape;
	}
	
	@Override
	public void setShape(String shape) {
		this.shape = shape;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public List<AttributeDefinition> getAttributes() {
		return null;
	}

}

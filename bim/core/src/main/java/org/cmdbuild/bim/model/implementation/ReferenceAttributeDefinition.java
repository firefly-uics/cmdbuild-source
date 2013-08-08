package org.cmdbuild.bim.model.implementation;

import org.cmdbuild.bim.model.EntityDefinition;

public class ReferenceAttributeDefinition extends AttributeDefinitionImpl {

	private EntityDefinition reference;

	public ReferenceAttributeDefinition(String attributeName) {
		super(attributeName);
	}

	@Override
	public EntityDefinition getReference() {
		return reference;
	}

	public void setReference(EntityDefinition referencedEntity) {
		this.reference = referencedEntity;
	}

}

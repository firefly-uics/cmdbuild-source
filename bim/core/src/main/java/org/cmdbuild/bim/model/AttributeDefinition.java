package org.cmdbuild.bim.model;

public interface AttributeDefinition {

	String getName();

	String getLabel();

	void setLabel(String label);

	// boolean isSimple();
	//
	// boolean isMultiple();
	//
	// boolean isReference();

	EntityDefinition getReference();

}

package org.cmdbuild.shark.toolagent;

public class SelectAttributeToolAgent extends ManageAttributeToolAgent {

	private static final String ATTRIBUTE_VALUE = "AttributeValue";

	@Override
	protected String outputName() {
		return ATTRIBUTE_VALUE;
	}
	
	@Override
	protected Object attributeValue() {
		return super.attributeValue().toString();
	}

}

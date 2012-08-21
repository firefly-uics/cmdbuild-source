package org.cmdbuild.shark.toolagent;

public class SelectReferenceByReferenceToolAgent extends ManageAttributeToolAgent {

	private static final String OUT_REF = "OutRef";

	@Override
	protected String outputName() {
		return OUT_REF;
	}

	@Override
	protected Object outputValue() {
		return getWorkflowApi().referenceTypeFrom(referencedId());
	}

	private int referencedId() {
		final int referencedId = Integer.parseInt(attributeValue());
		return referencedId;
	}

}

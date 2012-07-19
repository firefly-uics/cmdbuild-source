package org.cmdbuild.shark.toolagent;

import org.cmdbuild.common.Constants;
import org.cmdbuild.workflow.type.ReferenceType;

public class SelectReferenceToolAgent extends AbstractConditionalToolAgent {

	private static final String CLASS_NAME = "ClassName";
	private static final String CODE = "Code";
	private static final String ATTRIBUTE_NAME = "AttributeName";
	private static final String ATTRIBUTE_VALUE = "AttributeValue";

	private static final String OUTPUT = "OutRef";

	@Override
	protected void innerInvoke() throws Exception {
		final String className = getParameterValue(CLASS_NAME);
		final String attributeName;
		final String attributeValue;
		if (hasParameter(CODE)) {
			attributeName = Constants.CODE_ATTRIBUTE;
			attributeValue = getParameterValue(CODE);
		} else {
			attributeName = getParameterValue(ATTRIBUTE_NAME);
			attributeValue = getParameterValue(ATTRIBUTE_VALUE);
		}

		final ReferenceType output = getWorkflowApi().selectReference(className, attributeName, attributeValue);

		setParameterValue(OUTPUT, output);
	}

}

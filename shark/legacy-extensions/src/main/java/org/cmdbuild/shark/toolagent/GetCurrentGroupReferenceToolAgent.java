package org.cmdbuild.shark.toolagent;

import org.cmdbuild.workflow.Constants;
import org.cmdbuild.workflow.type.ReferenceType;

public class GetCurrentGroupReferenceToolAgent extends AbstractConditionalToolAgent {

	private static final String OUTPUT = "GroupRef";

	@Override
	protected void innerInvoke() throws Exception {
		final ReferenceType groupReference = getProcessAttributeValue(Constants.CURRENT_GROUP_VARIABLE);
		setParameterValue(OUTPUT, groupReference);
	}

}

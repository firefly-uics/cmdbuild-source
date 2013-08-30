package org.cmdbuild.logic.email.rules;

import org.apache.commons.lang.Validate;
import org.cmdbuild.logic.workflow.WorkflowLogic;

public class StartWorkflowFactory implements RuleFactory<StartWorkflow> {

	private final WorkflowLogic workflowLogic;

	public StartWorkflowFactory(final WorkflowLogic workflowLogic) {
		this.workflowLogic = workflowLogic;
	}

	@Override
	public StartWorkflow create() {
		Validate.notNull(workflowLogic, "null workflow logic");
		return new StartWorkflow(workflowLogic);
	}

}

package org.cmdbuild.logic;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowEngine;

/**
 * Business Logic Layer for Workflow Operations
 */
public class WorkflowLogic {

	private final CMWorkflowEngine wfEngine;

	@Legacy("Temporary constructor before switching to Spring DI")
	public WorkflowLogic(final UserContext userCtx) {
		wfEngine = TemporaryObjectsBeforeSpringDI.getWorkflowEngine(userCtx);
	}

	public WorkflowLogic(final CMWorkflowEngine wfEngine) {
		this.wfEngine = wfEngine;
	}

	public CMProcessClass getProcessClass(final Object processClassNameOrId) {
		return wfEngine.findProcessClass(processClassNameOrId);
	}

}

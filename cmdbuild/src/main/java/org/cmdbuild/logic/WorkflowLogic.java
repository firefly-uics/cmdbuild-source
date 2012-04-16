package org.cmdbuild.logic;

import javax.activation.DataSource;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CMWorkflowEngine;
import org.cmdbuild.workflow.CMWorkflowException;

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

	public DataSource getProcessDefinitionTemplate(final Object processClassNameOrId) throws CMWorkflowException {
		return wfEngine.findProcessClass(processClassNameOrId).getDefinitionTemplate();
	}

	public String[] getProcessDefinitionVersions(final Object processClassNameOrId) throws CMWorkflowException {
		return wfEngine.findProcessClass(processClassNameOrId).getDefinitionVersions();
	}

}

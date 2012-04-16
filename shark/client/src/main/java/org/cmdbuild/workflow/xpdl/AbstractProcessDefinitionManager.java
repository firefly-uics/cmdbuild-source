package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.service.CMWorkflowService;

/**
 * Process Definition Manager that uses XPDL definitions.
 */
public abstract class AbstractProcessDefinitionManager implements ProcessDefinitionManager {

	private final CMWorkflowService workflowService;

	public AbstractProcessDefinitionManager(final CMWorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@Override
	public final String[] getVersions(final CMProcessClass process) throws CMWorkflowException {
		return workflowService.getPackageVersions(getPackageId(process));
	}

	@Legacy("As in 1.x")
	protected final String getPackageId(final CMProcessClass process) {
		return "Package_" + process.getName().toLowerCase();
	}

	@Legacy("As in 1.x")
	protected final String getProcessId(final CMProcessClass process) {
		return "Process_" + process.getName().toLowerCase();
	}

}

package org.cmdbuild.servlets.json;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.xpdl.ProcessDefinitionException;

public class Workflow extends JSONBase {

	@Admin
	@JSONExported
	public DataHandler downloadXpdlTemplate(
			@Parameter(value = "idClass", required = true) Long processClassId,
			final UserContext userCtx) throws ProcessDefinitionException {
		WorkflowLogic logic = new WorkflowLogic(userCtx);
		final CMProcessClass process = logic.getProcessClass(processClassId);
		final DataSource ds = process.getDefinitionTemplate();
		return new DataHandler(ds);
	}
}

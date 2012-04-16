package org.cmdbuild.servlets.json;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.CMWorkflowException;

public class Workflow extends JSONBase {

	@Admin
	@JSONExported
	public DataHandler downloadXpdlTemplate(
			@Parameter(value = "idClass", required = true) Long processClassId,
			final UserContext userCtx) throws CMWorkflowException {
		WorkflowLogic logic = new WorkflowLogic(userCtx);
		final DataSource ds = logic.getProcessDefinitionTemplate(processClassId);
		return new DataHandler(ds);
	}

	@Admin
	@JSONExported
	public JsonResponse xpdlVersions(
			@Parameter(value = "idClass", required = true) Long processClassId,
			final UserContext userCtx) throws CMWorkflowException {
		WorkflowLogic logic = new WorkflowLogic(userCtx);
		final String[] versions = logic.getProcessDefinitionVersions(processClassId);
		return JsonResponse.success(versions);
	}

}

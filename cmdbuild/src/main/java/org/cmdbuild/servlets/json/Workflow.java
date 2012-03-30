package org.cmdbuild.servlets.json;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.xpdl.XPDLException;

public class Workflow extends JSONBase {

	@Admin
	@JSONExported
	public DataHandler downloadXpdlTemplate(
			@Parameter(value = "idClass", required = true) Long processClassId,
			final UserContext userCtx) throws XPDLException {
		WorkflowLogic logic = new WorkflowLogic(userCtx);
		final DataSource ds = logic.getXpdlTemplate(processClassId);
		return new DataHandler(ds);
	}
}

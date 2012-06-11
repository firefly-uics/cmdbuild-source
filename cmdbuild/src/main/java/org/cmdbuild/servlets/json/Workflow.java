package org.cmdbuild.servlets.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonActivityDefinition;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;

public class Workflow extends JSONBase {

	@JSONExported
	public JsonResponse getStartActivity(
			@Parameter("idClass") Long processClassId,
			final UserContext userCtx) throws CMWorkflowException {
		final WorkflowLogic logic = new WorkflowLogic(userCtx);
		final CMActivity ad = logic.getStartActivity(processClassId);
		return JsonResponse.success(JsonActivityDefinition.fromActivityDefinition(ad));
	}

	@JSONExported
	public JsonResponse saveActivity(
//	Id, ProcessInstanceId, WorkItemId, attributes, ww
			@Parameter("IdClass") Long processClassId,
			@Parameter("advance") boolean advance,
			final UserContext userCtx) throws CMWorkflowException {
		final WorkflowLogic logic = new WorkflowLogic(userCtx);
		final Map<String, Object> vars = new HashMap<String, Object>(); // TODO
//		if (processCardId) {
			final CMProcessInstance procInst = logic.startProcess(processClassId, vars, advance);
//		} else {
//			...
//		}
		return JsonResponse.success(new HashMap<String, Object>() {{
			put("Id", procInst.getCardId());
			put("IdClass", procInst.getType().getId());
			put("ProcessInstanceId", procInst.getProcessInstanceId());
			// WorkItemId -> WHY?!?!?!?!?!?!
		}});
	}

	@Admin
	@JSONExported
	public DataHandler downloadXpdlTemplate(
			@Parameter("idClass") Long processClassId,
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

	@Admin
	@JSONExported
	public DataHandler downloadXpdl(
			@Parameter("idClass") Long processClassId,
			@Parameter("version") String version,
			final UserContext userCtx) throws CMWorkflowException {
		WorkflowLogic logic = new WorkflowLogic(userCtx);
		final DataSource ds = logic.getProcessDefinition(processClassId, version);
		return new DataHandler(ds);
	}

	@Admin
	@JSONExported
	public JsonResponse uploadXpdl(
			@Parameter("idClass") Long processClassId,
			@Parameter(value="xpdl", required=false) final FileItem xpdlFile,
			@Parameter(value="sketch",required=false) FileItem sketchFile,
			final UserContext userCtx) throws CMWorkflowException, IOException {
		final List<String> messages = new ArrayList<String>();
		WorkflowLogic logic = new WorkflowLogic(userCtx);
		if (xpdlFile.getSize() != 0) {
			logic.updateProcessDefinition(processClassId, wrapAsDataSource(xpdlFile));
			messages.add("saved_xpdl");
		}

		// Wrong behavior, but kept as it was before the refactoring
		logic.removeSketch(processClassId);
		if (sketchFile.getSize() != 0) {
			logic.addSketch(processClassId, wrapAsDataSource(sketchFile));
			messages.add("saved_image");
		} else {
			messages.add("deleted_image");
		}
		return JsonResponse.success(messages);
	}

	private DataSource wrapAsDataSource(final FileItem xpdlFile) {
		return new DataSource() {
			public String getContentType() {
				return xpdlFile.getContentType();
			}
			public InputStream getInputStream() throws IOException {
				return xpdlFile.getInputStream();
			}
			public String getName() {
				return xpdlFile.getName();
			}
			public OutputStream getOutputStream() throws IOException {
				return xpdlFile.getOutputStream();
			}
		};
	}

}

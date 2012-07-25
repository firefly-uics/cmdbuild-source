package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.management.ModCard.applySortToCardQuery;

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
import org.cmdbuild.elements.interfaces.ProcessQuery;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonActivityDefinition;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonActivityInstance;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonProcessCard;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowEngine;
import org.cmdbuild.workflow.CMWorkflowException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Workflow extends JSONBase {
	/**
	 * Get the workItems OR closed processes, depending on the state required.
	 * If required state is closed, then processes with state closed.*
	 * (completed/terminated/aborted) will be returned.
	 * If state is open, the activities in open.not_running.not_started 
	 * and open.running will be returned
	 * 
	 * @param params
	 * @return
	 * @throws JSONException
	 * @throws CMWorkflowException
	 */
	@JSONExported
	public JsonResponse getProcessInstanceList(JSONObject serializer, UserContext userCtx, // TODO: but is the right name? It returns ProcessInstances
			@Parameter("state") String flowStatus,
			@Parameter("limit") int limit,
			@Parameter("start") int offset,
			@Parameter(value = "sort", required = false) JSONArray sorters,
			@Parameter(value = "query", required = false) String fullTextQuery,


			/*
			 * Don't clone it or getCardPosition does not work,
			 * unless sort and query are set somewhere else
			 * already filtered with the passed flow status
			 */
			ProcessQuery processQuery

	) throws JSONException, CMWorkflowException {

		final WorkflowLogic logic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);
		final List<JsonProcessCard> processInstances = new ArrayList<JsonProcessCard>();

		configureQuery(processQuery, fullTextQuery, sorters, limit, offset);

		for (CMProcessInstance pi : logic.query(processQuery)) {
			processInstances.add(new JsonProcessCard(pi));
		}

		final int totalRows =  processQuery.getTotalRows();

		return JsonResponse.success(new HashMap<String, Object>() {{
			put("results", totalRows);
			put("rows", processInstances);
		}});

	}

	private void configureQuery(ProcessQuery processQuery,
			String fullTextQuery, JSONArray sorters, int limit, int offset)
			throws JSONException {
		if (fullTextQuery != null) {
			processQuery.fullText(fullTextQuery.trim());
		}
		applySortToCardQuery(sorters, processQuery);
		processQuery.subset(offset, limit).count();
	}

	@JSONExported
	public JsonResponse getStartActivity(
			@Parameter("classId") Long processClassId,
			final UserContext userCtx) throws CMWorkflowException {
		final WorkflowLogic logic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);
		final CMActivity ad = logic.getStartActivity(processClassId);
		return JsonResponse.success(JsonActivityDefinition.fromActivityDefinition(ad));
	}

	@JSONExported
	public JsonResponse getActivityInstance(
			@Parameter("classId") Long processClassId,
			@Parameter("cardId") Long processInstanceId,
			@Parameter("activityInstanceId") String activityInstanceId,
			final UserContext userCtx) throws CMWorkflowException {

		final WorkflowLogic logic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);
		final CMActivityInstance ad = logic.getActivityInstance(processClassId, processInstanceId, activityInstanceId);

		return JsonResponse.success(new JsonActivityInstance(ad));
	}

	@JSONExported
	public JsonResponse saveActivity(
			@Parameter("classId") final Long processClassId,
			@Parameter(value = "cardId", required=false) final long processCardId, // even with Long, it won't be null
			@Parameter(value = "activityInstanceId", required=false) final String activityInstanceId,
			@Parameter("attributes") final String jsonVars,
			@Parameter("advance") boolean advance,
			@Parameter("ww") String jsonWidgetSubmission,
			final UserContext userCtx) throws CMWorkflowException, Exception {

		final WorkflowLogic logic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);
		final CMProcessInstance procInst;
		@SuppressWarnings("unchecked")
		final Map<String, Object> vars = new ObjectMapper().readValue(jsonVars, Map.class);
		@SuppressWarnings("unchecked")
		final Map<String, Object> widgetSubmission = new ObjectMapper().readValue(jsonWidgetSubmission, Map.class);

		if (processCardId > 0) { // should check for null
			procInst = logic.updateProcess(processClassId, processCardId, activityInstanceId, vars, widgetSubmission, advance);
		} else {
			procInst = logic.startProcess(processClassId, vars, widgetSubmission, advance);
		}

		return JsonResponse.success(new HashMap<String, Object>() {{
			put("Id", procInst.getCardId());
			put("IdClass", procInst.getType().getId());
			put("ProcessInstanceId", procInst.getProcessInstanceId());
			// WorkItemId -> WHY?!?!?!?!?!?!
		}});
	}

	@JSONExported
	public JsonResponse abortprocess(
			@Parameter("classId") final Long processClassId,
			@Parameter("cardId") final long processCardId,
			final UserContext userCtx) throws CMWorkflowException {

		final WorkflowLogic logic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);

		if (processCardId < 0) { // should check for null
			//TODO throw an exception
		} else {
			logic.abortProcess(processClassId, processCardId);
		}

		return JsonResponse.success(null);
	}

	@Admin
	@JSONExported
	public DataHandler downloadXpdlTemplate(
			@Parameter("idClass") Long processClassId,
			final UserContext userCtx) throws CMWorkflowException {
		final WorkflowLogic logic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);
		final DataSource ds = logic.getProcessDefinitionTemplate(processClassId);
		return new DataHandler(ds);
	}

	@Admin
	@JSONExported
	public JsonResponse xpdlVersions(
			@Parameter(value = "idClass", required = true) Long processClassId,
			final UserContext userCtx) throws CMWorkflowException {
		final WorkflowLogic logic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);
		final String[] versions = logic.getProcessDefinitionVersions(processClassId);
		return JsonResponse.success(versions);
	}

	@Admin
	@JSONExported
	public DataHandler downloadXpdl(
			@Parameter("idClass") Long processClassId,
			@Parameter("version") String version,
			final UserContext userCtx) throws CMWorkflowException {
		final WorkflowLogic logic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);
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
		final WorkflowLogic logic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);
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

	@Admin
	@JSONExported
	public void sync(UserContext userCtx) throws CMWorkflowException {
		final CMWorkflowEngine workflowEngine = TemporaryObjectsBeforeSpringDI.getWorkflowEngine(userCtx);
		workflowEngine.sync();
	}
}

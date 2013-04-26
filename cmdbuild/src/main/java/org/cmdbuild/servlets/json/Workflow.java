package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.ComunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.ComunicationConstants.SORT;
import static org.cmdbuild.servlets.json.ComunicationConstants.START;
import static org.cmdbuild.servlets.json.ComunicationConstants.STATE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonActivityDefinition;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonActivityInstance;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonProcessCard;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.ActivityPerformer;
import org.cmdbuild.workflow.ActivityPerformerExpressionEvaluator;
import org.cmdbuild.workflow.BshActivityPerformerExpressionEvaluator;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class Workflow extends JSONBaseWithSpringContext {

	/**
	 * Get the workItems OR closed processes, depending on the state required.
	 * If required state is closed, then processes with state closed.*
	 * (completed/terminated/aborted) will be returned. If state is open, the
	 * activities in open.not_running.not_started and open.running will be
	 * returned
	 * 
	 * @param params
	 * @return
	 * @throws JSONException
	 * @throws CMWorkflowException
	 */
	// TODO: but is the right name? It returns ProcessInstances
	@JSONExported
	@SuppressWarnings("serial")
	public JsonResponse getProcessInstanceList( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(LIMIT) final int limit, //
			@Parameter(START) final int offset, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			@Parameter(STATE) final String flowStatus //
	) throws JSONException, CMWorkflowException {
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(flowStatusHelper().merge(filter, flowStatus)) //
				.build();

		final List<JsonProcessCard> processInstances = Lists.newArrayList();
		final PagedElements<UserProcessInstance> response = workflowLogic().query(className, queryOptions);
		for (final UserProcessInstance pi : response) {
			processInstances.add(new JsonProcessCard(pi));
		}

		return JsonResponse.success(new HashMap<String, Object>() {
			{
				put("results", response.totalSize());
				put("rows", processInstances);
			}
		});

	}

	@JSONExported
	public JsonResponse getStartActivity( //
			@Parameter("classId") final Long processClassId) throws CMWorkflowException {
		final CMActivity activityDefinition = workflowLogic().getStartActivity(processClassId);

		return JsonResponse.success(new JsonActivityDefinition( //
				activityDefinition, //
				performerFor(activityDefinition)));
	}

	private String performerFor(final CMActivity activityDefinition) {
		final String performerName;
		final ActivityPerformer performer = activityDefinition.getFirstNonAdminPerformer();
		switch (performer.getType()) {
		case ROLE: {
			performerName = performer.getValue();
			break;
		}
		case EXPRESSION: {
			final String maybe = operationUser().getPreferredGroup().getName();
			final String expression = performer.getValue();
			final ActivityPerformerExpressionEvaluator evaluator = new BshActivityPerformerExpressionEvaluator(
					expression);
			final Set<String> names = evaluator.getNames();
			performerName = names.contains(maybe) ? maybe : StringUtils.EMPTY;
			break;
		}
		default: {
			performerName = StringUtils.EMPTY;
			break;
		}
		}
		return performerName;
	}

	@JSONExported
	public JsonResponse getActivityInstance( //
			@Parameter("classId") final Long processClassId, //
			@Parameter("cardId") final Long processInstanceId, //
			@Parameter("activityInstanceId") final String activityInstanceId //
	) throws CMWorkflowException {
		final UserActivityInstance activityInstance = workflowLogic().getActivityInstance( //
				processClassId, processInstanceId, activityInstanceId);

		return JsonResponse.success(new JsonActivityInstance(activityInstance));
	}

	@JSONExported
	@SuppressWarnings("serial")
	public JsonResponse saveActivity( //
			@Parameter("classId") final Long processClassId, //
			// processCardId even with Long, it won't be null
			@Parameter(value = "cardId", required = false) final long processCardId, //
			@Parameter(value = "activityInstanceId", required = false) final String activityInstanceId, //
			@Parameter("attributes") final String jsonVars, //
			@Parameter("advance") final boolean advance, //
			@Parameter("ww") final String jsonWidgetSubmission //
	) throws CMWorkflowException, Exception {
		final WorkflowLogic logic = workflowLogic();
		final CMProcessInstance procInst;
		@SuppressWarnings("unchecked")
		final Map<String, Object> vars = new ObjectMapper().readValue(jsonVars, Map.class);
		@SuppressWarnings("unchecked")
		final Map<String, Object> widgetSubmission = new ObjectMapper().readValue(jsonWidgetSubmission, Map.class);

		if (processCardId > 0) { // should check for null
			procInst = logic.updateProcess(processClassId, processCardId, activityInstanceId, vars, widgetSubmission,
					advance);
		} else {
			procInst = logic.startProcess(processClassId, vars, widgetSubmission, advance);
		}

		return JsonResponse.success(new HashMap<String, Object>() {
			{
				put("Id", procInst.getCardId());
				put("IdClass", procInst.getType().getId());
				put("ProcessInstanceId", procInst.getProcessInstanceId());
				// WorkItemId -> WHY?!?!?!?!?!?!
			}
		});
	}

	@JSONExported
	public JsonResponse abortprocess( //
			@Parameter("classId") final Long processClassId, //
			@Parameter("cardId") final long processCardId //
	) throws CMWorkflowException {
		workflowLogic().abortProcess(processClassId, processCardId);

		return JsonResponse.success(null);
	}

	@Admin
	@JSONExported
	public DataHandler downloadXpdlTemplate( //
			@Parameter("idClass") final Long processClassId //
	) throws CMWorkflowException {
		final DataSource ds = workflowLogic().getProcessDefinitionTemplate(processClassId);

		return new DataHandler(ds);
	}

	@Admin
	@JSONExported
	public JsonResponse xpdlVersions( //
			@Parameter(value = "idClass", required = true) final Long processClassId //
	) throws CMWorkflowException {
		final String[] versions = workflowLogic().getProcessDefinitionVersions(processClassId);

		return JsonResponse.success(versions);
	}

	@Admin
	@JSONExported
	public DataHandler downloadXpdl( //
			@Parameter("idClass") final Long processClassId, //
			@Parameter("version") final String version //
	) throws CMWorkflowException {
		final DataSource ds = workflowLogic().getProcessDefinition(processClassId, version);

		return new DataHandler(ds);
	}

	@Admin
	@JSONExported
	public JsonResponse uploadXpdl( //
			@Parameter("idClass") final Long processClassId, //
			@Parameter(value = "xpdl", required = false) final FileItem xpdlFile, //
			@Parameter(value = "sketch", required = false) final FileItem sketchFile //
	) throws CMWorkflowException, IOException {
		final List<String> messages = Lists.newArrayList();
		final WorkflowLogic logic = workflowLogic();
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
			@Override
			public String getContentType() {
				return xpdlFile.getContentType();
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return xpdlFile.getInputStream();
			}

			@Override
			public String getName() {
				return xpdlFile.getName();
			}

			@Override
			public OutputStream getOutputStream() throws IOException {
				return xpdlFile.getOutputStream();
			}
		};
	}

	@Admin
	@JSONExported
	public void sync() throws CMWorkflowException {
		workflowLogic().sync();
	}

}

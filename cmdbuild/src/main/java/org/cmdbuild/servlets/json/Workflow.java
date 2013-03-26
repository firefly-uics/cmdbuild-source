package org.cmdbuild.servlets.json;

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
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ProcessQuery;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.services.auth.UserContext;
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

public class Workflow extends JSONBase {

	private WorkflowLogic workflowLogic() {
		return TemporaryObjectsBeforeSpringDI.getWorkflowLogic();
	}

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
			final JSONObject serializer, //
			@Parameter("state") final String flowStatus, //
			@Parameter("limit") final int limit, //
			@Parameter("start") final int offset, //
			@Parameter(value = "sort", required = false) final JSONArray sorters, //
			@Parameter(value = "query", required = false) final String fullTextQuery, //

			/*
			 * Don't clone it or getCardPosition does not work, unless sort and
			 * query are set somewhere else already filtered with the passed
			 * flow status
			 */
			final ProcessQuery processQuery //
	) throws JSONException, CMWorkflowException {
		configureQuery(processQuery, fullTextQuery, sorters, limit, offset);

		final List<JsonProcessCard> processInstances = Lists.newArrayList();
		for (final UserProcessInstance pi : workflowLogic().query(processQuery)) {
			processInstances.add(new JsonProcessCard(pi));
		}

		return JsonResponse.success(new HashMap<String, Object>() {
			{
				put("results", processQuery.getTotalRows());
				put("rows", processInstances);
			}
		});

	}

	private void configureQuery(final ProcessQuery processQuery, final String fullTextQuery, final JSONArray sorters,
			final int limit, final int offset) throws JSONException {
		if (fullTextQuery != null) {
			processQuery.fullText(fullTextQuery.trim());
		}
		applySortToCardQuery(sorters, processQuery);
		processQuery.subset(offset, limit).count();
	}

	public static void applySortToCardQuery(final JSONArray sorters, final CardQuery cardQuery) throws JSONException {
		if (sorters != null && sorters.length() > 0) {
			final JSONObject s = sorters.getJSONObject(0);
			String sortField = s.getString("property");
			final String sortDirection = s.getString("direction");

			if (sortField != null || sortDirection != null) {
				if (sortField.endsWith("_value")) {
					sortField = sortField.substring(0, sortField.length() - 6);
				}

				cardQuery.clearOrder().order(sortField, OrderFilterType.valueOf(sortDirection));
			}
		}
	}

	@JSONExported
	public JsonResponse getStartActivity( //
			@Parameter("classId") final Long processClassId, //
			final UserContext userCtx //
	) throws CMWorkflowException {
		final CMActivity activityDefinition = workflowLogic().getStartActivity(processClassId);

		return JsonResponse.success(new JsonActivityDefinition( //
				activityDefinition, //
				performerFor(activityDefinition, userCtx)));
	}

	private String performerFor(final CMActivity activityDefinition, final UserContext userCtx) {
		final String performerName;
		final ActivityPerformer performer = activityDefinition.getFirstNonAdminPerformer();
		switch (performer.getType()) {
		case ROLE: {
			performerName = performer.getValue();
			break;
		}
		case EXPRESSION: {
			final String maybe = userCtx.getDefaultGroup().getName();
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

package org.cmdbuild.servlets.json.management;

import static org.cmdbuild.servlets.json.management.ModCard.applySortToCardQuery;

import java.util.Map;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.ProcessQuery;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.WorkflowVariableType;
import org.cmdbuild.workflow.operation.SharkFacade;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.enhydra.shark.api.common.SharkConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModWorkflow extends JSONBase {

	/**
	 * Get the workItems OR closed processes, depending on the state required.
	 * if required state is closed, then processes with state closed.*
	 * (completed/terminated/aborted) will be returned, if state is open, the
	 * activities in open.not_running.not_started and open.running will be
	 * returned
	 * 
	 * @param params
	 * @return
	 * @throws JSONException
	 * @throws CMWorkflowException
	 */
	@JSONExported
	public JSONObject getActivityList(JSONObject serializer, UserContext userCtx,
			@Parameter("state") String flowStatus, @Parameter("limit") int limit, @Parameter("start") int offset,
			@Parameter(value = "sort", required = false) JSONArray sorters,
			@Parameter(value = "query", required = false) String fullTextQuery,
			// Don't clone it or getCardPosition does not work, unless sort and
			// query are set somewhere else
			ProcessQuery processQuery // already filtered with the passed flow
										// status
	) throws JSONException, CMWorkflowException {
		final WorkflowLogic logic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);

		setFullTextQuery(fullTextQuery, processQuery);
		applySortToCardQuery(sorters, processQuery);

		for (CMProcessInstance procInst : logic.query(processQuery)) {
			JSONObject jsonCard = new JSONObject();// Serializer.serializeCard(card,
													// false);
			boolean writePriv;
			if (procInst.getState() == WSProcessInstanceState.OPEN) {
				jsonCard.put("stoppable", procInst.getType().isUserStoppable()); // IT
																					// SHOULD
																					// BE
																					// AT
																					// THE
																					// CLASS
																					// LEVEL!
				jsonCard.put("ProcessInstanceId", procInst.getProcessInstanceId());
				if (!procInst.getActivities().isEmpty()) {
					addActivityInstanceProperties(jsonCard, procInst.getActivities().get(0));
				}
				writePriv = true; // FIXME! It should put in the single activity
									// is the user is assigned that activity
			} else {
				writePriv = false;
			}
			jsonCard.put("priv_write", writePriv);
			jsonCard.put(ProcessAttributes.FlowStatus.dbColumnName() + "_code",
					convertToSharkStatusCode(procInst.getState()));
			serializer.append("rows", jsonCard);
		}
		serializer.put("results", processQuery.getTotalRows());

		return serializer;
	}

	@Legacy("It's stupid to use Shark's internal code! Use the value for the enum instead!")
	private String convertToSharkStatusCode(final WSProcessInstanceState state) {
		switch (state) {
		case OPEN:
			return SharkConstants.STATE_OPEN_RUNNING;
		case SUSPENDED:
			return SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED;
		case COMPLETED:
			return SharkConstants.STATE_CLOSED_COMPLETED;
		case TERMINATED:
			return SharkConstants.STATE_CLOSED_TERMINATED;
		case ABORTED:
			return SharkConstants.STATE_CLOSED_ABORTED;
		default:
			return null;
		}
	}

	@Legacy("Old serialization")
	private void addActivityInstanceProperties(final JSONObject jsonCard, final CMActivityInstance actInst) throws JSONException {
		CMActivity actDef;
		try {
			actDef = actInst.getDefinition();
		} catch (CMWorkflowException e) {
			return; // Skip activity on problems
		}
		jsonCard.put("ActivityDescription", actDef.getInstructions());
		jsonCard.put("WorkItemId", actInst.getId()); // FIXME NOT REALLY THE WORK ITEM ID AND ANYWAY NEVER USED
		jsonCard.put(ICard.CardAttributes.Code.dbColumnName(), actDef.getDescription());
		// jsonCard.put("CmdbuildExtendedAttributes", null); // DO IT FOR THE REAL SERIALIZATION
		jsonCard.put("activityPerformerName", actInst.getPerformerName());
		int index = 0;
		for (final CMActivityVariableToProcess var : actDef.getVariables()) {
			jsonCard.put(var.getName() + "_type", convertVariableType(var.getType()));
			jsonCard.put(var.getName() + "_index", index);
			++index;
		}
	}

	@Legacy("Old serialization")
	private String convertVariableType(final CMActivityVariableToProcess.Type type) {
		switch (type) {
		case READ_ONLY:
			return WorkflowVariableType.VIEW.name();
		case READ_WRITE:
			return WorkflowVariableType.UPDATE.name();
		case READ_WRITE_REQUIRED:
			return WorkflowVariableType.REQUIRED.name();
		default:
			return null;
		}
	}

	private void setFullTextQuery(String fullTextQuery, ProcessQuery cardFilter) {
		if (fullTextQuery != null)
			cardFilter.fullText(fullTextQuery.trim());
	}

	/**
	 * Abort the process which holds the activity
	 * 
	 * @param params
	 * @return
	 * @throws JSONException
	 */
	@JSONExported
	public JSONObject abortProcess(JSONObject serializer, SharkFacade mngt, ActivityIdentifier ai,
			Map<String, String> params) throws JSONException {
		serializer.put("success", mngt.abortProcess(ai.processInstanceId, ai.workItemId));
		return serializer;
	}

}

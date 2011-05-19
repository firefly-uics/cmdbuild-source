package org.cmdbuild.servlets.json.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.ProcessQuery;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.WorkflowService;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.OverrideKeys;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.ActivityVariable;
import org.cmdbuild.workflow.WorkflowConstants;
import org.cmdbuild.workflow.extattr.CmdbuildExtendedAttribute;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.cmdbuild.workflow.operation.SharkFacade;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModWorkflow extends JSONBase {
	
	/**
	 * Get the workItems OR closed processes, depending on the state required.
	 * if required state is closed, then processes with state closed.* (completed/terminated/aborted) will be returned,
	 * if state is open, the activities in open.not_running.not_started and open.running will be returned
	 * @param params
	 * @return
	 * @throws JSONException
	 */
	@JSONExported
	public JSONObject getActivityList(
			JSONObject serializer,
			SharkFacade mngt,
			UserContext userCtx,
			@OverrideKeys(key="idClass",newKey="IdClass") ITable classTable,
			@Parameter("state")String flowStatus,
			@Parameter("limit")int limit,
			@Parameter("start")int offset,
			@Parameter(value="sort",required=false) String sortField,
			@Parameter(value="dir",required=false) String sortDirection,
			@Parameter(value="query",required=false) String fullTextQuery,
			ProcessQuery processFilter) throws  JSONException {

		setFullTextQuery(fullTextQuery, processFilter);		
		setSorting(sortField, sortDirection, processFilter);
		setFilterByFlowStatus(processFilter, flowStatus);
		processFilter.setNextExecutorFilter(userCtx);

		final List<ICard> cards = getWFCards(limit, offset, processFilter);
		final Map<Integer, ActivityDO> activityMap = mngt.getActivityMap(classTable, cards, flowStatus);

		final JSONArray rows = serializeCards(mngt, userCtx, classTable, cards, activityMap);		
		serializer.put("results", processFilter.getTotalRows());
		serializer.put("rows", rows);
		return serializer;
	}

	private JSONArray serializeCards(SharkFacade mngt, UserContext userCtx,
			ITable classTable, List<ICard> cards, Map<Integer, ActivityDO> activityMap) throws JSONException {

		final Lookup openedStatus = WorkflowService.getInstance().getStatusLookupFor(WorkflowConstants.StateOpenRunning);

		final JSONArray rows = new JSONArray();
		for (ICard card : cards) {
			JSONObject jsonCard = Serializer.serializeCard(card, false);
			if (card.getAttributeValue(ProcessAttributes.FlowStatus.toString()).toString().equals(openedStatus.getDescription())
					&& activityMap.keySet().contains(card.getId())) {
				ActivityDO activity = activityMap.get(card.getId());
				addActivityInfo(jsonCard, activity, userCtx);
			}
			rows.put(jsonCard);
		}
		return rows;
	}
	
	private void addActivityInfo(JSONObject jsonCard, ActivityDO activity, UserContext userCtx) throws JSONException {
		jsonCard.put("ProcessInstanceId", activity.getProcessInstanceId());
		jsonCard.put("WorkItemId", activity.getWorkItemId());
		jsonCard.put("ActivityDescription", activity.getActivityInfo().getActivityDescription());
		jsonCard.put(ICard.CardAttributes.Code.toString(), activity.getActivityInfo().getActivityName());

		JSONArray extAttrArray = new JSONArray();
		for(CmdbuildExtendedAttribute extattr : activity.getCmdbExtAttrs()) {
			try {
				JSONObject extAttrObj = extattr.serializeJson(activity);
				Log.WORKFLOW.debug("put ext.attr. " + extattr.extendedAttributeName() + " at pos: " + extattr.index());
				extAttrArray.put(extattr.index(), extAttrObj);
			} catch(Exception e) {
				Log.WORKFLOW.error("Cannot serialize extended attribute: ", e);
			}
		}
		
		jsonCard.put("CmdbuildExtendedAttributes", extAttrArray);
		jsonCard.put("activityPerformerName", activity.getPerformer());
		jsonCard.put("editableByCurrentUser", activity.isEditable());

		boolean stoppable = (userCtx.privileges().isAdmin() || activity.isUserStoppable());
		jsonCard.put("stoppable", stoppable);

		if(activity.getActivityInfo().isQuickAcceptActivity()){
			jsonCard.put("QuickAccept", activity.getActivityInfo().getQuickAcceptVariable());
		}

		// add further info for the activity form
		for (ActivityVariable var : activity.getVariables()) {
			String name = var.getName();
			jsonCard.put(name + "_type", var.getType().name());
			jsonCard.put(name + "_index", var.getClientIndex());
		}
	}

	protected JSONObject serializeActivity(ActivityDO activity, UserContext userCtx, ITable classTable) throws JSONException {
		JSONObject serializer = new JSONObject();

		int cardId = activity.getCmdbuildCardId();
		Log.WORKFLOW.debug("serialize activityDO cardId: " + cardId);
		for (ActivityVariable var : activity.getVariables()) {
			String name = var.getName();
			serializer.put(name, var.getStringValue());
			Integer id = var.getId();
			if (id != null) {
				serializer.put(name, id);
				serializer.put(name + "_value", var.getStringValue());
			} else {
				serializer.put(name, var.getStringValue());
			}
		}
		serializer.put("Code", activity.getActivityInfo().getActivityName());
		serializer.put("ActivityDescription", activity.getActivityInfo().getActivityDescription());
		serializer.put("Id", cardId );
		serializer.put("IdClass", activity.getCmdbuildClassId());
		serializer.put("Notes", activity.getCmdbuildCardNotes());

		addActivityInfo(serializer, activity, userCtx);
		addAccessPrivileges(serializer, classTable);

		return serializer;
	}

	private void setFullTextQuery(String fullTextQuery,
			ProcessQuery cardFilter) {
		if (fullTextQuery != null)
		    cardFilter.fullText(fullTextQuery.trim());
	}

	private void setFilterByFlowStatus(ProcessQuery cardFilter, String flowStatus) {
		Lookup flowStatusLookup = WorkflowService.getInstance().getStatusLookupFor(flowStatus);
		if (flowStatusLookup != null) {
			Log.WORKFLOW.debug("Workflow status: " + flowStatusLookup.getDescription());
			cardFilter.filterUpdate(ProcessAttributes.FlowStatus.toString(), AttributeFilterType.EQUALS, flowStatusLookup.getId() + "");
		} else {
			//need to select all the cards, and "clear" the filtering by FlowStatus
			cardFilter.filterUpdate(ProcessAttributes.FlowStatus.toString(), AttributeFilterType.DIFFERENT,
				String.valueOf(WorkflowService.getInstance().getStatusLookupFor(WorkflowConstants.StateClosedTerminated).getId()),
				String.valueOf(WorkflowService.getInstance().getStatusLookupFor(WorkflowConstants.StateClosedAborted).getId())
			);
		}
	}

	private void setSorting(String sortField, String sortDirection,
			ProcessQuery cardFilter) {
		if (sortField != null && sortDirection != null) {
			if (sortField.endsWith("_value"))
				sortField = sortField.substring(0, sortField.length()-6);
			cardFilter.clearOrder().order(sortField, OrderFilterType.valueOf(sortDirection));
		}
	}

	private List<ICard> getWFCards(int limit, int offset, ProcessQuery cardFilter) {
		List<ICard> cards = new ArrayList<ICard>();
		for(ICard card : cardFilter.subset(offset, limit).count()) {
			cards.add(card);
		}
		return cards;
	}

	@JSONExported
	public JSONObject getStartActivityTemplate(
			JSONObject serializer,
			SharkFacade mngt,
			UserContext userCtx,
			ProcessType processType) throws JSONException, CMDBException {
		ActivityDO template = processType.startActivityTemplate();
		if (template != null) {
			serializer.put("data", serializeActivity(template, userCtx, processType));
		}
		return serializer;
	}

	@JSONExported
	public JSONObject startProcess(
			JSONObject serializer,
			SharkFacade mngt,
			UserContext userCtx,
			ITable classTable) throws JSONException, CMDBException {
		String className = classTable.getName();
		
		ActivityDO activity = mngt.startProcess(className);
		activity.setCmdbuildClassId(classTable.getId());
		serializer.put("data", serializeActivity(activity,userCtx,classTable));
		return serializer;
	}

	/**
	 * Open an activity (if not already opened) and put the new attribute values (new state will be open.running)
	 * @param params
	 * @return
	 * @throws JSONException
	 */
	@JSONExported 
	public JSONObject updateActivity(
			JSONObject serializer,
			SharkFacade mngt,
			ActivityIdentifier ai,
			@Parameter("advance") boolean advance,
			Map<String, String> params) throws JSONException {
		serializer.put("success", mngt.updateActivity(ai.processInstanceId,ai.workItemId,params,advance));
		return serializer;
	}

	/**
	 * Abort the process which holds the activity
	 * @param params
	 * @return
	 * @throws JSONException
	 */
	@JSONExported
	public JSONObject abortProcess(
			JSONObject serializer,
			SharkFacade mngt,
			ActivityIdentifier ai,
			Map<String, String> params) throws JSONException {
		serializer.put("success", mngt.abortProcess(ai.processInstanceId,ai.workItemId));		
		return serializer;
	}
	
	/**
	 * react to the submission of a custom cmdbuild extended attribute
	 * @param serializer
	 * @param identifier
	 * @param ai
	 * @param mngt
	 * @param params
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	@JSONExported
	public JSONObject reactExtendedAttribute(
			JSONObject serializer,
			@Parameter("identifier") String identifier,
			ActivityIdentifier ai,
			SharkFacade mngt,
			HttpServletRequest request
			) throws JSONException {
		Log.WORKFLOW.debug("reacting: " + identifier + ", " + ai.workItemId);
		serializer.put("success", mngt.reactToExtendedAttributeSubmission(ai.processInstanceId, ai.workItemId, request.getParameterMap(), identifier));
		return serializer;
	}
	
	private void addAccessPrivileges(JSONObject serializer, BaseSchema schema) throws JSONException {
		Object privileges = schema.getMetadata().get(MetadataService.RUNTIME_PRIVILEGES_KEY);
		if (privileges != null) {
			boolean writePriv = PrivilegeType.WRITE.equals(privileges);
			serializer.put("priv_write", writePriv);
			boolean createPriv = writePriv;
			if (schema instanceof ITable) {
				createPriv &= !((ITable) schema).isSuperClass();
			}
			serializer.put("priv_create", createPriv);
		}
	}

}

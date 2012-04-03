package org.cmdbuild.servlets.json.management;

import static org.cmdbuild.servlets.json.management.ModCard.applySortToCardQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.ProcessQuery;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.proxy.LazyCard;
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
			@Parameter(value = "sort", required = false) JSONArray sorters,
			@Parameter(value="query",required=false) String fullTextQuery,
			// Don't clone it or getCardPosition does not work, unless sort and query are set somewhere else
			ProcessQuery processQuery // already filtered with the passed flow status
		) throws  JSONException {

		setFullTextQuery(fullTextQuery, processQuery);
		applySortToCardQuery(sorters, processQuery);

		final List<ICard> cards = getWFCards(limit, offset, processQuery);
		final Map<Integer, ActivityDO> activityMap = mngt.getActivityMap(classTable, cards, flowStatus);

		final JSONArray rows = serializeCards(mngt, userCtx, classTable, cards, activityMap);		
		serializer.put("results", processQuery.getTotalRows());
		serializer.put("rows", rows);
		return serializer;
	}

	private JSONArray serializeCards(SharkFacade mngt, UserContext userCtx,
			ITable classTable, List<ICard> cards, Map<Integer, ActivityDO> activityMap) throws JSONException {

		final Lookup openedStatus = WorkflowService.getInstance().getStatusLookupFor(WorkflowConstants.StateOpenRunning);

		final JSONArray rows = new JSONArray();
		for (ICard card : cards) {
			JSONObject jsonCard = Serializer.serializeCard(card, false);
			boolean writePriv;
			final Lookup flowStatus = card.getAttributeValue(ProcessAttributes.FlowStatus.toString()).getLookup();

			if (openedStatus.equals(flowStatus) && activityMap.keySet().contains(card.getId())) {
				boolean stoppable = card.getSchema().isUserStoppable() || userCtx.privileges().isAdmin();
				jsonCard.put("stoppable", stoppable);

				ActivityDO activity = activityMap.get(card.getId());
				addActivityInfo(jsonCard, activity, userCtx);
				writePriv = activity.isEditable();
			} else {
				// Closed or inconsistent activity
				writePriv = false;
			}

			jsonCard.put(ProcessAttributes.FlowStatus.toString() + "_code", flowStatus.getCode());
			jsonCard.put("priv_write", writePriv);
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
	public JSONObject saveActivity(
			ActivityIdentifier ai,
			ICard processCard,
			@Parameter("attributes") JSONObject attributes,
			@Parameter("ww") JSONObject ww,
			@Parameter("advance") boolean advance,
			UserContext userCtx, SharkFacade sharkFacade) throws JSONException, PartialFailureException {
		processCard = startProcessIfNotStarted(sharkFacade, ai, processCard);
		final JSONObject out = Serializer.serializeActivityIds(ai, processCard);
		try {
			saveWorkflowWidgets(sharkFacade, ai, ww, advance);
			updateActivityAttributes(sharkFacade, ai, attributes, advance);
		} catch (Exception e) {
			throw new PartialFailureException(out, e);
		}
		return out;
	}

	private ICard startProcessIfNotStarted(final SharkFacade sharkFacade, ActivityIdentifier ai, ICard processCard) {
		if (ai.processInstanceId == null || ai.processInstanceId.isEmpty()) {
			final ITable processTable = processCard.getSchema();
			final ActivityDO ado = sharkFacade.startProcess(processTable.getName());
			ai.processInstanceId = ado.getProcessInstanceId();
			ai.workItemId = ado.getWorkItemId();
			processCard = new LazyCard(processTable.getId(), ado.getCmdbuildCardId());
		}
		return processCard;
	}

	private void saveWorkflowWidgets(SharkFacade sharkFacade, ActivityIdentifier ai, JSONObject ww, boolean advance) throws JSONException, MultipleException {
		MultipleException me = null;
		if (ww.length() == 0) {
			// JSONObject.getNames(ww) returns null if the object is empty
			return;
		}
		for (String identifier : JSONObject.getNames(ww)) {
			final Map<String, String[]> values = jsonObjectToStringArrayMap(ww.getJSONObject(identifier));
			try {
				sharkFacade.saveWorkflowWidget(ai, identifier, values, advance);
			} catch (Exception e) {
				if (me == null) {
					me = new MultipleException(e);
				} else {
					me.addException(e);
				}
			}
		}
		if (me != null) {
			throw me;
		}
	}

	private Map<String, String[]> jsonObjectToStringArrayMap(JSONObject jsonObject) throws JSONException {
		final Map<String, String[]> valueMap = new HashMap<String, String[]>();
		for (String key : JSONObject.getNames(jsonObject)) {
			JSONArray a = jsonObject.optJSONArray(key);
			if (a == null) {
				final String s = jsonObject.optString(key);
				if (s != null) {
					a = new JSONArray();
					a.put(s);
				}
			}
			if (a != null && a.length() > 0) {
				final String[] value = new String[a.length()];
				for (int i = 0; i < a.length(); ++i) {
					value[i] = a.getString(i);
				}
				valueMap.put(key, value);
			}
		}
		return valueMap;
	}

	private boolean updateActivityAttributes(SharkFacade sharkFacade, ActivityIdentifier ai, JSONObject attributes, boolean advance) throws MultipleException {
		final Map<String, String> values = jsonObjectToStringMap(attributes);
		return sharkFacade.updateActivity(ai, values, advance);
	}

	private Map<String, String> jsonObjectToStringMap(JSONObject jsonObject) {
		final Map<String, String> valueMap = new HashMap<String, String>();
		if (jsonObject != null && jsonObject.length() > 0) {
			// Awesome library, Douglas!
			for (String key : JSONObject.getNames(jsonObject)) {
				String value = jsonObject.optString(key);
				if (value != null) {
					valueMap.put(key, value);
				}
			}
		}
		return valueMap;
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

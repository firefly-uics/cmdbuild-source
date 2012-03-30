package org.cmdbuild.workflow.extattr;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.elements.wrappers.EmailCard;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.workflow.SharkWSFacade;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ManageEmail extends AbstractCmdbuildExtendedAttribute {

	static final String READONLY_PARAM = "ReadOnly";
	static final String DISABLED_PARAM = "Disabled";
	static final String REACT_UPDATED = "Updated";
	static final String REACT_DELETED = "Deleted";
	static final String REACT_SEND = "ImmediateSend";

	public String extendedAttributeName() {
		return "manageEmail";
	}

	@Override
	protected void addCustomParams(ActivityDO activityDO, JSONObject object, ExtendedAttributeConfigParams eacp) throws Exception {
		object.put(DISABLED_PARAM, !EmailService.isConfigured());
	}

	@Override
	protected void doReact(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, SharkWSFacade facade, WMWorkItem workItem,
			ActivityDO activityDO, Map<String, String[]> submissionParameters,
			ExtendedAttributeConfigParams oldConfig,
			Map<String, Object> outputParameters, boolean advance) {
		if (isReadOnly(oldConfig.getParameters())) {
			return;
		} else {
			ICard processCard = getProcessCard(handle, userCtx, factory, workItem, activityDO);
			Log.WORKFLOW.info(String.format("ManageEmail doReact for classid %d cardid %d", processCard.getIdClass(), processCard.getId()));
			updateEmails(submissionParameters, processCard);
			if (advance) {
				EmailCard.sendOutgoingAndDrafts(processCard);
			}
		}
	}

	private boolean isReadOnly(Map<String, Object> eaParams) {
		Object readOnly = eaParams.get(READONLY_PARAM);
		if (readOnly instanceof Boolean) {
			return (Boolean) readOnly;
		} else if (readOnly instanceof Integer) {
			return (((Integer) readOnly) > 0);
		} else if (readOnly instanceof String) {
			return (((String) readOnly).length() > 0);
		}
		return false;
	}

	private void updateEmails(Map<String, String[]> submissionParameters, ICard processCard) {
		try {
			deleteEmails(submissionParameters, processCard);
			createUpdateEmails(submissionParameters, processCard);
		} catch (JSONException e) {
			throw WorkflowExceptionType.WF_GENERIC_ERROR.createException();
		}
	}

	private void deleteEmails(Map<String, String[]> submissionParameters, ICard processCard) throws JSONException {
		JSONArray deletedJson = new JSONArray(firstStringOrNull(submissionParameters.get(REACT_DELETED)));
		for (int i=0, n=deletedJson.length(); i<n; ++i) {
			int emailCardId = deletedJson.getInt(i);
			EmailCard email = EmailCard.get(processCard, emailCardId);
			email.delete();
		}
	}

	private void createUpdateEmails(Map<String, String[]> submissionParameters, ICard processCard) throws JSONException {
		JSONArray outgoingEmailsJson = new JSONArray(firstStringOrNull(submissionParameters.get(REACT_UPDATED)));
		for (int i=0, n=outgoingEmailsJson.length(); i<n; ++i) {
			JSONObject outgoingJson = outgoingEmailsJson.getJSONObject(i);
			Map<String, String> emailValues = jsonToValueMap(outgoingJson);
			EmailService.createOrUpdateProcessEmail(processCard, emailValues);
		}
	}

	private Map<String, String> jsonToValueMap(JSONObject jsonObject) throws JSONException {
		Map<String, String> valueMap = new HashMap<String, String>();
		String attributeNames[] = JSONObject.getNames(jsonObject);
		for (int i=0, n=attributeNames.length; i<n; ++i) {
			String attributeName = attributeNames[i];
			valueMap.put(attributeName, jsonObject.getString(attributeName));
		}
		return valueMap;
	}

	private ICard getProcessCard(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, WMWorkItem workItem, ActivityDO activityDO) {
		String className;
		try {
			className = (String) factory.getWAPIConnection().getProcessInstanceAttributeValue(handle, workItem.getProcessInstanceId(), "ProcessClass").getValue();
		} catch (Exception e) {
			throw WorkflowExceptionType.WF_GENERIC_ERROR.createException();
		}
		ITable processTable = userCtx.tables().get(className);
		int processId = activityDO.getCmdbuildCardId();
		ICard processCard = new LazyCard(processTable, processId);
		return processCard;
	}

}

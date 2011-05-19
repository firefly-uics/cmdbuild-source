package org.cmdbuild.services.soap.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.WorkflowWidgetDefinition;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.soap.structure.ActivitySchema;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.WorkflowWidgetSubmission;
import org.cmdbuild.services.soap.structure.WorkflowWidgetSubmissionParameter;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.Card;
import org.cmdbuild.services.soap.types.Workflow;
import org.cmdbuild.services.soap.utils.WorkflowUtils;
import org.cmdbuild.workflow.extattr.CmdbuildExtendedAttribute;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.cmdbuild.workflow.operation.SharkFacade;
import org.cmdbuild.workflow.operation.WorkItemQuery;

public class PrivateWorkflow {
	
	private UserContext userCtx;

	public PrivateWorkflow(UserContext userCtx) {
		this.userCtx = userCtx;
	}
	
	public ActivitySchema getActivityObjects(String className, Integer cardid) {
		List<AttributeSchema> attributes = getProcessAttributeSchema(className, cardid, userCtx);
		List<WorkflowWidgetDefinition> widgets = getWorkflowWidgets(className, cardid, userCtx);
		return serializeActivitySchema(attributes, widgets);
	}
		
	public Workflow updateWorkflow(Card card, boolean completeTask, WorkflowWidgetSubmission[] submissionWidgets) {
		SharkFacade management = new SharkFacade(userCtx);
		ActivityDO activity;
		if (card.getId() > 0) {
			activity = getActivityForCard(card, management);
		} else {
			activity = startWorkflow(card, management);
		}
		applyWorkflowWidget(management, activity, submissionWidgets);
		updateActivity(card, management, activity, completeTask);
		Workflow workflow = new Workflow();
		workflow.setProcessid(activity.getCmdbuildCardId());
		workflow.setProcessinstanceid(activity.getProcessInstanceId());
		return workflow;
	}
	

	private void applyWorkflowWidget(SharkFacade management, ActivityDO activity, WorkflowWidgetSubmission[] submissionWidgets) {
		if (submissionWidgets == null) {
			return;
		}
		boolean allWWSucceded = true;
		for (WorkflowWidgetSubmission widget : submissionWidgets) {
			boolean currentSucceeded = executeWorkflowWidget(management, activity, widget);
			if (!currentSucceeded) {
				Log.SOAP.error("Workflow widget "+widget.getIdentifier()+" failed");
				throw WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR.createException(widget.getIdentifier());
			}
			if (!allWWSucceded) {
				Log.SOAP.error("One or more workflow widgets failed");
			}
		}
	}

	private boolean executeWorkflowWidget(SharkFacade management, ActivityDO activity, WorkflowWidgetSubmission widgets) {
		Map<String, String[]> submissionParameters = new HashMap<String, String[]>();
		for (WorkflowWidgetSubmissionParameter parameter : widgets.getParameters()){
			submissionParameters.put(parameter.getKey(), parameter.getValues());
		}
		return management.reactToExtendedAttributeSubmission(activity.getProcessInstanceId(), activity.getWorkItemId(), 
				submissionParameters, widgets.getIdentifier());
	}

	private ActivityDO startWorkflow(Card card, SharkFacade management) {
		Log.SOAP.debug("Starting workflow for class " + card.getClassName());

		ActivityDO activity = management.startProcess(card.getClassName());

		String processid = String.valueOf(activity.getCmdbuildCardId());
		Log.SOAP.debug("Process id is " + processid);
		return activity;
	}

	private void updateActivity(Card card, SharkFacade management, ActivityDO activity,
			boolean completeTask) {
		String workitemid = activity.getWorkItemId();
		Log.SOAP.debug("Workflow work item id " + workitemid);
		Map<String, String> params = new HashMap<String, String>();
		List<Attribute> attributes = card.getAttributeList();
		if (attributes != null) {
			for (Attribute attribute : attributes) {
				String name = attribute.getName();
				String value = attribute.getValue();
				params.put(name, value);
			}
		}
		String processinstanceid = activity.getProcessInstanceId();
		Log.SOAP.debug("Process instance id is " + processinstanceid);
		management.generalUpdateActivity(processinstanceid, workitemid, params, completeTask);
	}
	
	private ActivityDO getActivityForCard(Card card, SharkFacade management) {
		WorkflowUtils utils = new WorkflowUtils(userCtx);

		String processinstanceid = utils.getProcessInstanceId(card); 
		Log.SOAP.debug("Updating workflow " + processinstanceid);
		WorkItemQuery query = new WorkItemQuery();
		query.setProcessInstanceId(processinstanceid);
		Log.SOAP.debug("Searching activity query: " + query);
		List<ActivityDO> activities = management.getWorkItems(query, false);
		ActivityDO activity = activities.iterator().next();
		return activity;
	}

	private ActivitySchema serializeActivitySchema(List<AttributeSchema> attributes, List<WorkflowWidgetDefinition> widgets) {
		ActivitySchema activitySchema = new ActivitySchema();
		activitySchema.setAttributes(attributes);
		activitySchema.setWidgets(widgets);
		return activitySchema;
	}

	private List<WorkflowWidgetDefinition> getWorkflowWidgets(String classname, Integer cardid, UserContext userCtx) {
		SharkFacade management = new SharkFacade(userCtx);
		ActivityDO activity; 
		if (cardid != null && cardid > 0){
			ICard card = userCtx.tables().get(classname).cards().get(cardid);
			activity = management.getActivityList(card);
		} else {
			activity = management.startActivityTemplate(classname);
		}
		List<WorkflowWidgetDefinition> widgetDefinitionList = new ArrayList<WorkflowWidgetDefinition>();
		for (CmdbuildExtendedAttribute extendedAttribute : activity.getCmdbExtAttrs()){
			WorkflowWidgetDefinition wwd = extendedAttribute.serialize(activity);
			widgetDefinitionList.add(wwd);
		}
		return widgetDefinitionList;
	}

	private List<AttributeSchema> getProcessAttributeSchema(String className, Integer cardid, UserContext userCtx) {
		EWorkflow wf = new EWorkflow(userCtx);
		if (cardid != null && cardid.intValue() > 0) {
			return wf.getActivity(className, cardid, userCtx);
		} else {
			return wf.getStartActivityTemplate(className, userCtx);
		}
	}
}

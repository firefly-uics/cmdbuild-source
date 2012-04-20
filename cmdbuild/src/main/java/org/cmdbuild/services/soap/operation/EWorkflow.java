package org.cmdbuild.services.soap.operation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.Card;
import org.cmdbuild.services.soap.types.Workflow;
import org.cmdbuild.services.soap.utils.WorkflowUtils;
import org.cmdbuild.workflow.ActivityVariable;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.cmdbuild.workflow.operation.SharkFacade;
import org.cmdbuild.workflow.operation.WorkItemQuery;

public class EWorkflow {

	private UserContext userCtx;

	public EWorkflow(UserContext userCtx) {
		this.userCtx = userCtx;
	}
	
	public Workflow startWorkflow(Card card, boolean completeTask) {
		
		SharkFacade management = new SharkFacade(userCtx);
		String className = card.getClassName();
		Log.SOAP.debug("Starting workflow for card " + className);
		ActivityDO activity = management.startProcess(className);
		String workitemid = activity.getWorkItemId();
		Log.SOAP.debug("Workflow work item id " + workitemid);
		String processinstanceid = activity.getProcessInstanceId();
		String processid = String.valueOf(activity.getCmdbuildCardId());
		WorkflowUtils utils = new WorkflowUtils(userCtx);
		Map<String, String> params = utils.setCardSystemValues(card);

		List<Attribute> attributes = card.getAttributeList();
		if (attributes != null) {
			for (Attribute attribute : attributes) {
				String name = attribute.getName();
				String value = attribute.getValue();
				params.put(name, value);
			}
		}

		Log.SOAP.debug("Process id is " + processid);
		Log.SOAP.debug("Process instance id is " + processinstanceid);
		management.generalUpdateActivity(processinstanceid, workitemid, params,
				completeTask);
		Workflow workflow = new Workflow();
		workflow.setProcessid(activity.getCmdbuildCardId());
		workflow.setProcessinstanceid(processinstanceid);
		return workflow;
	}

	public boolean updateWorkflow(Card card, boolean completeTask) {

		List<Attribute> attributes = card.getAttributeList();
		WorkflowUtils utils = new WorkflowUtils(userCtx);
		String processId = utils.getProcessInstanceId(card);
//		Map<String, String> params = utils.setCardSystemValues(card);
		Map<String, String> params = new HashMap<String, String>();
		SharkFacade management = new SharkFacade(userCtx);
		Log.SOAP.debug("Updating workflow " + processId);
		WorkItemQuery query = new WorkItemQuery();
		query.setProcessInstanceId(processId);
		Log.SOAP.debug("Searching activity query: " + query);
		List<ActivityDO> activities = management.getWorkItems(query, false);
		ActivityDO activity = activities.iterator().next();
		String workitemid = activity.getWorkItemId();
		Log.SOAP.debug("Assigned work item id " + workitemid);
		if (attributes != null) {
			for (Attribute attribute : attributes) {
				String name = attribute.getName();
				String value = attribute.getValue();
				params.put(name, value);
			}
		}

		return management.generalUpdateActivity(processId, workitemid, params,
				completeTask);
	}
	
	public boolean resumeWorkflow(Card card, boolean completeTask) {

		WorkflowUtils utils = new WorkflowUtils(userCtx);
		String processId = utils.getProcessInstanceId(card);
		Log.SOAP.debug("Resuming workflow " + processId);
		
		SharkFacade management = new SharkFacade(userCtx);
		return management.resumeProcess(processId);
	}

	public String getProcessHelp(String classname, Integer cardid) {
		String help = "";
		SharkFacade management = new SharkFacade(userCtx);
		ActivityDO template;

		if (cardid != null) {
			ICard card = userCtx.tables().get(classname).cards().get(
					Integer.valueOf(cardid));
			List<ICard> cards = new LinkedList<ICard>();
			cards.add(card);
			template = management.getActivityList(classname, cards, true)
					.get(0);
		} else {
			template = management.startActivityTemplate(classname);
		}

		help = template.getActivityInfo().getActivityDescription();
		return help;
	}

	public AttributeSchema[] getActivityObjects(String className, Integer cardid) {
		List<AttributeSchema> elements;
		if (cardid != null && cardid.intValue() > 0) {
			elements = getActivity(className, cardid, userCtx);
		} else {
			elements = getStartActivityTemplate(className, userCtx);
		}
		
		AttributeSchema[] attributes = new AttributeSchema[elements.size()];
		attributes = elements.toArray(attributes);
		return attributes;
	}

	protected List<AttributeSchema> getStartActivityTemplate(String className,
			UserContext userCtx) {

		SharkFacade management = new SharkFacade(userCtx);
		ActivityDO template = management.startActivityTemplate(className);

		ITable table = userCtx.tables().get(className);
		template.setCmdbuildClassId(table.getId());

		List<AttributeSchema> attrs = serializeActivityDO(template);

		return attrs;
	}

	protected List<AttributeSchema> getActivity(String classname, int cardid,
			UserContext userCtx) {
		SharkFacade management = new SharkFacade(userCtx);
		ICard card = userCtx.tables().get(classname).cards().get(cardid);
		ActivityDO activity = management.getActivityList(card);
		List<AttributeSchema> attrs = serializeActivityDO(activity);
		return attrs;
	}

	private List<AttributeSchema> serializeActivityDO(ActivityDO template) {
		List<ActivityVariable> vars = template.getVariables();
		List<AttributeSchema> attrs = new LinkedList<AttributeSchema>();

		for (ActivityVariable v : vars) {
			if (v.getClientIndex() >= 0) {
				String visibility = v.getType().name();
				EAdministration administration = new EAdministration(userCtx);
				AttributeSchema element = administration.serialize(v.getAttribute(), v.getClientIndex());				
				element.setVisibility(visibility);
				attrs.add(element);
			}
		}
		return attrs;
	}
}

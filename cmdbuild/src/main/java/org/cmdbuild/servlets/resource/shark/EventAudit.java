package org.cmdbuild.servlets.resource.shark;

import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.WorkflowService;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.resource.OutSimpleXML;
import org.cmdbuild.servlets.resource.RESTExported;
import org.cmdbuild.servlets.resource.RESTExported.RestMethod;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.servlets.utils.URIParameter;
import org.cmdbuild.workflow.utils.SimpleXMLDoc;
import org.cmdbuild.workflow.utils.SimpleXMLNode;
import org.cmdbuild.workflow.CmdbuildProcessInfo;
import org.cmdbuild.workflow.WorkflowAttributeType;
import org.cmdbuild.workflow.WorkflowCache;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.common.SharkConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: change implementation: process vars should be passed directly with the representation.
public class EventAudit extends AbstractSharkResource {

	public String baseURI() {
		return "eventaudit";
	}
	
	/**
	 * This method is called by the EventAudit implementation in shark.
	 * It creates a new process card and sends back the values of the attributes 
	 * created by the dbms
	 * @param out
	 * @param requester
	 * @param packId
	 * @param procDefId
	 * @param procInstId
	 * @return
	 * @throws Exception
	 */
	@RESTExported(
			httpMethod=RestMethod.POST,
			subResource="processstarted",
			successCode=HttpServletResponse.SC_CREATED)
	public SimpleXMLDoc processStarted(
			@OutSimpleXML("ProcessCreated") SimpleXMLDoc out,
			@Parameter("requester") String requester,
			@URIParameter(2) String packId,
			@URIParameter(3) String procDefId,
			@URIParameter(4) String procInstId
			) throws Exception {
		Log.WORKFLOW.info(String.format("Event audit - Process instance %s started", procInstId));
		Log.WORKFLOW.debug("Event audit - packId: " + packId + " - procDefId: " + procDefId + " - procInstId: " + procInstId);
		WorkflowCache wfcache = WorkflowCache.getInstance();
		Entry<WMEntity,CmdbuildProcessInfo> entry = wfcache.getProcessInfo(packId, procDefId);
		final String processClass = entry.getValue().getCmdbuildBindedClass();
		ICard crd = UserContext.systemContext().tables().get(processClass).cards().create();
		crd.setValue("ProcessCode", procInstId);
		crd.setUser(requester);
		
		Lookup lkp = WorkflowService.getInstance().getStatusLookupFor(SharkConstants.STATE_OPEN_RUNNING);
		crd.setValue(ProcessAttributes.FlowStatus.toString(), lkp);
		crd.save();
		Log.WORKFLOW.debug("Card saved");
		final int id = crd.getId();
		
		Log.WORKFLOW.debug("Card ID: " + id);
		
		//get the created card..
		ICard theCard = UserContext.systemContext().tables().get(processClass).cards().get(id);
		out.getRoot()
			.createChild("CardId").set(id).parent()
			.createChild("CmdbuildClass").set(processClass);
		
		SimpleXMLNode attrsNode = out.getRoot().createChild("Attributes");
		for(AttributeValue av : theCard.getAttributeValueMap().values()) {
			if(!av.isNull()) {
				String n = av.getSchema().getName();
				SimpleXMLNode attrNode = attrsNode.createChild("Attribute")
				.put("name", n).put("type", WorkflowAttributeType.getSharkType(av.getSchema()));
				XMLAttributeHelper.serializeValue(av, attrNode);
			}
		}
		return out;
	}
	
	@RESTExported(
			httpMethod=RestMethod.PUT,
			subResource="processstatechanged")
	public void processStateChanged(
			@Parameter("requester") String requester,
			@Parameter("cardid") int cardId,
			@Parameter("newstatus") String status,
			@URIParameter(2) String packId,
			@URIParameter(3) String procDefId,
			@URIParameter(4) String procInstId
			) throws Exception {
		Log.WORKFLOW.info(String.format("Event audit - Process instance %s changed state to %s", procInstId, status));
		WorkflowCache wfcache = WorkflowCache.getInstance();
		Entry<WMEntity,CmdbuildProcessInfo> entry = wfcache.getProcessInfo(packId, procDefId);
		String processClass = entry.getValue().getCmdbuildBindedClass();

		//retrieve the Card form the DB and update the variables
		ICard crd = UserContext.systemContext().tables().get(processClass).cards().get(cardId);

		if(requester != null){
			if( !(crd.getUser().equals(requester)) ){
				crd.setUser(requester);
			}
		}
		
		Lookup lookup = WorkflowService.getInstance().getStatusLookupFor(status);
		if(lookup != null){
			Log.WORKFLOW.debug("new status lookup description: " + lookup.getDescription() + ", code: " + lookup.getCode());
			crd.setValue(ProcessAttributes.FlowStatus.toString(), lookup);
		} else {
			Log.WORKFLOW.warn("lookup for status code '" + status + "' not found!");
		}
		
		crd.save();
	}
	
	@RESTExported(
		httpMethod=RestMethod.PUT,
		subResource="nextactivityinfo")
	public void nextActivityInfo(
			@Parameter("cardid") int cardId,
			@Parameter("actdefid") String activityDefinitionId,
			@Parameter("actinstid") String activityInstanceId,
			@Parameter("nextexecutor") String performer,
			@URIParameter(2) String packId,
			@URIParameter(3) String procDefId,
			@URIParameter(4) final String procInstId,
			@URIParameter(5) final String actInstId
			) throws Exception {
		Log.WORKFLOW.info(String.format("Event audit - Next activity %s for process instance %s", activityInstanceId, procInstId));
		Log.WORKFLOW.debug("Event audit - next activity info: " + activityInstanceId + ", actDefId: " + activityDefinitionId);
		
		WorkflowCache wfcache = WorkflowCache.getInstance();
		
		Entry<WMEntity,CmdbuildProcessInfo> procEntry = wfcache.getProcessInfo(packId, procDefId);
		
		//TODO: check if the activity is a starting one for a multistart process. In this case, do not save anything.		
		if(
			procEntry.getValue().isMultiStart() &&
			procEntry.getValue().isStartingActivity(activityDefinitionId)
		) {
			Log.WORKFLOW.debug("activity " + actInstId + " is a starting one of a multistart process.");
			return;
		}
		
		String processClass = procEntry.getValue().getCmdbuildBindedClass();
		ICard crd = UserContext.systemContext().tables().get(processClass).cards().get(cardId);
		
		//set the next executor name
		crd.setValue(ProcessAttributes.CurrentActivityPerformers.toString(), performer);

		crd.save();
	}
	
	@RESTExported(
		httpMethod=RestMethod.PUT,
		subResource="activitymodified")
	public void activityModified(
			Document cmdbCard,
			@Parameter("requester") String requester,
			@Parameter("cardid") int cardId,
			@Parameter("activityname") String activityName,
			@URIParameter(2) String packId,
			@URIParameter(3) String procDefId,
			@URIParameter(4) final String procInstId,
			@URIParameter(5) final String actInstId
			) throws Exception {
		Log.WORKFLOW.info(String.format("Event audit - Activity %s modified", actInstId));
		Log.WORKFLOW.debug("Event audit - packId: " + packId + " - procDefId: " + procDefId + " - procInstId: " + procInstId + " - actInstId: " + actInstId
				+", params -- cardId: " + cardId + " - requester: " + requester);
		WorkflowCache wfcache = WorkflowCache.getInstance();
		Entry<WMEntity,CmdbuildProcessInfo> entry = wfcache.getProcessInfo(packId, procDefId);
		String processClass = entry.getValue().getCmdbuildBindedClass();

		//retrieve the Card form the DB and update the variables
		ICard card = UserContext.systemContext().tables().get(processClass).cards().get(cardId);
		if (requester != null) {
			if (!(card.getUser().equals(requester))){
				card.setUser(requester);
			}
		}

		NodeList nl = cmdbCard.getElementsByTagName("Attribute");
		for (int i=0; i<nl.getLength(); i++) {
			Node node = nl.item(i);
			String attrName = node.getAttributes().getNamedItem("Name").getNodeValue();
			try {
				Object value = parsedRestValue(card, node, attrName);
				card.setValue(attrName, value);
			} catch(Exception e) {
				Log.WORKFLOW.error("error setting the value of " + attrName, e);
			}
		}

		card.setCode(activityName);
		card.save();
	}

	private Object parsedRestValue(ICard card, Node node, String attrName) {
		Object ret = null;
		AttributeValue av = card.getAttributeValue(attrName);
		switch(av.getSchema().getType()) {
		case LOOKUP:
		case REFERENCE:
			NodeList lcs = node.getChildNodes();
			for (int j=0;j<lcs.getLength();j++) {
				Node lc = lcs.item(j);
				if (lc.getNodeName().equals("Id")) {
					String txt = lc.getTextContent();
					if (!txt.equals("-1")) {
						ret = lc.getTextContent();
					}
					break;
				}
			}
			break;
		default:
			ret = node.getTextContent();
		}
		return ret;
	}
}

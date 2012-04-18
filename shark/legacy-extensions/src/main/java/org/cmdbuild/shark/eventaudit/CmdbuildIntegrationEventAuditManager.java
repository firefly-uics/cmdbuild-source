package org.cmdbuild.shark.eventaudit;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis.encoding.Base64;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.cmdbuild.shark.util.ActivityPerformerResolver;
import org.cmdbuild.shark.util.CmdbuildUtils;
import org.cmdbuild.shark.util.CmdbuildUtils.CmdbuildAttributeStruct;
import org.cmdbuild.shark.util.CmdbuildUtils.CmdbuildTableStruct;
import org.cmdbuild.shark.util.CmdbuildUtils.HttpMethod;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.SharkConnection;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.api.internal.eventaudit.AssignmentEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.CreateProcessEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.DataEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.EventAuditException;
import org.enhydra.shark.api.internal.eventaudit.EventAuditManagerInterface;
import org.enhydra.shark.api.internal.eventaudit.EventAuditPersistenceInterface;
import org.enhydra.shark.api.internal.eventaudit.StateEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("unchecked")
public class CmdbuildIntegrationEventAuditManager implements
		EventAuditManagerInterface {
	
	CallbackUtilities cus;
	//String base64authentication;
	String cmdbuildEndpoint;
	
	public void configure(CallbackUtilities callbackutilities) throws Exception {
		this.cus = callbackutilities;
		
		//this.cmdbuildEndpoint = cus.getProperty("CMDBuild.EndPoint", "http://localhost:8080/cmdbuild/shark/");
		this.cmdbuildEndpoint = "eventaudit/";
		
		String user = cus.getProperty("CMDBuild.EndPoint.User");
		String password = cus.getProperty("CMDBuild.EndPoint.Password");
		
		String toEnc = user + ":" + password;
		String base64authentication = Base64.encode(toEnc.getBytes());
		
		cus.info(null, "CMDBuild UserGroupManager configured.");
		
		CmdbuildUtils.configure(cus.getProperty("CMDBuild.EndPoint", "http://localhost:8080/cmdbuild/shark/"), base64authentication);
	}

	public void delete(
			WMSessionHandle wmsessionhandle,
			AssignmentEventAuditPersistenceObject assignmenteventauditpersistenceobject)
			throws EventAuditException {
		// TODO Auto-generated method stub

	}

	public void delete(
			WMSessionHandle wmsessionhandle,
			CreateProcessEventAuditPersistenceObject createprocesseventauditpersistenceobject)
			throws EventAuditException {
		// TODO Auto-generated method stub

	}

	public void delete(WMSessionHandle wmsessionhandle,
			DataEventAuditPersistenceObject dataeventauditpersistenceobject)
			throws EventAuditException {
		// TODO Auto-generated method stub

	}

	public void delete(WMSessionHandle wmsessionhandle,
			StateEventAuditPersistenceObject stateeventauditpersistenceobject)
			throws EventAuditException {
		// TODO Auto-generated method stub

	}

	public List listActivityHistoryInfoWhere(WMSessionHandle wmsessionhandle,
			String s, int i, int j, boolean flag) throws EventAuditException {
		// TODO Auto-generated method stub
		return null;
	}

	public List listProcessDefinitionHistoryInfoWhere(
			WMSessionHandle wmsessionhandle, String s, boolean flag)
			throws EventAuditException {
		// TODO Auto-generated method stub
		return null;
	}

	public List listProcessHistoryInfoWhere(WMSessionHandle wmsessionhandle,
			String s, int i, int j, boolean flag, boolean flag1, boolean flag2)
			throws EventAuditException {
		// TODO Auto-generated method stub
		return null;
	}

	public void persist(
			WMSessionHandle wmsessionhandle,
			AssignmentEventAuditPersistenceObject assignmenteventauditpersistenceobject)
			throws EventAuditException {
		// TODO Auto-generated method stub

	}

	public void persist(
			WMSessionHandle wmsessionhandle,
			CreateProcessEventAuditPersistenceObject createprocesseventauditpersistenceobject)
			throws EventAuditException {
		// TODO Auto-generated method stub

	}

	public void persist(WMSessionHandle wmsessionhandle,
			DataEventAuditPersistenceObject dataeventauditpersistenceobject)
			throws EventAuditException {
		// TODO Auto-generated method stub

	}

	/**
	 * <pre>
	 * type == process
	 *   old: not_started & new: running
	 *   	-> fire processStarted
	 *   new: closed
	 *   	-> fire processClosed
	 *   old: running & new: suspended
	 *   	-> fire processSuspended
	 *   old: suspended & new: running
	 *   	-> fire processResumed
	 *   
	 * type == activity
	 *   new: completed
	 *   	-> fire activityClosed
	 *   new: not_started
	 *   	-> fire nextActivityInfo  
	 * </pre>
	 *
	 */
	public void persist(WMSessionHandle wmsessionhandle,
			StateEventAuditPersistenceObject seao)
			throws EventAuditException {
		try {
			if(seao.getType().equals("processStateChanged")){
				System.out.println(String.format("Event audit -- Process %s state changed from %s to %s", seao.getProcessId(), seao.getOldState(), seao.getNewState()));
				if( "open.not_running.not_started".equals(seao.getOldState()) &&
					"open.running".equals(seao.getNewState()) ){
					//fire processstarted
					CmdbCard classId = sendProcessStarted( seao );
					if(classId != null){
						setCmdbAttributes(classId, wmsessionhandle, seao);
					}
				} else if(seao.getNewState().startsWith("closed")){
					//fire processclosed
					CmdbCard cmdb = getCmdbCardInfo(wmsessionhandle, seao);
					sendProcessModified(seao, cmdb);
				} else if( SharkConstants.STATE_OPEN_RUNNING.equals(seao.getOldState()) && 
					SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED.equals(seao.getNewState())) {
					//process suspended
					CmdbCard cmdb = getCmdbCardInfo(wmsessionhandle, seao);
					sendProcessModified(seao, cmdb);
				} else if(SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED.equals(seao.getOldState()) &&
					SharkConstants.STATE_OPEN_RUNNING.equals(seao.getNewState())) {
					//process resumed
					CmdbCard cmdb = getCmdbCardInfo(wmsessionhandle, seao);
					sendProcessModified(seao, cmdb);
				}
			} else if(seao.getType().equals("activityStateChanged")){
				System.out.println(String.format("Event audit - Activity %s state changed from %s to %s\n", seao.getActivityId(), seao.getOldState(), seao.getNewState()));
				CmdbCard theCard = getCmdbCard(wmsessionhandle, seao);
				if(theCard != null) {
					if(seao.getNewState().equals("closed.completed")) {
						//fire activityclosed
						sendActivityModified(wmsessionhandle,seao,theCard);
					} else if(seao.getNewState().equals(SharkConstants.STATE_OPEN_NOT_RUNNING_NOT_STARTED)) {
						sendNextActivityInfo(wmsessionhandle,seao,theCard);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventAuditException(e);
		}
	}

	public boolean restore(
			WMSessionHandle wmsessionhandle,
			AssignmentEventAuditPersistenceObject assignmenteventauditpersistenceobject)
			throws EventAuditException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean restore(
			WMSessionHandle wmsessionhandle,
			CreateProcessEventAuditPersistenceObject createprocesseventauditpersistenceobject)
			throws EventAuditException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean restore(WMSessionHandle wmsessionhandle,
			DataEventAuditPersistenceObject dataeventauditpersistenceobject)
			throws EventAuditException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean restore(WMSessionHandle wmsessionhandle,
			StateEventAuditPersistenceObject stateeventauditpersistenceobject)
			throws EventAuditException {
		// TODO Auto-generated method stub
		return false;
	}

	public List restoreActivityHistory(WMSessionHandle wmsessionhandle,
			String s, String s1) throws EventAuditException {
		// TODO Auto-generated method stub
		return null;
	}

	public List restoreProcessHistory(WMSessionHandle wmsessionhandle, String s)
			throws EventAuditException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * set in the process the cmdbuild card id, class name and process instance id
	 * @param card
	 * @param handle
	 * @param seao
	 * @throws Exception
	 */
	private void setCmdbAttributes( CmdbCard card, WMSessionHandle handle, StateEventAuditPersistenceObject seao ) throws Exception {
		//set ProcessClass/Code/Id
		String procInstId = seao.getProcessId();
		
		Shark shark = Shark.getInstance();
		shark.getWAPIConnection().assignProcessInstanceAttribute(handle, procInstId, "ProcessId", card.cardId);
		shark.getWAPIConnection().assignProcessInstanceAttribute(handle, procInstId, "ProcessClass", card.cmdbClass);
		shark.getWAPIConnection().assignProcessInstanceAttribute(handle, procInstId, "ProcessCode", procInstId);
		
		
		
		for(WMAttribute wmattr : shark.getWAPIConnection().listProcessInstanceAttributes(handle, procInstId, null, false).getArray()){
			resetDateVariables(handle, procInstId, shark, wmattr);
			updateVariablesFromCmdbuild(card, handle, procInstId, shark, wmattr);
		}
	}

	/*
	 * Reset to null the date variables automatically initialized by shark
	 * with the current date
	 */
	private void resetDateVariables(WMSessionHandle handle, String procInstId,
			Shark shark, WMAttribute wmattr) throws Exception {
		if(wmattr.getType() == WMAttribute.DATETIME_TYPE) {
			shark.getWAPIConnection().assignProcessInstanceAttribute(handle, procInstId, wmattr.getName(), null);
		}
	}

	/*
	 * Updates the process variables with the values from the process card
	 */
	private void updateVariablesFromCmdbuild(CmdbCard card,
			WMSessionHandle handle, String procInstId, Shark shark,
			WMAttribute wmattr) throws Exception {
		for(CmdbAttr attr : card.attrs) {
			if(wmattr.getName().equals(attr.name)) {
				shark.getWAPIConnection().assignProcessInstanceAttribute(handle, procInstId, attr.name, attr.value);
			}
		}
	}

	/**
	 * load the cmdbuild card id and class name
	 * @param handle
	 * @param seao
	 * @return
	 * @throws Exception
	 */
	private CmdbCard getCmdbCardInfo( WMSessionHandle handle, StateEventAuditPersistenceObject seao) throws Exception {
		String procInstId = seao.getProcessId();
		CmdbCard out = new CmdbCard(-1,null,null);
		
		WAPI wapi = Shark.getInstance().getWAPIConnection();
		long id = (Long)wapi.getProcessInstanceAttributeValue(handle, procInstId, "ProcessId").getValue();
		out.cardId = (int)id;
		out.cmdbClass = (String)wapi.getProcessInstanceAttributeValue(handle, procInstId, "ProcessClass").getValue();

		return out;
	}
	
	/**
	 * load the entire cmdbuild card with attributes.<br>
	 * only the Cmdbuild defined attributes are returned.
	 * @param handle
	 * @param seao
	 * @return
	 * @throws Exception
	 */
	private CmdbCard getCmdbCard( WMSessionHandle handle, StateEventAuditPersistenceObject seao ) throws Exception {
		String procInstId = seao.getProcessId();
		CmdbCard out = new CmdbCard(-1,null,new ArrayList<CmdbAttr>());
		
		
		WAPI wapi = Shark.getInstance().getWAPIConnection();
		long id = (Long)wapi.getProcessInstanceAttributeValue(handle, procInstId, "ProcessId").getValue();
		out.cardId = (int)id;
		out.cmdbClass = (String)wapi.getProcessInstanceAttributeValue(handle, procInstId, "ProcessClass").getValue();
		
		CmdbuildTableStruct struct = CmdbuildUtils.getInstance().getStructureFromName(out.cmdbClass);
		CmdbAttr cur = null;
		Object val = null;
		WMAttribute wmattr = null;
		String aName;
		for(CmdbuildAttributeStruct attrDef : struct.getAttributes()) {
			aName = attrDef.getName();
			if( aName.equals("ProcessCode") ||
				aName.equals("FlowStatus") ||
				aName.equals("ActivityDescription") ||
				aName.equals("Code") ||
				aName.equals("Notes")) {
				continue;
			}
			wmattr = wapi.getActivityInstanceAttributeValue(handle, procInstId, seao.getActivityId(), aName);
			if(wmattr == null) { 
				continue;
			}
			val = wmattr.getValue();
			if(val != null) {
				cur = new CmdbAttr(attrDef.getName(),val);
				out.attrs.add(cur);
			}
		}
		
		return out;
	}
	
	private String getProcessResourceAddress( EventAuditPersistenceInterface eapi ) {
		return eapi.getPackageId() + "/" + eapi.getProcessDefinitionId() + "/" + eapi.getProcessId();
	}
	private String getActivityResourceAddress( EventAuditPersistenceInterface eapi ){
		return getProcessResourceAddress(eapi) + "/" + eapi.getActivityId();
	}
	
	private boolean isToolActivity( WMSessionHandle handle, StateEventAuditPersistenceObject seapo ) {
		return seapo.getActivityDefinitionType() == 2;
	}

	
	private void sendNextActivityInfo( WMSessionHandle handle,
			StateEventAuditPersistenceObject seapo, CmdbCard card ) {
		
		if(isToolActivity(handle,seapo)) {
			//no need to send activity info..
			return;
		}
		
		String nextExecutor = null;
		try {
			/*
			 * put this code in ActivityPerformerResolver, to not interfere with previous usage use another method
			 
			String perfOrId = null;
			XPDLBrowser browser = Shark.getInstance().getXPDLBrowser();
			WMEntity actEntity = Shark.getInstance().getAdminMisc().getActivityDefinitionInfo(handle, seapo.getProcessId(), seapo.getActivityId());
			WMFilter filter = new WMFilter("Name", WMFilter.EQ, "Performer");
			filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
			WMAttributeIterator ei = browser.listAttributes(handle, actEntity, filter, true);
			WMAttribute out = (ei.hasNext()?ei.getArray()[0]:null);
			System.out.println("performer of " + seapo.getActivityId() + ": " + out.getValue());
			perfOrId = (String)out.getValue();
			
			WMEntity procEntity = Shark.getInstance().getAdminMisc().getProcessDefinitionInfo(handle, seapo.getProcessId());
			WMEntity[] participants = WMEntityUtilities.getAllParticipants(handle, browser, procEntity);
			
			for(WMEntity participant : participants) {
				if(participant.getId().equals(perfOrId)) {
					nextExecutor = perfOrId;
				}
			}
			/*
			 * the ActivityPerformerResolver should be called so:
			 * resolver.resolvePerformer(handle, seapo.getProcessId(), seapo.getActivityId());
			 */
			
			ActivityPerformerResolver resolver = new ActivityPerformerResolver();
			SharkConnection sconn = Shark.getInstance().getSharkConnection();
			sconn.attachToHandle(handle);
			nextExecutor = resolver.resolvePerformer(sconn, handle, seapo.getProcessId(), seapo.getActivityId());
		} catch (Exception e1) {
			e1.printStackTrace();
			nextExecutor = null;
		}
		
		String url = this.cmdbuildEndpoint + "nextactivityinfo/" + getActivityResourceAddress(seapo);
		cus.debug(null, "activity opened: " + url);
		
		PutMethod put = (PutMethod)CmdbuildUtils.getInstance()
		.createCmdbuildMethod(HttpMethod.PUT, url,
			new NameValuePair("actdefid",seapo.getActivityDefinitionId()),
			new NameValuePair("actinstid",seapo.getActivityId()),
			new NameValuePair("cardid",card.cardId+""),
			new NameValuePair("nextexecutor",nextExecutor)
		);
		
		int status = CmdbuildUtils.getInstance().execute(put);
		System.out.println("ActivityOpened Request status: " + status);
	}
	
	private String getUserName(String userRole) {
		int atIdx = userRole.indexOf('@');
		String out = userRole;
		if(atIdx != -1) {
			out = userRole.substring(0,atIdx);
		}
		return out;
	}
	
	private void sendActivityModified( WMSessionHandle handle,
			StateEventAuditPersistenceObject seapo, CmdbCard card ) throws Exception {
		int cardId = card.cardId;
		System.out.println("sendActivityModified cardId: " + cardId);
		
		String url = this.cmdbuildEndpoint + "activitymodified/" + getActivityResourceAddress(seapo);
		System.out.println("activity closed: " + url);

		String uName = (isToolActivity(handle,seapo)) ? "system" : getUserName(seapo.getUsername());

		PutMethod method = (PutMethod)CmdbuildUtils.getInstance()
		.createCmdbuildMethod(HttpMethod.PUT, url,
			new NameValuePair("newstatus",seapo.getNewState()),
			new NameValuePair("requester",uName),
			new NameValuePair("cardid",cardId+""),
			new NameValuePair("activityname",seapo.getActivityName())
		);

		String xmlText = card.toSimpleXML().toString();
		System.out.println("act.mod:\n" + xmlText);
		RequestEntity reqEntity = new StringRequestEntity(xmlText,"text/xml","UTF8");
		method.setRequestEntity(reqEntity);
		int status = CmdbuildUtils.getInstance().execute(method);
		System.out.println("ActivityClosed Request status: " + status);
	}
	
	private void sendProcessModified( StateEventAuditPersistenceObject seapo,CmdbCard cmdb ){
		String url = this.cmdbuildEndpoint + "processstatechanged/" + getProcessResourceAddress(seapo);
		cus.debug(null, "process modified: " + url);
		
		PutMethod put = (PutMethod)CmdbuildUtils.getInstance()
		.createCmdbuildMethod(HttpMethod.PUT, url,
			new NameValuePair("requester",getUserName(seapo.getUsername())),
			new NameValuePair("cardid",cmdb.cardId+""),
			new NameValuePair("oldstatus",seapo.getOldState()),
			new NameValuePair("newstatus",seapo.getNewState())
		);
		
		int status = CmdbuildUtils.getInstance().execute(put);
		System.out.println("ProcessStateChanged Request status: " + status);
	}
	private CmdbCard sendProcessStarted( StateEventAuditPersistenceObject seapo ) throws Exception {
		String url = this.cmdbuildEndpoint + "processstarted/" + getProcessResourceAddress(seapo);
		cus.debug(null, "process created: " + url);
		
		PostMethod post = (PostMethod)CmdbuildUtils.getInstance()
		.createCmdbuildMethod(HttpMethod.POST, url,
			new NameValuePair("requester",getUserName(seapo.getUsername()))
		);
		
		int status = CmdbuildUtils.getInstance().execute(post);
		if(status < 0){return null;}

		System.out.println("ProcessStarted Request status: " + status);
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(post.getResponseBodyAsStream());
		
		String cardIdRaw = doc.getElementsByTagName("CardId").item(0).getTextContent();
		String cmdbCls = doc.getElementsByTagName("CmdbuildClass").item(0).getTextContent();
		
		List<CmdbAttr> attrs = parseAttrs(doc.getElementsByTagName("Attribute"));
		return new CmdbCard(Integer.parseInt(cardIdRaw),cmdbCls,attrs);
	}

	private List<CmdbAttr> parseAttrs(NodeList nodes) {
		List<CmdbAttr> out = new ArrayList<CmdbAttr>();
		for(int i=0; i<nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String name = node.getAttributes().getNamedItem("name").getNodeValue();
			Object value = CmdbAttrXMLParser.resolve(node);
			if(value != null) {
				out.add(new CmdbAttr(name,value));
			} else {
				System.err.println("failed to parse attribute '" + name + "'");
			}
			
		}
		return out;
	}

}

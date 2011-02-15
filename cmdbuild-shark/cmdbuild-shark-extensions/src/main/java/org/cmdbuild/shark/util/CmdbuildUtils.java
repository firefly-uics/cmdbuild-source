package org.cmdbuild.shark.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.shark.eventaudit.CmdbAttrXMLParser;
import org.cmdbuild.shark.toolagent.AbstractWSToolAgent;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.api.client.wfservice.NameValue;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CmdbuildUtils {
	public enum HttpMethod {
		GET,POST,PUT,DELETE;
	}
	
	String base64authentication;
	String cmdbuildEndpoint;
	
	static CmdbuildUtils instance = new CmdbuildUtils();
	public static CmdbuildUtils getInstance(){ return instance; }
	
	public static void configure(String endpoint, String base64auth) {
		System.out.println("cmdbuildUtils endpoint: " + endpoint);
		instance.cmdbuildEndpoint = endpoint;
		instance.base64authentication = base64auth;
	}
	
	public static class CmdbuildTableStruct {
		int id;
		String name;
		
		List<CmdbuildAttributeStruct> attributes;
		
		public CmdbuildTableStruct() {
			id = -1;
			name = "";
			attributes = new ArrayList<CmdbuildAttributeStruct>();
		}
		
		public int getId() {
			return id;
		}
		public String getName() {
			return name;
		}
		public List<CmdbuildAttributeStruct> getAttributes() {
			return attributes;
		}
		public CmdbuildAttributeStruct getAttribute(String attrName) {
			for(CmdbuildAttributeStruct attr : attributes) {
				if(attr.name.equals(attrName))
					return attr;
			}
			return null;
		}
	}
	public static class CmdbuildAttributeStruct {
		String name;
		String type;
		
		String lookupType;
		String referenceClass;
		
		public String getName() {
			return name;
		}
		public String getType() {
			return type;
		}
		
		public String getLookupType() {
			return lookupType;
		}
		public String getReferenceClass() {
			return referenceClass;
		}
	}
	
	HttpClient client;
	
	private CmdbuildUtils() {
		
		client = getHttpClient();
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
	}

	public static HttpClient getHttpClient() {
		 MultiThreadedHttpConnectionManager conmgr = new MultiThreadedHttpConnectionManager();
         int maxConn = getMaxConnectionProperty();
         conmgr.getParams().setDefaultMaxConnectionsPerHost(maxConn);
         conmgr.getParams().setMaxTotalConnections(maxConn);
         HttpClient client = new HttpClient(conmgr);
         return client;
	}
	
	private static int getMaxConnectionProperty(){
		int maxconn;
		try {
			String maxConnProp = getSharkConfProperty("CMDBuild.WS.MaxConn");
			maxconn = Integer.valueOf(maxConnProp);
		} catch (Exception e) {
			maxconn = 10;
		}
		return maxconn;
	}

	private static String getSharkConfProperty(String propName) throws Exception {
		NameValue[] values = Shark.getInstance().getProperties();
		for (int i=0; i<values.length; i++){
			NameValue nv = values[i];
			if (nv.getName().equals(propName)){
				return nv.getValue().toString();
			}
		}
		return null;
	}

	
	private void addAuth( HttpMethodBase method ) {
		Header auth = new Header("Authorization", "Basic " + this.base64authentication);
		method.addRequestHeader(auth);
	}
	private void setQueryString( HttpMethodBase method,NameValuePair...params) {
		method.setQueryString(params);
	}
	
	private void setAuthAndQS( HttpMethodBase method, NameValuePair...params) {
		addAuth(method);
		setQueryString(method,params);
	}
	
	public int execute(HttpMethodBase method) {
		try {
			return client.executeMethod(method);
		} catch (Exception e) {
			// Not using exceptions
		}
		return -1;
	}
	
	private CmdbuildTableStruct getStructFromIdOrName(Object o) throws SAXException, IOException, ParserConfigurationException {
		GetMethod method = new GetMethod(this.cmdbuildEndpoint + "cmdbuildstruct/tableInfo");
		setAuthAndQS(method, (o instanceof String) ? new NameValuePair("classname",o.toString()) : new NameValuePair("classid",o.toString()));
		execute(method);
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(method.getResponseBodyAsStream());
		
		return getStructFromXML(doc);
	}
	
	/**
	 * Load a ReferenceType object from a class, the reference attribute name and the id of the referenced row.
	 * @param processClass
	 * @param attributeName
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public ReferenceType loadReferenceForProcess(String processClass,String attributeName,int id) throws Exception {
		
		if(id <= 0) {
			return null;
		}
		GetMethod method = new GetMethod(this.cmdbuildEndpoint + "cmdbuildstruct/loadreference");
		setAuthAndQS(method, 
				new NameValuePair("classname",processClass),
				new NameValuePair("attributename",attributeName),
				new NameValuePair("referenceid",id+""));
		execute(method);
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(method.getResponseBodyAsStream());
		
		return (ReferenceType)CmdbAttrXMLParser.EXTERNAL.parse(doc.getDocumentElement());
	}
	
	/**
	 * Load a LookupType from its Id
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public LookupType loadLookup(int id) throws Exception {
		
		if(id <= 0) {
			return null;
		}
		GetMethod method = new GetMethod(this.cmdbuildEndpoint + "cmdbuildstruct/loadlookup");
		setAuthAndQS(method, 
				new NameValuePair("lookupid",id+""));
		execute(method);
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(method.getResponseBodyAsStream());
		
		return (LookupType)CmdbAttrXMLParser.EXTERNAL.parse(doc.getDocumentElement());
	}
	
	public boolean sendSuspendProcess(String processInstanceId) throws Exception {
		PostMethod method = new PostMethod(this.cmdbuildEndpoint + "suspendprocess");
		setAuthAndQS(method,
				new NameValuePair("processinstanceid",processInstanceId));
		return (-1 != execute(method));
	}
	
	//public for test
	public CmdbuildTableStruct getStructFromXML(Document doc) {
		CmdbuildTableStruct out = new CmdbuildTableStruct();
		Node tableElm = doc.getElementsByTagName("TableInfo").item(0);
		out.name = tableElm.getAttributes().getNamedItem("name").getNodeValue();
		out.id = Integer.parseInt(tableElm.getAttributes().getNamedItem("id").getNodeValue());
		
		NodeList nl = doc.getElementsByTagName("Attribute");
		for(int i=0;i<nl.getLength();i++) {
			Node n = nl.item(i);
			CmdbuildAttributeStruct cas = new CmdbuildAttributeStruct();
			cas.name = n.getAttributes().getNamedItem("name").getNodeValue();
			cas.type = n.getAttributes().getNamedItem("type").getNodeValue();
			
			Node tmp;
			cas.lookupType = ((tmp = n.getAttributes().getNamedItem("lookupType")) != null) ? tmp.getNodeValue() : null;
			cas.referenceClass = ((tmp = n.getAttributes().getNamedItem("referenceClass")) != null) ? tmp.getNodeValue() : null;
			
			out.attributes.add(cas);
		}
		
		return out;
	}

	public CmdbuildTableStruct getStructureFromId(int id) throws Exception{
		System.out.println("cmdbuildInfo getStruct from id: " + id);
		return getStructFromIdOrName(id);
	}
	public CmdbuildTableStruct getStructureFromName(String name) throws Exception {
		System.out.println("cmdbuildInfo getStruct from name: " + name);
		return getStructFromIdOrName(name);
	}
	
	public String getCmdbuildEndpoint() {
		return cmdbuildEndpoint;
	}
	
	/**
	 * Create an HttpMethod to be used to communicate with the rest-resources of cmdbuild
	 * (handle authorization)
	 * @param m
	 * @param url
	 * @param qs
	 * @return
	 */
	public HttpMethodBase createCmdbuildMethod( HttpMethod m, String url, NameValuePair...qs )
	{
		HttpMethodBase out = null;
		switch(m){
		case DELETE:	out = new DeleteMethod(this.cmdbuildEndpoint + url); break;
		case GET:		out = new GetMethod(this.cmdbuildEndpoint + url); break;
		case POST:		out = new PostMethod(this.cmdbuildEndpoint + url); break;
		case PUT:		out = new PutMethod(this.cmdbuildEndpoint + url); break;
		}
		this.setAuthAndQS(out, qs);
		return out;
	}
	public HttpMethodBase createCmdbuildMethod( HttpMethod m, String url, Map<String,String> params )
	{
		List<NameValuePair> qs = new ArrayList<NameValuePair>();
		for(String key : params.keySet()) {
			if(null != params.get(key)) {
				qs.add(new NameValuePair(key,params.get(key)));
			}
		}
		return createCmdbuildMethod(m, url, qs.toArray(new NameValuePair[]{}));
	}
	
	public static String getUserFromUserRole(String userRole){
		int atIdx = userRole.indexOf('@');
		String user = userRole;
		if(atIdx != -1) {
			user = userRole.substring(0,atIdx);
		}	
		return user;
	}

	public static WMWorkItem getActiveWorkItem(WMSessionHandle shandle, WAPI wapi, String processInstanceId)
			throws Exception, ToolAgentGeneralException {
		WMFilter filter = Shark.getInstance().getAssignmentFilterBuilder().addProcessIdEquals(shandle, processInstanceId);
		WMWorkItem[] workItemArray = wapi.listWorkItems(shandle, filter, false).getArray();
		if (workItemArray.length < 1) {
			throw new ToolAgentGeneralException("No workitems for process " + processInstanceId);
		} else {
			System.out.println("More than one workitem. Picking the first one.");
			return workItemArray[0];
		}
	}
	
	public static ReferenceType createReferenceFromWSCard(Card card) throws Exception {
		ReferenceType out = null;
		if (card != null) {
			int id = card.getId();
			int idClass = CmdbuildUtils.getInstance().getStructureFromName(card.getClassName()).getId();
			String descr = "";
			for (Attribute a : card.getAttributeList()) {
				if ("Description".equals(a.getName())) {
					descr = a.getValue();
				}
			}
			out = new ReferenceType(id, idClass, descr);
		}
		return out;
	}

	/*
	 * 
	 */

	static public String getCurrentUserNameForProcessInstance(Private stub,
			String cmdbuildProcessClass, int cmdbuildProcessId) throws Exception {
		Card card = stub.getCard(cmdbuildProcessClass, cmdbuildProcessId, usernameAttributeArray());

		if (AbstractWSToolAgent.SYSTEM_USER.equals(card.getUser())) {
			List<Card> history = stub.getCardHistory(cmdbuildProcessClass, cmdbuildProcessId, null, null).getCards();
			for (Card historyItem : history) {
				if (!AbstractWSToolAgent.SYSTEM_USER.equals(historyItem.getUser())) {
					card = historyItem;
					break;
				}
			}
		}
		return card.getUser();
	}

	private final static String CLASS_USERATTRIBUTE = "User";

	static private List<Attribute> usernameAttributeArray() {
		Attribute usernameAttribute = new Attribute();
		usernameAttribute.setName(CLASS_USERATTRIBUTE);
		List<Attribute> usernameAttributeList = new ArrayList<Attribute>();
		usernameAttributeList.add(usernameAttribute);
		return usernameAttributeList;
	}

	/*
	 * 
	 */

	public static String getCurrentGroupName(WMSessionHandle shandle) {
		String partName = (String)shandle.getVendorData();
		int atIdx = partName.indexOf('@');
		if(atIdx != -1) {
			partName = partName.substring(atIdx+1, partName.length());
		}
		return partName;
	}
}

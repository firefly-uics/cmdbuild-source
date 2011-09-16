package org.cmdbuild.shark.toolagent;

import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.BOOLEAN_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.DATETIME_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.FLOAT_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.INTEGER_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.STRING_TYPE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis.encoding.Base64;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.cmdbuild.shark.eventaudit.CmdbAttrXMLParser;
import org.cmdbuild.shark.util.CmdbuildUtils;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.xpdl.elements.ExtendedAttributes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExecuteStoredProcedureToolAgent extends
		AbstractConditionalToolAgent {
	
	static final String postTo = "executestoredprocedure/";
	String cmdbuildEndpoint;
	String base64authentication;
	@Override
	public void configure(CallbackUtilities arg0) throws Exception {
		super.configure(arg0);
		
		this.cmdbuildEndpoint = arg0.getProperty("CMDBuild.EndPoint");
		
		String user = cus.getProperty("CMDBuild.EndPoint.User");
		String password = cus.getProperty("CMDBuild.EndPoint.Password");
		
		String toEnc = user + ":" + password;
		base64authentication = Base64.encode(toEnc.getBytes());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void innerInvoke(WMSessionHandle shandle, long handle,
			WMEntity appInfo, WMEntity toolInfo, String applicationName,
			String procInstId, String assId, AppParameter[] parameters,
			Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {

		String url = cmdbuildEndpoint + postTo;// + parameters[0].the_value;
		boolean isRs = false;//(Boolean)parameters[1].the_value;

		String extAttribs = (String) parameters[0].the_value;
		String procedure = "";
		try {
			ExtendedAttributes eas = this.readParamsFromExtAttributes(extAttribs);
			if(eas.containsElement("Procedure")){
				procedure = eas.getFirstExtendedAttributeForName("Procedure").getVValue();
			}else if(eas.containsElement("CursorProcedure")){
				procedure = eas.getFirstExtendedAttributeForName("CursorProcedure").getVValue();
				isRs = true;
			}

			if(procedure == null || procedure.trim().length() == 0){
				throw new Exception("extended attribute \"Procedure\" or \"CursorProcedure\" must be valorized!");
			}
			url += procedure;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new ToolAgentGeneralException("cannot find extended attributes");
		}
		
		List<Object> args = new ArrayList();
		List<AppParameter> outClasses = new ArrayList();
		
		for (int i = 1; i < parameters.length; i++) {
			final AppParameter param = parameters[i];
			if (param.the_mode.contains("IN")) {
				args.add(param.the_value);
			}
			if (param.the_mode.contains("OUT")) {
				outClasses.add(param);
			}
		}
		
		StringBuffer sbuf = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		sbuf.append("<ExecuteStoredProcedure ResultSetType=\"").append(isRs).append("\" >")
		.append("<Inputs>");
		for(Object arg : args) {
			sbuf.append("<Input type=\"");
			if(arg instanceof Integer || arg instanceof Long) {
				sbuf.append(INTEGER_TYPE).append("\" >").append(arg);
			} else if(arg instanceof Float || arg instanceof Double) {
				sbuf.append(FLOAT_TYPE).append("\" >").append(arg);
			} else if(arg instanceof java.util.Calendar) {
				sbuf.append(DATETIME_TYPE).append("\" >")
				.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(((java.util.Calendar)arg).getTime()));
			} else if(arg instanceof java.util.Date) {
				sbuf.append(DATETIME_TYPE).append("\" >")
				.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format((java.util.Date)arg));
			} else if(arg instanceof Boolean) {
				sbuf.append(BOOLEAN_TYPE).append("\" >")
				.append(arg);
			} else if(arg instanceof String) {
				sbuf.append(STRING_TYPE).append("\" >")
				.append(arg);
			} else if(arg instanceof ReferenceType) {
				sbuf.append(INTEGER_TYPE).append("\" >").append( ((ReferenceType)arg).getId() );
			} else if(arg instanceof LookupType) {
				sbuf.append(INTEGER_TYPE).append("\" >").append( ((LookupType)arg).getId() );
			} else {
				System.out.println("unsupported input class type: " + ((arg==null) ? "null!" : arg.getClass()));
				throw new ToolAgentGeneralException("ExecuteStoredProcedureToolAgent unsupported input class type: " + ((arg==null) ? "null!" : arg.getClass()));
			}
			sbuf.append("</Input>\r\n");
		}
		sbuf.append("</Inputs><OutClasses>");
		int idx = 0;
		for(AppParameter outCls : outClasses) {
			sbuf.append("<OutClass index=\"").append(idx).append("\">");
			if(outCls.the_class.equals(Long.class)) {
				sbuf.append(Integer.class.getCanonicalName());
			} else if(outCls.the_class.equals(ReferenceType.class)) {
				sbuf.append(Integer.class.getCanonicalName());
			} else if(outCls.the_class.equals(LookupType.class)) {
				sbuf.append(Integer.class.getCanonicalName());
			} else {
				sbuf.append(outCls.the_class.getCanonicalName());
			}
			sbuf.append("</OutClass>\r\n");
			idx++;
		}
		sbuf.append("</OutClasses></ExecuteStoredProcedure>");
		
		HttpClient client = new HttpClient();
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		
		PostMethod method = new PostMethod(url);
		Header auth = new Header("Authorization", "Basic " + this.base64authentication);
		method.addRequestHeader(auth);
		String xmls = sbuf.toString();
//		method.setRequestBody(sbuf.toString());
		System.out.println("ExecuteStoredProcedure xml document:\r\n" + xmls);
		try {
			RequestEntity reqEnt = new StringRequestEntity(xmls,"text/xml","UTF8");
			method.setRequestEntity(reqEnt);
			int status = client.executeMethod(method);
			System.out.println("ExecuteStoredProcedure status: " + status);

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(method.getResponseBodyAsStream());
			NodeList results = doc.getElementsByTagName("Result");
			Element result = (Element)results.item(0);
			NodeList outs = result.getElementsByTagName("Item");
			for(int i=0;i<outs.getLength();i++) {
				Node node = outs.item(i);
				int theIdx = Integer.parseInt(node.getAttributes().getNamedItem("idx").getNodeValue());
				AppParameter outparam = outClasses.get(theIdx);
				Object val;
				if(outparam.the_class.equals(ReferenceType.class)) {
					int refId = (Integer)CmdbAttrXMLParser.INT.parse(node);
					val = CmdbuildUtils.getInstance().loadReferenceForProcess(this.cmdbuildProcessClass, outparam.the_actual_name, refId);
				} else if(outparam.the_class.equals(LookupType.class)) {
					int lkpId = (Integer)CmdbAttrXMLParser.INT.parse(node);
					val = CmdbuildUtils.getInstance().loadLookup(lkpId);
				} else {
					val = CmdbAttrXMLParser.resolve(node);
				}
				outparam.the_value = val;
				System.out.println("Received sp value " + theIdx + ", " + outparam.the_formal_name + ": " + outparam.the_value);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ToolAgentGeneralException("cannot execute stored procedure: " + procedure);
		}
	}

}

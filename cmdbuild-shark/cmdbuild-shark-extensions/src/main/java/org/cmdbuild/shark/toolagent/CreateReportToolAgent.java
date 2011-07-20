package org.cmdbuild.shark.toolagent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethodBase;
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
import org.enhydra.shark.xpdl.elements.ExtendedAttributes;

public class CreateReportToolAgent extends AbstractConditionalToolAgent {

	@Override
	protected void innerInvoke(WMSessionHandle shandle, long handle,
			WMEntity appInfo, WMEntity toolInfo, String applicationName,
			String procInstId, String assId, AppParameter[] parameters,
			Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {
		
		String type,code,format,cmdburl;
		Map<String,String> params = new HashMap<String,String>();
		
		ExtendedAttributes eas;
		try {
			eas = this.readParamsFromExtAttributes((String) parameters[0].the_value);
			type = eas.getFirstExtendedAttributeForName("Type").getVValue();
			code = eas.getFirstExtendedAttributeForName("Code").getVValue();
			format = eas.getFirstExtendedAttributeForName("Format").getVValue();
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new ToolAgentGeneralException("cannot read type or code of report!");
		}
		cmdburl = CmdbuildUtils.getInstance().getCmdbuildEndpoint() + "report/";

		params.put("code", code);
		params.put("type", type);
		params.put("format", format);
		params.put("cmdburl", cmdburl);

		AppParameter out = null;
		for(int i=1; i<parameters.length;i++) {
			AppParameter p = parameters[i];
			log(p);
			if(p.the_mode.equalsIgnoreCase("IN")) {
				params.put(p.the_formal_name, toString(p.the_value));
			} else {
				out = p;
			}
		}
	
		HttpMethodBase method = CmdbuildUtils.getInstance().createCmdbuildMethod(CmdbuildUtils.HttpMethod.POST, "createreport", params);
		int status = CmdbuildUtils.getInstance().execute(method);
		if(status == 200) {
			try {
				out.the_value = method.getResponseBodyAsString();
			} catch (IOException e) {
				e.printStackTrace();
				throw new ToolAgentGeneralException("cannot get report url: " + type + "." + code);
			}
		} else {
			throw new ToolAgentGeneralException("cannot create report: " + type + "." + code);
		}
	}
	
	private void log(AppParameter p) {
		l("AppParameter: " + p.the_formal_name + ", mode: " + p.the_mode + ", value: " + p.the_value);
	}
	private void l(Object o){System.out.println(o);}
	
	private String toString(Object attrValue) {
		String serialized = null;
		if(attrValue != null) {
			if(attrValue instanceof String) {
				serialized = (String)attrValue;
			} else if(attrValue instanceof ReferenceType) {
				serialized = ( ((ReferenceType)attrValue).getId() + "" );
			} else if(attrValue instanceof LookupType) {
				serialized = ( ((LookupType)attrValue).getId() + "" );
			} else if(attrValue instanceof Calendar) {
				Calendar cal = (Calendar)attrValue;
				serialized = ( new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(cal.getTime()) );
			} else {
				serialized = ( attrValue.toString() );
			}
		}
		return serialized;
	}

}

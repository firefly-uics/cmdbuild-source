package org.cmdbuild.workflow.xpdl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.cmdbuild.logger.Log;
import org.cmdbuild.utils.FileUtils;
import org.cmdbuild.workflow.xpdl.XPDLFormalParameterDescriptor.FormalParameterMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unchecked")
public class JSONApplicationReader {

	static final String jsonFile = "wfapplications.json";
	static List<XPDLApplicationDescriptor> descriptors = new ArrayList();
	public static List<XPDLApplicationDescriptor> getApplications() {
		if(descriptors.size() > 0){ return descriptors; }
		try {
			InputStream is = JSONApplicationReader.class.getClassLoader().getResourceAsStream("org/cmdbuild/workflow/xpdl/wfapplications.json");

			JSONObject obj = new JSONObject(FileUtils.getContents(is));
			Iterator<Object> okeyIter = obj.keys();
			while(okeyIter.hasNext()) {
				Object okey = okeyIter.next();
				String key = (String)okey;
				JSONObject appObj = obj.getJSONObject(key);
				String desc = appObj.getString("description");
				JSONArray jparams = appObj.getJSONArray("params");
				JSONArray jextattrs = appObj.getJSONArray("extattrs");
				
				List<XPDLFormalParameterDescriptor> params = parseParams(jparams);
				List<XPDLExtendedAttribute> extattrs = parseExtAttrs(jextattrs);
				
				descriptors.add(new XPDLApplicationDescriptor(key,desc,params,extattrs));
			}
			
			Collections.sort(descriptors, new Comparator<XPDLApplicationDescriptor>(){
				public int compare(XPDLApplicationDescriptor o1,
						XPDLApplicationDescriptor o2) {
					return o1.id.compareTo(o2.id);
				}
			});
		} catch (JSONException e) {
			Log.WORKFLOW.error("Cannot read tool agent file");
		}
		return descriptors;
	}
	
	static List<XPDLFormalParameterDescriptor> parseParams( JSONArray jparams ) throws JSONException {
		List<XPDLFormalParameterDescriptor> out = new ArrayList();
		for(int i=0;i<jparams.length();i++) {
			JSONObject jparam = jparams.getJSONObject(i);
			String id = jparam.getString("id");
			String type = jparam.getString("type");
			String mode = jparam.getString("mode");
			out.add(new XPDLFormalParameterDescriptor(XPDLAttributeType.valueOf(type),FormalParameterMode.valueOf(mode),id));
		}
		return out;
	}
	
	static List<XPDLExtendedAttribute> parseExtAttrs( JSONArray jextattrs ) throws JSONException {
		List<XPDLExtendedAttribute> out = new ArrayList();
		for(int i=0;i<jextattrs.length();i++) {
			JSONObject jea = jextattrs.getJSONObject(i);
			Iterator<Object> okeyIter = jea.keys();
			while(okeyIter.hasNext()){
				String key = (String)okeyIter.next();
				String value = jea.getString(key);
				out.add(new XPDLExtendedAttribute(key,value));
			}
		}
		return out;
	}
}

package org.cmdbuild.workflow.extattr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.SharkWSFacade;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ManageRelations extends AbstractCmdbuildExtendedAttribute {
	
	public String extendedAttributeName() {
		return "manageRelation";
	}
	
	static final String Domain = "DomainName";
	static final String DomainDirect = "IsDirect";
	static final String ObjRef = "ObjRef";
	static final String ClassName = "ClassName";
	static final String ClassId = "ClassId";
	static final String ObjId = "ObjId";
	static final String Required = "Required";
	static final String TargetClassId = "TargetClassId";
	
	static final String Functions = "EnabledFunctions";
	
	static final String[] functionNames = {
		"linkElement",
		"createAndLinkElement",
		"multi",
		"single",
		"allowModify",
		"allowUnlink",
		"allowModifyCard",
		"allowDelete"
	};

	@Override
	protected void doConfigure(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, WMWorkItem workItem, ActivityDO activityDO,
			Map<String, Object> processVars,
			Map<String, Object> currentOutValues) {
		ExtendedAttributeConfigParams eacf = (ExtendedAttributeConfigParams)activityDO.getCmdbExtAttrsParams().get(identifier);
		String domainName = (String)eacf.getParameters().get(Domain);
		IDomain theDom = userCtx.domains().get(domainName);
		
		activityDO.getCmdbExtAttrsParams().put(this.identifier + "_domain", theDom);
	}

	@Override
	protected void doReact(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, SharkWSFacade facade,
			WMWorkItem workItem, ActivityDO activityDO,
			Map<String, String[]> submissionParameters, 
			ExtendedAttributeConfigParams oldConfig,
			Map<String, Object> outputParameters) {
		if(this.outParameters.size() == 0){ return; }
		// so here.. we need to create/remove the relationships (nope!),
		// create the ReferenceType[] and save it in shark
		// ... that's all?
		Map<String,Object> prm = oldConfig.getParameters();
		
		ITable otherSide = null;
		if(prm.containsKey(ObjRef)) {
			ReferenceType theRef = (ReferenceType)prm.get(ObjRef);
			otherSide = UserContext.systemContext().tables().get(theRef.getIdClass());
		} else if(prm.get(ClassName) instanceof String){
			String cname = (String)prm.get(ClassName);
			try{
				ITable tbl = UserContext.systemContext().tables().get(cname);
				otherSide = tbl;
			} catch(NotFoundException e){
				Log.WORKFLOW.error("cannot find class " + cname, e);
				throw e;
			}
		}
		
		String domainName = (String)prm.get(Domain);
		int[] refs = (int[])outputParameters.get(this.outParameters.get(0));
		
		if(refs.length == 0) {
			Log.WORKFLOW.debug("no relations selected!");
			return;
		}
		
		String s = "received int array: ";
		for(int ref : refs) {
			s += ref + ",";
		}
		Log.WORKFLOW.debug(domainName + ", " + s);

		IDomain theDom = userCtx.domains().get(domainName);
		ITable valueClass;

		boolean isDirect;
		if( prm.containsKey(DomainDirect) ) {
			isDirect = prm.get(DomainDirect).equals("true");
		} else {
			isDirect = (theDom.getDirectionFrom( otherSide ));
		}
		if(!isDirect) {
			valueClass = theDom.getClass1();
		} else {
			valueClass = theDom.getClass2();
		}
		String[] idVals = new String[refs.length];
		int id = 0;
		for(int refId : refs) {
			idVals[id] = refId + "";
			id++;
		}
		List<ReferenceType> newValueList = new ArrayList<ReferenceType>();
		for( ICard crd : valueClass.cards().list().filter("Id",AttributeFilterType.EQUALS, (Object[])idVals) ) {
			newValueList.add(new ReferenceType(crd.getId(),crd.getIdClass(),crd.getDescription()));
		}
		
		ReferenceType[] newValue = newValueList.toArray(new ReferenceType[]{});
		
		Map<String,Object> newValues = new HashMap<String,Object>();
		newValues.put(this.outParameters.get(0), newValue);
		
		facade.updateWorkItemValues(handle, userCtx, factory, workItem, newValues);
	}
	
	@Override
	protected Object getOutputValue(String outParamName, WMSessionHandle handle,
			UserContext userCtx, SharkWSFactory factory, WMWorkItem workItem,
			ActivityDO activityDO, Map<String, String[]> submissionParameters) {
		return convertToIntArray(submissionParameters.get(outParamName));
	}
	
	private void putObjId( Map<String,Object> params, JSONObject out ) throws JSONException {
		if(params.containsKey(ObjRef)) {
			ReferenceType theRef = (ReferenceType)params.get(ObjRef);
			out.put("id", theRef.getId());
		} else {
			if(params.get(ObjId) instanceof Long){
				out.put("id", ((Long)params.get(ObjId)).intValue());
			} else if (params.get(ObjId) instanceof String) {
				String objIdString = (String) params.get(ObjId);
				try {
					int objId = Integer.parseInt(objIdString);
					out.put("id", objId);
				} catch (NumberFormatException e) {
					// client param
					out.put("id", objIdString);
				}
			}
		}
	}

	private ITable putClassId( Map<String,Object> params, JSONObject out ) throws JSONException {
		if (params.containsKey(ObjRef)) {
			ReferenceType theRef = (ReferenceType)params.get(ObjRef);
			out.put("idClass", theRef.getIdClass());
			return UserContext.systemContext().tables().get(theRef.getIdClass());
		} else {
			String cname = (String)params.get(ClassName);
			try{
				ITable tbl = UserContext.systemContext().tables().get(cname);
				out.put("idClass", tbl.getId());
				return tbl;
			} catch(NotFoundException e){
				Log.WORKFLOW.error("cannot find class " + cname, e);
				throw e;
			}
		}
	}

	@Override
	protected void addCustomParams(ActivityDO activityDO, JSONObject object,
			ExtendedAttributeConfigParams eacp) throws JSONException {
		Map<String,Object> prm = eacp.getParameters();
		putEnabledFunctionalities( (String)prm.get(Functions),object );
		
		putObjId(prm, object);
		ITable tbl = putClassId(prm, object);
		IDomain theDom = (IDomain)activityDO.getCmdbExtAttrsParams().get(this.identifier + "_domain");
		
		if(eacp.getParameters().containsKey(Required)) {
			object.put("required", true);
		} else {
			object.put("required", false);
		}
		
		object.put("domainIdNoDir", theDom.getId());
		if (getDomainDirection(prm, theDom, tbl)) {
			object.put("domainId", theDom.getId() + "_D");
			object.put("TargetClassId", theDom.getClass2().getId());
		} else {
			object.put("domainId", theDom.getId() + "_I");
			object.put("TargetClassId", theDom.getClass1().getId());
		}

		if( this.outParameters.size() > 0 ) {
			object.put("outputName", this.outParameters.get(0));
			ReferenceType[] curVal = (ReferenceType[])eacp.currentOuts.get(this.outParameters.get(0));
			Log.WORKFLOW.debug("current manage.rel. value: " + curVal);
			if(curVal != null) {
				Log.WORKFLOW.debug("curVal size: " + curVal.length);
			}
			if(curVal != null && curVal.length > 0) {
				JSONArray curValJson = new JSONArray();
				for(ReferenceType ref : curVal) {
					curValJson.put(ref.getId());
				}
				object.put("currentValue", curValJson);
			}
		}
	}
	
	private boolean getDomainDirection(Map<String,Object> prm, IDomain theDom, ITable tbl) {
		if (prm.containsKey(DomainDirect)) {
			return prm.get(DomainDirect).equals("true");
		} else {
			return (theDom.getDirectionFrom(tbl));
		}
	}
	
	private void putEnabledFunctionalities(String functionString, JSONObject object) throws JSONException {
		JSONObject enabledFunctions = new JSONObject();
		String trimmedFunctionString = functionString.trim();
		int len = Math.min(trimmedFunctionString.length(), functionNames.length); 
		for (int i=0; i<len; i++) {
			char c = trimmedFunctionString.charAt(i);
			enabledFunctions.put(functionNames[i], c == '1');
		}
		object.put("enabledFunctions", enabledFunctions);
	}
	
}

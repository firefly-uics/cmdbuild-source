package org.cmdbuild.workflow.extattr;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.SharkWSFacade;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateModifyCard extends AbstractCmdbuildExtendedAttribute {
	static final String ObjRef = "Reference";
	static final String ClassName = "ClassName";
	static final String ObjId = "ObjId";
	static final String READONLY = "ReadOnly";

	public String extendedAttributeName() {
		return "createModifyCard";
	}
	
	private ITable getTable( Map<String,Object> params ) {
		if (params.containsKey(ObjRef)) {
			ReferenceType theRef = (ReferenceType)params.get(ObjRef);
			return UserContext.systemContext().tables().get(theRef.getIdClass());
		} else {
			return UserContext.systemContext().tables().get((String)params.get(ClassName));
		}
	}
	private void putObjId( Map<String,Object> params, JSONObject out ) throws JSONException {
		if(params.containsKey(ObjRef)) {
			ReferenceType theRef = (ReferenceType)params.get(ObjRef);
			out.put("id", theRef.getId());
		} else if(params.containsKey(ObjId)){
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
		} else {
			out.put("id", -1);
		}
	}

	private void putClassIdAndName( Map<String,Object> params, JSONObject out ) throws JSONException {
		if(params.containsKey(ObjRef)) {
			ReferenceType theRef = (ReferenceType)params.get(ObjRef);
			ITable refTable = UserContext.systemContext().tables().get(theRef.getIdClass());
			out.put("idClass", refTable.getId());
			out.put("ClassName", refTable.getName());
		} else {
			String cname = (String) params.get(ClassName);
			try{
				out.put("idClass", UserContext.systemContext().tables().get(cname).getId());
			} catch (NotFoundException e){
				//assume client param
				out.put("idClass", params.get(ClassName));
			}
		}
	}

	@Override
	protected void doConfigure(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, WMWorkItem workItem, ActivityDO activityDO,
			Map<String, Object> processVars,
			Map<String, Object> currentOutValues) {
	}

	@Override
	protected void doReact(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, SharkWSFacade facade, WMWorkItem workItem,
			ActivityDO activityDO, Map<String, String[]> submissionParameters,
			ExtendedAttributeConfigParams oldConfig,
			Map<String, Object> outputParameters, boolean advance) {
		Map<String,Object> prms = oldConfig.getParameters();
		if (isReadOnly(prms))
			return;
		ITable table = getTable(prms);
		if (this.outParameters.size() > 0 ) {
			//this is a new card, save it
			String varName = this.outParameters.get(0);
			int cardId = (Integer)outputParameters.get(varName);
			ICard theCard = table.cards().get(cardId);
			
			ReferenceType theRef = new ReferenceType( cardId,table.getId(),theCard.getDescription() );
			
			Map<String,Object> newValues = new HashMap<String,Object>();
			newValues.put(varName, theRef);
			
			facade.updateWorkItemValues(handle, userCtx, factory, workItem, newValues);
		}
	}

	@Override
	protected void addCustomParams(ActivityDO activityDO, JSONObject object,
			ExtendedAttributeConfigParams eacp) throws JSONException {
		putClassIdAndName(eacp.getParameters(), object);
		putObjId(eacp.getParameters(), object);
		putReadOnly(eacp.getParameters(), object);
		if(this.outParameters.size() > 0){
			object.put("outputName", this.outParameters.get(0));
		}
	}

	private void putReadOnly(Map<String, Object> params, JSONObject object) throws JSONException {
		object.put("ReadOnly", isReadOnly(params));
	}

	boolean isReadOnly(Map<String, Object> params) {
		return (params.containsKey(READONLY));
	}

	@Override
	protected Object getOutputValue(String outParamName, WMSessionHandle handle,
			UserContext userCtx, SharkWSFactory factory, WMWorkItem workItem,
			ActivityDO activityDO, Map<String, String[]> submissionParameters) {
		return convertToInt(submissionParameters.get(outParamName));
	}

}

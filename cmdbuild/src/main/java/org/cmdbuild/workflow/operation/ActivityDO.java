package org.cmdbuild.workflow.operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.ActivityVariable;
import org.cmdbuild.workflow.CmdbuildActivityInfo;
import org.cmdbuild.workflow.CmdbuildProcessInfo;
import org.cmdbuild.workflow.extattr.CmdbuildExtendedAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.client.utilities.SharkWSFactory;

public class ActivityDO {
	String processInstanceId;
	String workItemId;
	
	int cmdbuildCardId;
	int cmdbuildClassId;
	
	String cmdbuildCardNotes;
	
	String performer;
	
	boolean editable;

	CmdbuildProcessInfo processInfo;
	CmdbuildActivityInfo activityInfo;
	List<ActivityVariable> variables;
	
	List<CmdbuildExtendedAttribute> cmdbExtAttrs;
	Map<String,Object> cmdbExtAttrsParams;
	
	public ActivityDO() {
		super();
	}

	// TODO: pass CmdbExtAttr list in constructor or get them directly from activityInfo?
	@SuppressWarnings("unchecked")
	public ActivityDO(CmdbuildProcessInfo processInfo,
			CmdbuildActivityInfo activityInfo,
			List<ActivityVariable> variables, 
			String processInstanceId,
			String workItemId,
			int cmdbuildCardId,
			boolean editable) {
		super();
		this.processInfo = processInfo;
		this.activityInfo = activityInfo;
		this.variables = variables;
		this.processInstanceId = processInstanceId;
		this.workItemId = workItemId;
		this.cmdbuildCardId = cmdbuildCardId;
		
		this.editable = editable;
		
		this.cmdbExtAttrsParams = new HashMap();
		this.cmdbExtAttrs = activityInfo.getExtendedAttrs();
		
		Log.WORKFLOW.debug("ActivityDO object created for: " + processInstanceId + ", " + workItemId);
	}
	
	public void updateVariables(Map<String,String> parameters) throws ORMException {
		for (ActivityVariable av : variables) {
			String avName = av.getName();
			if (parameters.containsKey(avName)) {
				String value = parameters.get(avName);
				av.setValue(value);
			}
		}
	}
	
	public void configureExtendedAttributes(
			WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem workItem) {
		Log.WORKFLOW.debug("configureExtAttrs. on ActivityDO called");
		for(CmdbuildExtendedAttribute cea : cmdbExtAttrs) {
			try{
				Log.WORKFLOW.debug("configure extended attribute: " + cea.extendedAttributeName());
				cea.configure(handle, userCtx, factory, workItem, this);
			} catch(Exception e) {
				Log.WORKFLOW.error("Failed to configure extended attribute: " + cea.extendedAttributeName(), e);
			}
		}
	}
	
	public boolean isUserStoppable() {
		return (this.processInfo != null) ? this.processInfo.isUserStoppable() : false;
	}
	
	public Object getCmdbExtAttrParam(String id) {
		return this.cmdbExtAttrsParams.get(id);
	}
	public void setCmdbExtAttrParam(String id, Object obj) {
		this.cmdbExtAttrsParams.put(id, obj);
	}

	public CmdbuildProcessInfo getProcessInfo() {
		return processInfo;
	}

	public CmdbuildActivityInfo getActivityInfo() {
		return activityInfo;
	}

	public List<ActivityVariable> getVariables() {
		return variables;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public String getWorkItemId() {
		return workItemId;
	}

	public int getCmdbuildCardId() {
		return cmdbuildCardId;
	}
	
	public List<CmdbuildExtendedAttribute> getCmdbExtAttrs() {
		return cmdbExtAttrs;
	}
	
	public Map<String, Object> getCmdbExtAttrsParams() {
		return cmdbExtAttrsParams;
	}
	
	public int getCmdbuildClassId() {
		return cmdbuildClassId;
	}
	public void setCmdbuildClassId(int cmdbuildClassId) {
		this.cmdbuildClassId = cmdbuildClassId;
	}

	public String getPerformer() {
		return performer;
	}

	public void setPerformer(String performer) {
		this.performer = performer;
	}
	
	public String getCmdbuildCardNotes() {
		return cmdbuildCardNotes;
	}
	public void setCmdbuildCardNotes(String cmdbuildCardNotes) {
		this.cmdbuildCardNotes = cmdbuildCardNotes;
	}
	
	public boolean isEditable() {
		return editable;
	}
}

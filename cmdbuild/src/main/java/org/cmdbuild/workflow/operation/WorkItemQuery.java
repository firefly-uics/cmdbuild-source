package org.cmdbuild.workflow.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.StringUtils;
import org.cmdbuild.workflow.SharkWSFacade;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.client.utilities.SharkWSFactory;

public class WorkItemQuery {
	SharkWSFacade facade;

	String processDefinitionId = null;
	String processInstanceId = null;
	
	String packageId = null;
	Collection<Integer> cmdbuildCardIds = null;
	Collection<String> procInstIds = null;
	
	public void setSharkFacade(SharkWSFacade facade) {
		this.facade = facade;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}
	
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	
	public String getPackageId() {
		return packageId;
	}

	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}

	public void setCmdbuildCardIds(Collection<Integer> cmdbuildCardIds) {
		this.cmdbuildCardIds = cmdbuildCardIds;
	}
	public Collection<Integer> getCmdbuildCardIds() {
		return cmdbuildCardIds;
	}
	
	public void setProcInstIds(Collection<String> procInstIds) {
		this.procInstIds = procInstIds;
	}
	public Collection<String> getProcInstIds() {
		return procInstIds;
	}

	public WMWorkItem[] filter( WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory) throws Exception{
		
		WMFilter flt = factory.getAssignmentFilterBuilder().createEmptyFilter(handle);
		
		if( processDefinitionId != null ){
			flt = factory.getAssignmentFilterBuilder().addProcessDefIdEquals(handle, processDefinitionId);
		}
		if( processInstanceId != null ){
			flt = factory.getAssignmentFilterBuilder().addProcessIdEquals(handle, processInstanceId);
		}
		if( packageId != null ) {
			flt = factory.getAssignmentFilterBuilder().addPackageIdEquals(handle, packageId);
		}

		if( procInstIds != null ) {
			if(procInstIds.size() == 0){
				//return empty array
				return new WMWorkItem[]{};
			}
			List<WMFilter> flts = new LinkedList<WMFilter>();
			for(String procInstId : procInstIds) {
				WMFilter cur = factory.getAssignmentFilterBuilder().createEmptyFilter(handle);
				cur = factory.getAssignmentFilterBuilder().addProcessIdEquals(handle, procInstId);
				flts.add(cur);
			}
			flt = factory.getAssignmentFilterBuilder().orForArray(handle, flts.toArray(new WMFilter[]{}));
		}
		
		//first list accepted
		flt = factory.getAssignmentFilterBuilder().setOrderByAccepted(handle, flt, true);
		//then by the latest created
		flt = factory.getAssignmentFilterBuilder().setOrderByCreatedTime(handle, flt, false);
		
		WMWorkItem[] allWorkItems = factory.getWAPIConnection().listWorkItems(handle, flt, false).getArray();
		
		if (this.cmdbuildCardIds != null) {
			Log.WORKFLOW.debug("loaded work items size: " + allWorkItems.length);
			allWorkItems = cmdbuildIdsFilter(handle, userCtx, factory, allWorkItems, this.cmdbuildCardIds);
			Log.WORKFLOW.debug("size after filter on cardIds: " + allWorkItems.length);
		}

		return keepOneWorkItemPerActivity(allWorkItems, userCtx);
	}
	
	private WMWorkItem[] keepOneWorkItemPerActivity(WMWorkItem[] allWorkItems, UserContext userCtx) {
		Map<String,WMWorkItem> map = new HashMap<String,WMWorkItem>();
		for (WMWorkItem item : allWorkItems) {
			if (!map.containsKey(item.getActivityInstanceId())) {
				map.put(item.getActivityInstanceId(), item);
			}
		}
		return map.values().toArray(new WMWorkItem[map.size()]);
	}

	private WMWorkItem[] cmdbuildIdsFilter(
			WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem[] items, Collection<Integer> ids) {
		Log.WORKFLOW.debug("select on " + StringUtils.join(ids, ","));
		Set<Integer> theSet = new HashSet<Integer>(ids);
		List<WMWorkItem> out = new ArrayList<WMWorkItem>();
		String dbg = "cmdbCardIds filter ";
		for(WMWorkItem item : items) {
			int theId = facade.getCmdbuildCardId(handle, userCtx, factory, item);
			if(theSet.contains(theId)){
				dbg += theId + ", ";
				out.add(item);
			} else {
				dbg += theId + " NOT FOUND, ";
			}
		}
		Log.WORKFLOW.debug(dbg);
		return out.toArray(new WMWorkItem[]{});
	}
}

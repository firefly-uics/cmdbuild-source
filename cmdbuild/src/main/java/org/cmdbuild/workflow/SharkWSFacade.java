package org.cmdbuild.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.WorkflowService;
import org.cmdbuild.services.auth.UserContext;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstanceState;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstanceState;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItemState;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.common.AssignmentFilterBuilder;
import org.enhydra.shark.client.utilities.SharkWSFactory;


/**
 * Here can be found the basic operations to interact with the Shark Engine.
 * This class does not handle connections, so the WMSessionHandle must be passed to the methods.
 * Developers should encapsulate method calls inside a WorkflowOperation instance and execute through 
 * the WorkflowService.execute method.
 */
public class SharkWSFacade {

	public enum SharkEvent {
		PROCESS_STARTED,
		PROCESS_COMPLETED,
		PROCESS_ABORTED,
		PROCESS_TERMINATED,
		PROCESS_SUSPENDED,
		ACTIVITY_STARTED,
		ACTIVITY_MODIFIED,
		ACTIVITY_CLOSED
	}
	public interface SharkEventListener {
		public void handle( SharkEvent evtType,
				UserContext userCtx,
				SharkWSFacade facade,
				WMSessionHandle handle,
				SharkWSFactory factory,
				WorkflowService service,
				Object... vars);
	}
	//marker interface, instances of this interface will be removed after the event has been fired
	public interface OneShotSharkEventListener extends SharkEventListener {}

	public class ActivityVariables {
		TableImpl processTable;
		List<ActivityVariable> variables;
	}

	public boolean isEditable(UserContext userCtx, WMWorkItem item, WMSessionHandle handle) {
		return isInEditableState(item) && canBeAssigned(item, userCtx, handle);
	}

	private boolean canBeAssigned(WMWorkItem item, UserContext userCtx, WMSessionHandle handle) {
		boolean assignable = false;
		if (userCtx.privileges().isAdmin()) {
			assignable = true;
		} else if (handle != null) { // ????
			String activityParticipant = getActivityParticipant(handle, userCtx, WorkflowService.getInstance().getFactory(), item);
			assignable = userCtx.belongsTo(activityParticipant);
		}
		return assignable;
	}

	private boolean isInEditableState(WMWorkItem item) {
		return item.getState().isOpen() && !WMWorkItemState.OPEN_SUSPENDED.equals(item.getState());
	}

	public void checkPrivilegesOnWorkItem(WMWorkItem item, UserContext userCtx, WMSessionHandle handle) {
		if (!userCtx.privileges().isAdmin() && !canBeAssigned(item, userCtx, handle)) {
			throw WorkflowExceptionType.WF_WRONG_CREDENTIALS.createException();
		}
	}

	private static boolean workItemIsAssignedToCurrentGroup(String workItemParticipantGroup, UserContext userCtx) {
		return userCtx.getDefaultGroup().getName().equals(workItemParticipantGroup);
	}

	private void sendEvent( SharkEvent evtType,
			UserContext userCtx,
			WMSessionHandle handle,
			Object...vars) {
		WorkflowService.getInstance().sendSharkEvent(evtType, userCtx, handle, this, vars);
	}

	private void logOriginalException(Exception e) {
		Log.WORKFLOW.error("Original workflow exception", e);
	}

	private Entry<WMEntity,CmdbuildProcessInfo> processInfo( String cmdbuildBindedClass ) {
		Entry<WMEntity,CmdbuildProcessInfo> out = WorkflowCache.getInstance().getProcessInfoFromBindedClass(cmdbuildBindedClass);
		return out;
	}
	
	public Entry<WMEntity,CmdbuildProcessInfo> getProcessInfo( WMSessionHandle handle,
			SharkWSFactory factory,
			WMWorkItem workItem) throws Exception {
		WMProcessInstance procInst = factory.getWAPIConnection().getProcessInstance(handle, workItem.getProcessInstanceId());
		return WorkflowCache.getInstance().getProcessInfo( procInst.getProcessFactoryName() );
	}
	public Entry<WMEntity,CmdbuildProcessInfo> getProcessInfo( WMSessionHandle handle,
			SharkWSFactory factory,
			String procInstId) throws Exception {
		WMProcessInstance procInst = factory.getWAPIConnection().getProcessInstance(handle, procInstId);
		return WorkflowCache.getInstance().getProcessInfo( procInst.getProcessFactoryName() );
	}
	public Entry<WMEntity,CmdbuildActivityInfo> getActivityInfo( WMSessionHandle handle,
			SharkWSFactory factory,
			WMWorkItem workItem) throws Exception {
		WMEntity procEnt = getProcessInfo( handle,factory,workItem ).getKey();
		return WorkflowCache.getInstance().getActivityInfo(workItem.getActivityDefinitionId(), procEnt);
	}
	public Entry<WMEntity,CmdbuildActivityInfo> getActivityInfo( WMSessionHandle handle,
			SharkWSFactory factory,
			WMWorkItem workItem,
			WMEntity procEnt) throws Exception {
		return WorkflowCache.getInstance().getActivityInfo(workItem.getActivityDefinitionId(), procEnt);
	}
	
	
	
	private AssignmentFilterBuilder assfb() throws Exception {
		return WorkflowService.getInstance().getFactory().getAssignmentFilterBuilder();
	}
	
	public String createAndStartProcess(WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			String cmdbuildBindedClass) {
		Entry<WMEntity,CmdbuildProcessInfo> procInfo = processInfo(cmdbuildBindedClass);
		if (!procInfo.getValue().hasInitialUserRole(userCtx)) {
			Log.WORKFLOW.debug("User has no start privileges on the process");
			throw WorkflowExceptionType.WF_CANNOT_START.createException();
		}
		
		try{
			WAPI wapi = factory.getWAPIConnection();
			String out = wapi.createProcessInstance(handle, 
					procInfo.getKey().getPkgId() + "#" + procInfo.getKey().getPkgVer() + "#" + procInfo.getKey().getId(), null);
			return wapi.startProcess(handle, out);
		} catch (Exception e){
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_START.createException();
		}
	}

	public List<WMWorkItem> getAllWorkItems( WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			String procInstanceId) {
		try{
			WMFilter flt = assfb().createEmptyFilter(handle);
			flt = assfb().addProcessIdEquals(handle, procInstanceId);

			WMWorkItem[] allItems = factory.getWAPIConnection().listWorkItems(handle, flt, false).getArray();
			
			if(allItems.length==0){
				Log.WORKFLOW.warn("No workitems found for user " + userCtx.getUsername() + "!");
			}
			
			return Arrays.asList(allItems);
		} catch(Exception e){
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_GET_WORKITEMS.createException();
		}
	}

	// TODO: I don't like really to start/abort the workitems, it'll be better to cancel directly the workitem/assignment/whatever
	/**
	 * this is a kind of trick: multi start processes by default creates an assignment for every user in the specified role
	 * (or to the specified user, if HUMAN participant is used).
	 * CMDBuild multi start processes are there only to avoid the creation of multiple processes and start from
	 * different roles (now, this thing can be accomplished in other ways -- ie, process parameter with the role to use --),
	 * so here I check if there are other activities than the one assigned to the current user and close them.
	 */
	public WMWorkItem filterForCurrentGroup( WMSessionHandle handle,
			SharkWSFactory factory,
			UserContext userCtx,
			String procInstId,
			String cmdbuildBindedClass,
			List<WMWorkItem> workItems) {
		try{
			WMEntity procInfo = processInfo(cmdbuildBindedClass).getKey();
			WMWorkItem out = null;
			List<String> closedActivities = new LinkedList<String>();
			if (workItems.size() > 1) { // is multistart?
				for (WMWorkItem workItem : workItems) {
					if (userCtx.getDefaultGroup().isAdmin()) {
						Log.WORKFLOW.debug("workItems size: " + workItems.size() + ", search for AdminStart activity");
						CmdbuildActivityInfo activityInfo = WorkflowCache.getInstance().getActivityInfo(workItem.getActivityDefinitionId(), procInfo).getValue();
						if (activityInfo.isAdminStart()) {
							Log.WORKFLOW.debug("found admin start activity");
							out = workItem;
							break;
						}
					} else {
						Log.WORKFLOW.debug("workItems size: " + workItems.size() + ", search for current group activity");
						String workItemParticipantGroup = getWorkItemParticipantGroup(workItem, procInfo);
						if (workItemIsAssignedToCurrentGroup(workItemParticipantGroup, userCtx)) {
							Log.WORKFLOW.debug("found current group activity");
							out = workItem;
							break;
						}
					}
				}
				closeEveryOtherWorkItem(out, handle, factory, procInstId, workItems, closedActivities);
			} else {
				Log.WORKFLOW.debug(" activity, close others (if any)...");
				out = workItems.get(0);
				checkPrivilegesOnWorkItem(out, userCtx, handle);
			}
			
			Log.WORKFLOW.debug("resetting activity info, due to multi start activities...");
			if (out != null) {
				String nextExecutor = this.getActivityParticipant(handle, userCtx, factory, out);
				int cardId = this.getCmdbuildCardId(handle, userCtx, factory, out);
				String table = this.getProcessInfo(handle, factory, out).getValue().getCmdbuildBindedClass();
				ICard crd = UserContext.systemContext().tables().get(table).cards().get(cardId);
				crd.setValue(ProcessAttributes.CurrentActivityPerformers.toString(), nextExecutor);
				crd.save();
			}
			Log.WORKFLOW.debug("..end");
			return out;
		} catch(Exception e){
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_START.createException();
		}
	}

	private String getWorkItemParticipantGroup(WMWorkItem workItem, WMEntity procInfo) {
		CmdbuildActivityInfo activityInfo = WorkflowCache.getInstance().getActivityInfo(workItem.getActivityDefinitionId(), procInfo).getValue();
		return activityInfo.getParticipantIdOrExpression();
	}

	private void closeEveryOtherWorkItem(WMWorkItem currentWorkItem, WMSessionHandle handle, SharkWSFactory factory,
			String procInstId, List<WMWorkItem> workItems, List<String> closedActivities) throws Exception {
		for (WMWorkItem item : workItems) {
			if (!item.getActivityInstanceId().equals(currentWorkItem.getActivityInstanceId())) {
				if (!closedActivities.contains(item.getActivityInstanceId())) {
					closedActivities.add(item.getActivityInstanceId());
					Log.WORKFLOW.debug("closing activity: " + item.getActivityInstanceId());
					factory.getWAPIConnection().changeActivityInstanceState(handle, procInstId, item.getActivityInstanceId(), WMActivityInstanceState.OPEN_RUNNING);
					factory.getWAPIConnection().changeActivityInstanceState(handle, procInstId, item.getActivityInstanceId(), WMActivityInstanceState.CLOSED_ABORTED);
				}
			}
		}
	}
	
	public WMWorkItem getWorkItem( WMSessionHandle handle,
			SharkWSFactory factory,
			String procInstId, String workItemId) {
		try {
			Log.WORKFLOW.debug("get WorkItem  [ProcInstId: " + procInstId + ", WorkItemId: " + workItemId);
			WMWorkItem item = factory.getWAPIConnection().getWorkItem(handle, procInstId, workItemId);
			if (item != null)
				return item;
		} catch (Exception e) {
			logOriginalException(e);
		}
		throw WorkflowExceptionType.WF_CANNOT_GET_WORKITEM.createException(workItemId);
	}

	public void setWorkItemToRunning( WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem workItem) {
		
		try {
			if (workItem.getState().getValue() == WMWorkItemState.OPEN_NOTRUNNING.getValue()) {
				WAPI wapi = factory.getWAPIConnection();
				Log.WORKFLOW.debug("accepting work item");
				wapi.changeWorkItemState(handle, workItem.getProcessInstanceId(), workItem.getId(), WMWorkItemState.OPEN_RUNNING);
			}
		} catch(Exception e){
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_ACCEPT_WORKITEM.createException();
		}
	}
	
	public void resumeWorkItemIfSuspended(WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem workItem) {
		
		try{
			if (workItem.getState().getValue() == WMWorkItemState.OPEN_SUSPENDED.getValue()) {
				WAPI wapi = factory.getWAPIConnection();
				Log.WORKFLOW.debug("resuming work item");
				wapi.changeWorkItemState(handle, workItem.getProcessInstanceId(), workItem.getId(), WMWorkItemState.OPEN_RUNNING);
			}
		} catch(Exception e){
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_RESUME_WORKITEM.createException();
		}
	}
	
	public List<ActivityVariable> getProcessVariables( WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			String procInstId) {
		
		try{
			Entry<WMEntity,CmdbuildProcessInfo> procInfo = this.getProcessInfo(handle, factory, procInstId);
			ITable schema = UserContext.systemContext().tables().get(procInfo.getValue().getCmdbuildBindedClass());
			
			List<ActivityVariable> out = new ArrayList<ActivityVariable>();
			for(IAttribute attr : schema.getAttributes().values()) {
				out.add(new ActivityVariable(attr));
			}
			return out;
		} catch(Exception e) {
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_GET_WORKITEM_VARIABLES.createException();
		}
	}
	public List<ActivityVariable> getWorkItemVariables( WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem workItem) {
		
		try{
			WAPI wapi = factory.getWAPIConnection();
			Entry<WMEntity,CmdbuildProcessInfo> procInfo = this.getProcessInfo(handle, factory, workItem);
			ITable schema = UserContext.systemContext().tables().get( procInfo.getValue().getCmdbuildBindedClass() );
			WMEntity procEntity = procInfo.getKey();
			CmdbuildActivityInfo activityInfo = WorkflowCache.getInstance()
				.getActivityInfo(workItem.getActivityDefinitionId(), procEntity).getValue();
			List<ActivityVariable> out = activityInfo.getVariableInstances(schema);

			
			activityInfo.getActivityName();
			for(ActivityVariable var : out){
				WMAttribute wmattr = null;
				
				//first try workItem
				wmattr = wapi.getWorkItemAttributeValue(handle, workItem.getProcessInstanceId(), workItem.getId(), var.variable.getSchema().getName());
				
				if(wmattr == null){
					//try process
					wmattr = wapi.getProcessInstanceAttributeValue(handle, workItem.getProcessInstanceId(), var.variable.getSchema().getName());
				}
				if(wmattr != null)
					WorkflowAttributeType.setIfFound(var.variable, wmattr);
			}
			
			return out;
		} catch(Exception e){
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_GET_WORKITEM_VARIABLES.createException();
		}
	}
	
	public int getCmdbuildCardId( WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem workItem) {
		try{
			WAPI wapi = factory.getWAPIConnection();
			
			long id = (Long)wapi.getProcessInstanceAttributeValue(handle, workItem.getProcessInstanceId(), "ProcessId").getValue();
			return (int)id;
		} catch(Exception e){
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_GET_PROCESSCARDID.createException();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<ActivityVariable> getProcessWorkItemVariables( WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem workItem) {
		try{
			WAPI wapi = factory.getWAPIConnection();
			Entry<WMEntity,CmdbuildProcessInfo> procInfo = this.getProcessInfo(handle, factory, workItem);
			ITable schema = UserContext.systemContext().tables().get( procInfo.getValue().getCmdbuildBindedClass() );
			
			List<ActivityVariable> attrs = new ArrayList();
			List<ActivityVariable> wiattrs = WorkflowCache.getInstance()
				.getActivityInfo(workItem.getActivityDefinitionId(), procInfo.getKey())
				.getValue()
				.getVariableInstances(schema);
			
			for(IAttribute attr : schema.getAttributes().values()){
				ActivityVariable var = null;
				//if it's an activity variable, use the instance found in CmdbActivityInfo...
				for(ActivityVariable av : wiattrs){
					if(av.variable.getSchema().getName().equals(attr.getName())) {
						var = av; break;
					}
				}
				//...otherwise build a new ActivityVariable
				//maybe set the activity mode to 'hidden'? or 'non-process-variable'?
				if(var == null){ var = new ActivityVariable(attr); }
				
				//search variable in WorkItem instance..
				WMAttribute wmattr = wapi.getWorkItemAttributeValue(handle, workItem.getProcessInstanceId(), workItem.getId(), var.variable.getSchema().getName());
				if(wmattr == null){
					//..not found, so search it in process instance
					wmattr = wapi.getProcessInstanceAttributeValue(handle, workItem.getProcessInstanceId(), var.variable.getSchema().getName());
				}
				//there is the possibility that the variable is also not in the process
				if(wmattr != null)
					WorkflowAttributeType.setIfFound(var.variable, wmattr);
				attrs.add(var);
			}
			
			return attrs;
		} catch(Exception e) {
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_GET_PROCESS_VARIABLES.createException();
		}
	}

	
	//variables known only by shark
	public void updateWorkItemValues( WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem workItem,
			Map<String,Object> newValues) {

		WAPI wapi = null;
		try{
			wapi = factory.getWAPIConnection();
		} catch(Exception e){
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_GETWAPI_CONNECTION.createException();
		}

		Log.WORKFLOW.debug("Assign WorkItem " + workItem.getName() + " - " + workItem.getId() + " attribute values... (no cmdbuild mapping)");
		for(String key : newValues.keySet()) {
			Object newValue = newValues.get(key);
			Log.WORKFLOW.debug("Attribute '" + key + "': " + newValue);
			try{
				wapi.assignWorkItemAttribute(handle, workItem.getProcessInstanceId(), workItem.getId(), key, newValue);
			} catch(Exception e){
				logOriginalException(e);
				throw WorkflowExceptionType.WF_CANNOT_UPDATE_WORKITEM_VARIABLES.createException();
			}
		}
		sendEvent(SharkEvent.ACTIVITY_MODIFIED, userCtx, handle, workItem);

	}
	
	public void updateWorkItemValues(WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem workItem,
			List<ActivityVariable> variables) {
		WAPI wapi = null;
		try{
			wapi = factory.getWAPIConnection();
		} catch(Exception e){
			throw WorkflowExceptionType.WF_CANNOT_GETWAPI_CONNECTION.createException();
		}

		for (ActivityVariable var : variables) {
			Object newValue = WorkflowAttributeType.getValue(var.variable);
			if (var.variable.isChanged()) {
				String varname = var.variable.getSchema().getName();
				Log.WORKFLOW.debug("Attribute '" + var.variable.getSchema().getName() + "': " + newValue);
				try{
					wapi.assignWorkItemAttribute(handle, workItem.getProcessInstanceId(), workItem.getId(), varname, newValue);
				} catch(Exception e){
					logOriginalException(e);
					throw WorkflowExceptionType.WF_CANNOT_UPDATE_WORKITEM_VARIABLES.createException(varname);
				}
			}
		}
		sendEvent(SharkEvent.ACTIVITY_MODIFIED, userCtx, handle, workItem);
	}

	public void checkIfRequiredVariablesCompleted(List<ActivityVariable> variables) {
		for(ActivityVariable var : variables) {
			if (var.type == WorkflowVariableType.REQUIRED && var.variable.isNull()) {
				throw WorkflowExceptionType.WF_WORKITEM_VARIABLES_REQUIRED.createException(var.variable.getSchema().getName());
			}
		}
	}

	public void completeWorkItem( WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem workItem) {
		try{
			factory.getWAPIConnection().changeWorkItemState(handle, workItem.getProcessInstanceId(), 
					workItem.getId(), WMWorkItemState.CLOSED_COMPLETED);
			sendEvent(SharkEvent.ACTIVITY_CLOSED, userCtx, handle, workItem);
		} catch(Exception e){
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_COMPLETE_WORKITEM.createException();
		}
	}
	
	public void abortProcess( WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem workItem) {
		try{
			Entry<WMEntity,CmdbuildProcessInfo> procInfo = this.getProcessInfo(handle, factory, workItem);
			if(procInfo.getValue().isUserStoppable()
					|| userCtx.privileges().isAdmin() ) {
				factory.getWAPIConnection().changeProcessInstanceState(handle, workItem.getProcessInstanceId(), WMProcessInstanceState.CLOSED_ABORTED);
				sendEvent(SharkEvent.PROCESS_ABORTED,userCtx,handle,workItem);
			} else {
				Log.WORKFLOW.debug("cant stop process: insufficient privileges.");
			}
		} catch(Exception e){
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_ABORT_PROCESS.createException();
		}
	}
	
	public void resumeProcess( WMSessionHandle handle, 
			UserContext userCtx,
			SharkWSFactory factory,
			String processInstanceId) {
		try {
			factory.getWAPIConnection().changeProcessInstanceState(handle, processInstanceId, WMProcessInstanceState.OPEN_RUNNING);
		} catch(Exception e) {
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_RESUME_WORKITEM.createException();
		}
	}
	
	public void suspendProcess( WMSessionHandle handle, 
			UserContext userCtx,
			SharkWSFactory factory,
			String processInstanceId) {
		try {
			factory.getWAPIConnection().changeProcessInstanceState(handle, processInstanceId, WMProcessInstanceState.OPEN_NOTRUNNING_SUSPENDED);
			sendEvent(SharkEvent.PROCESS_SUSPENDED,userCtx,handle,processInstanceId);
		} catch(Exception e) {
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_SUSPEND_PROCESS.createException();
		}
	}
	
	public String getActivityParticipant( WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem workItem) {
		try {
			Log.WORKFLOW.debug("getActivityParticipant called");
			Entry<WMEntity,CmdbuildProcessInfo> procInfo = this.getProcessInfo(handle, factory, workItem);
			Entry<WMEntity,CmdbuildActivityInfo> actInfo = this.getActivityInfo(handle, factory, workItem, procInfo.getKey());
			
			//get the XPDL Performer
			String perfOrId = actInfo.getValue().getParticipantIdOrExpression();
			
			//..and search it in the list of the process participants
			for (WMEntity participant : procInfo.getValue().getParticipants()) {
				if(participant.getId().equals(perfOrId)) {
					//if found, then return the name
					Log.WORKFLOW.debug("performer of " + actInfo.getValue().activityName + " group: " + perfOrId);
					return perfOrId;
				}
			}
			
			/*
			 * ok, this is tricky..I should evaluate the arbitrary expression here, on the client side..
			 * ..this means that one cannot use complex script or arbitrary workflow variables..
			 * Maybe a better choice would be to add a ws that evaluate a script in the context of a certain 
			 * activity...
			 */
			
			Log.WORKFLOW.debug("calling service to resolve participant name, expression: " + perfOrId);
			return WorkflowService.getInstance().resolveArbitraryPerformerExpression(
					workItem.getProcessInstanceId(), workItem.getActivityInstanceId(), 
					perfOrId);
			
		} catch(Exception e){
			logOriginalException(e);
			throw WorkflowExceptionType.WF_CANNOT_RESOLVE_PARTICIPANT.createException();
		}
	}
}

package org.cmdbuild.workflow.operation;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.WorkflowService;
import org.cmdbuild.services.WorkflowService.WorkflowOperation;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.management.ActivityIdentifier;
import org.cmdbuild.workflow.ActivityVariable;
import org.cmdbuild.workflow.CmdbuildActivityInfo;
import org.cmdbuild.workflow.CmdbuildProcessInfo;
import org.cmdbuild.workflow.SharkWSFacade;
import org.cmdbuild.workflow.WorkflowCache;
import org.cmdbuild.workflow.WorkflowConstants;
import org.cmdbuild.workflow.extattr.CmdbuildExtendedAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.api.client.wfservice.PackageAdministration;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SharkFacade {

	SharkWSFacade facade;
	UserContext userCtx;

	public SharkFacade(UserContext userCtx) {
		this.userCtx = userCtx;
		this.facade = new SharkWSFacade();
	}

	protected <T> T executeAdmin( WorkflowService.WorkflowOperation<T> operation ) {
		return execute(operation,true);
	}
	protected <T> T execute( WorkflowService.WorkflowOperation<T> operation ) {
		return execute(operation,false);
	}
	protected <T> T execute( WorkflowService.WorkflowOperation<T> operation, boolean adminConnection ) {
		try{
			return WorkflowService.getInstance().execute(operation, userCtx, adminConnection);
		} catch(CMDBWorkflowException e){
			Log.WORKFLOW.error("error executing an action (cmdb) - " + e.getExceptionTypeText(), e);
			throw e;
		} catch(Exception e){
			Log.WORKFLOW.error("error executing an action", e);
			throw WorkflowExceptionType.WF_GENERIC_ERROR.createException();
		}
	}

	private abstract class AbstractUpdateActivityOperation implements WorkflowOperation<Boolean> {
		private final String processInstanceId;
		private final String workItemId;
		private final boolean complete;
		private final Map<String,String> params;
		
		protected AbstractUpdateActivityOperation(final String processInstanceId, final String workItemId,
				final Map<String,String> params, final boolean complete) {
			this.processInstanceId = processInstanceId;
			this.workItemId = workItemId;
			this.complete = complete;
			this.params = params;
		}

		public Boolean execute(WMSessionHandle handle,
				SharkWSFactory factory, UserContext userCtx)
				throws Exception {
			Log.WORKFLOW.info("Updating workitem "+ workItemId);
			WMWorkItem item = facade.getWorkItem(handle, factory, processInstanceId, workItemId);
			if (item == null) {
				Log.WORKFLOW.error("Can't get workitem " + workItemId);
				throw WorkflowExceptionType.WF_CANNOT_GET_WORKITEM.createException(workItemId);
			}

			facade.checkPrivilegesOnWorkItem(item, userCtx, handle);

			// NdPaolo: WHY?! you should not change suspended processes!
			facade.resumeWorkItemIfSuspended(handle, userCtx, factory, item);

			Entry<WMEntity,CmdbuildProcessInfo> procInfo = facade.getProcessInfo(handle, factory, item);
			CmdbuildActivityInfo actInfo = facade.getActivityInfo(handle, factory, item, procInfo.getKey()).getValue();
			List<ActivityVariable> variables = getVariables(handle, userCtx, factory, item);
			int cardId = facade.getCmdbuildCardId(handle, userCtx, factory, item);
			ActivityDO activity = new ActivityDO(procInfo.getValue(),actInfo,variables,
					item.getProcessInstanceId(),item.getId(),cardId,true);

			ICard card = userCtx.tables().get(procInfo.getValue().getCmdbuildBindedClass()).cards().get(cardId);
			updateWFCard(params, card, actInfo);

			if (!this.params.isEmpty()) {
				activity.updateVariables(params);
				facade.updateWorkItemValues(handle, userCtx, factory, item, activity.variables);
			}
			if (complete) {
				facade.checkIfRequiredVariablesCompleted(activity.variables);
				facade.setWorkItemToRunning(handle, userCtx, factory, item);
				facade.completeWorkItem(handle, userCtx, factory, item);
			}
			return true;
		}

		protected abstract List<ActivityVariable> getVariables(WMSessionHandle handle, UserContext userCtx,
				SharkWSFactory factory, WMWorkItem item);

		/*
		 * when called on save only, advance process was not updating the notes
		 */
		private void updateWFCard(final Map<String, String> params, ICard card, CmdbuildActivityInfo activity) {
			//save attributes in cmdbuild also
			for (IAttribute attribute : card.getSchema().getAttributes().values()) {
				if (!attribute.isDisplayable())
					continue;
				String attrName = attribute.getName();
				String attrNewValue = params.get(attrName);
				if(null != attrNewValue){
					card.getAttributeValue(attrName).setValue(attrNewValue);
				}
			}
			card.getAttributeValue(ICard.CardAttributes.Code.toString()).setValue(activity.getActivityName());
			if (complete) {
				card.forceSave();
			} else {
				card.save();
			}
		}
	}

	public ActivityDO startActivityTemplate( final String cmdbuildBindedClass ) {
		ActivityDO out = null;
		Entry<WMEntity,CmdbuildProcessInfo> proc = WorkflowCache.getInstance().getProcessInfoFromBindedClass(cmdbuildBindedClass);
		
		String actId = proc.getValue().getInitialActivityIdFor(userCtx);
		if(actId == null) {
			throw WorkflowExceptionType.WF_CANNOT_START.createException();
		}
		Entry<WMEntity,CmdbuildActivityInfo> act = WorkflowCache.getInstance().getActivityInfo(actId, proc.getKey());
		
		CmdbuildActivityInfo actInfo = act.getValue();
		ITable schema = UserContext.systemContext().tables().get( cmdbuildBindedClass );
		
		List<ActivityVariable> emptyVars = actInfo.getVariableInstances(schema);
		
		out = new ActivityDO(proc.getValue(),actInfo,emptyVars,WorkflowConstants.ProcessToStartId,null,-1,true);
		out.setPerformer( actInfo.getParticipantIdOrExpression() );
		out.configureExtendedAttributes(null, userCtx, null, null);
		
		return out;
	}
	
	/**
	 * Start the latest version of the process associated with the given cmdbuildbindedClass
	 * @param cmdbuildBindedClass
	 * @return
	 * @throws Exception
	 */
	public ActivityDO startProcess(final String cmdbuildBindedClass) {
		WorkflowOperation<ActivityDO> operation = new WorkflowOperation<ActivityDO>(){
			public ActivityDO execute(WMSessionHandle handle,
					SharkWSFactory factory, UserContext userCtx)
					throws Exception {
				
				String procInstId = facade.createAndStartProcess(handle, userCtx, factory, cmdbuildBindedClass);
				List<WMWorkItem> items = facade.getAllWorkItems(handle, userCtx, factory, procInstId);
				WMWorkItem item = facade.filterForCurrentGroup(handle, factory, userCtx, procInstId, cmdbuildBindedClass, items);

				List<ActivityVariable> variables = facade.getWorkItemVariables(handle, userCtx, factory, item);
				Entry<WMEntity,CmdbuildProcessInfo> procInfo = facade.getProcessInfo(handle, factory, item);
				Entry<WMEntity,CmdbuildActivityInfo> actInfo = facade.getActivityInfo(handle, factory, item, procInfo.getKey());

				ActivityDO out = new ActivityDO(procInfo.getValue(),actInfo.getValue(),variables,
						procInstId,item.getId(),facade.getCmdbuildCardId(handle, userCtx, factory, item),
						facade.isEditable(userCtx,item,handle));
				out.configureExtendedAttributes(handle, userCtx, factory, item);
				return out;
			}
		};
		
		return execute(operation);
	}
	
	public ActivityDO getActivityList(ICard card) {
		List<ICard> cardList = new LinkedList<ICard>();
		cardList.add(card);
		List<ActivityDO> activityList = getActivityList(card.getSchema().getName(),
				cardList, false);
		try {
			return activityList.iterator().next();
		} catch (NoSuchElementException e) {
			throw WorkflowExceptionType.WF_CANNOT_GET_WORKITEM.createException();
		}
	}

	public Map<Integer, ActivityDO> getActivityMap(final ITable table, final List<ICard> cards) {
		// The web service does not know what flow status was requested
		return getActivityMap(table, cards, null);
	}

	public Map<Integer, ActivityDO> getActivityMap(final ITable table, final List<ICard> cards, final String flowStatus) {
		final Map<Integer, ActivityDO> activityMap = new HashMap<Integer, ActivityDO>();

		if (table.isActivity()) {
			List<ActivityDO> acts;
			if (flowStatus == null || flowStatus.startsWith(WorkflowConstants.StateOpen) || WorkflowConstants.AllState.equals(flowStatus)) {
				acts = getActivityList(table.getName(), cards, false);
			} else {
				acts = new ArrayList<ActivityDO>();
			}
			
			
			for (ActivityDO activity : acts) {
				if (activity == null) {
					Log.WORKFLOW.warn("a process was not found!");
				} else {
					activityMap.put(activity.getCmdbuildCardId(), activity);
				}
			}
		}

		return activityMap;
	}

	public List<ActivityDO> getActivityList(final String className, final List<ICard> cmdbuildCards, final boolean onlyExecutables) {
		WorkflowOperation<List<ActivityDO>> operation = new WorkflowOperation<List<ActivityDO>>() {

			public List<ActivityDO> execute(WMSessionHandle handle,
					SharkWSFactory factory, UserContext userCtx)
					throws Exception {

				List<ActivityDO> out = new ArrayList<ActivityDO>(cmdbuildCards.size());
				for (int i=0;i<cmdbuildCards.size();i++) {
					out.add(null);
				}

				List<String> procInstIds = new ArrayList<String>(cmdbuildCards.size());
				for (ICard crd : cmdbuildCards) {
					procInstIds.add((String)crd.getValue("ProcessCode"));
				}

				WorkItemQuery query = new WorkItemQuery();
				try {
				    query.setPackageId(WorkflowCache.getInstance().getProcessInfoFromBindedClass(className).getKey().getPkgId());
                } catch (CMDBWorkflowException e) {
                    // TODO: quick fix not to crash for activity superclasses (should be handled differently)
                    if (e.getExceptionType() != WorkflowExceptionType.WF_PROCESSINFO_NOT_FOUND)
                        throw e;
                }
				query.setSharkFacade(facade);
				query.setProcInstIds(procInstIds);
				WMWorkItem[] items = query.filter(handle, userCtx, factory);
				
				for (WMWorkItem item : items) {
					int cmdbId = facade.getCmdbuildCardId(handle, userCtx, factory, item);
					ICard crd = null;
					for(ICard card : cmdbuildCards){
						if(card.getId() == cmdbId){
							Log.WORKFLOW.debug("found card for " + cmdbId);
							crd = card; break;
						}
					}
					Entry<WMEntity,CmdbuildProcessInfo> procInfo = facade.getProcessInfo(handle, factory, item);
					Entry<WMEntity,CmdbuildActivityInfo> actInfo = facade.getActivityInfo(handle, factory, item, procInfo.getKey());

					if(crd == null) {
						Log.WORKFLOW.error("Card not found for " + cmdbId);
						continue;
					}
					if(actInfo == null) {
						Log.WORKFLOW.error("ActivityInfo not found for " + item.getActivityDefinitionId());
						continue;
					}
					int index = cmdbuildCards.indexOf(crd);

					List<ActivityVariable> variables = actInfo.getValue().getVariableInstances(crd.getSchema());

// NdPaolo: I don't know what this piece of code did, but it does not work! (value is always null!)
// Awaiting large worklow refactoring
//					for (ActivityVariable av : variables) {
//						String name = av.getName();
//						Object value = av.getValue();
//						crd.setValue(name, value);
//					}

					ActivityDO activity = new ActivityDO(
							procInfo.getValue(),actInfo.getValue(),
							variables,item.getProcessInstanceId(),item.getId(),
							cmdbId,
							facade.isEditable(userCtx,item,handle));
					activity.setCmdbuildCardNotes(crd.getNotes());
					activity.setCmdbuildClassId(crd.getIdClass());
					activity.configureExtendedAttributes(handle, userCtx, factory, item);
					activity.setPerformer( facade.getActivityParticipant(handle, userCtx, factory, item) );

					out.set(index, activity);
				}
				for(int i=0;i<cmdbuildCards.size();i++) {
					if(null == out.get(i)) {
						ICard crd = cmdbuildCards.get(i);
						Log.WORKFLOW.warn("the process " + crd.getValue("ProcessCode") + " in card " + crd.getId() + " was not found in Shark!");
					}
				}
				return out;
			}
		};
		return execute(operation);
	}
	
	public List<ActivityDO> getWorkItems( final WorkItemQuery query, final boolean onlyBaseDisplay ) {
		WorkflowOperation<List<ActivityDO>> operation = new WorkflowOperation<List<ActivityDO>>(){

			public List<ActivityDO> execute(WMSessionHandle handle,
					SharkWSFactory factory, UserContext userCtx)
					throws Exception {
				WMWorkItem[] items = query.filter(handle, userCtx, factory);

				if(userCtx.privileges().isAdmin()) {
					//one workitem per activityinstanceid
					List<WMWorkItem> tmp = new ArrayList<WMWorkItem>();
					for(WMWorkItem item : items){
						boolean found = false;
						for(WMWorkItem itemTmp : tmp){
							if(itemTmp.getActivityInstanceId().equals(item.getActivityInstanceId())){
								found = true; break;
							}
						}
						if(!found){
							tmp.add(item);
						}
					}
					
					items = tmp.toArray(new WMWorkItem[]{});
				}
				
				List<ActivityDO> out = new ArrayList<ActivityDO>();
				
				for(WMWorkItem item : items){
					Entry<WMEntity,CmdbuildProcessInfo> procInfo = facade.getProcessInfo(handle, factory, item);
					Entry<WMEntity,CmdbuildActivityInfo> actInfo = facade.getActivityInfo(handle, factory, item, procInfo.getKey());

					List<ActivityVariable> variables = facade.getProcessWorkItemVariables(handle, userCtx, factory, item);
					
					ActivityDO activity = new ActivityDO(
							procInfo.getValue(),actInfo.getValue(),
							variables,item.getProcessInstanceId(),item.getId(),
							facade.getCmdbuildCardId(handle, userCtx, factory, item),
							facade.isEditable(userCtx,item,handle));
					activity.configureExtendedAttributes(handle, userCtx, factory, item);
					out.add(activity);
				}
				
				return out;
			}
		};
		return execute(operation);
	}

	public boolean updateActivity(final ActivityIdentifier ai,
			final Map<String,String> params, final boolean complete) {
		return updateActivity(ai.getProcessInstanceId(), ai.getWorkItemId(), params, complete);
	}

	public boolean updateActivity(final String processInstanceId, final String workItemId,
			final Map<String,String> params, final boolean complete) {
		WorkflowOperation<Boolean> operation = new AbstractUpdateActivityOperation(processInstanceId, workItemId, params, complete){
			protected List<ActivityVariable> getVariables(WMSessionHandle handle, UserContext userCtx,
					SharkWSFactory factory, WMWorkItem item) {
				return facade.getWorkItemVariables(handle, userCtx, factory, item);
			}
		};
		return execute(operation);
	}

	/*
	 * Used by the web services because shark needs to update variables not in the work item!
	 */
	public boolean generalUpdateActivity(final String processInstanceId, final String workItemId,
			final Map<String,String> params, final boolean complete) {
		WorkflowOperation<Boolean> operation = new AbstractUpdateActivityOperation(processInstanceId, workItemId, params, complete){
			protected List<ActivityVariable> getVariables(WMSessionHandle handle, UserContext userCtx,
					SharkWSFactory factory, WMWorkItem item) {
				return facade.getProcessVariables(handle, userCtx, factory, item.getProcessInstanceId()); 
			}
		};
		return execute(operation);
	}

	public boolean saveWorkflowWidget(final ActivityIdentifier ai, final String identifier,
			final Map<String, String[]> values, boolean advance) {
		return reactToExtendedAttributeSubmission(ai.getProcessInstanceId(), ai.getWorkItemId(), values, identifier, advance);
	}

	public boolean reactToExtendedAttributeSubmission(final String processInstanceId, final String workItemId,
			final Map<String, String[]> submissionParameters, final String extAttrIdentifier, final boolean advance) {
		WorkflowOperation<Boolean> operation = new WorkflowOperation<Boolean>() {
			public Boolean execute(WMSessionHandle handle,
					SharkWSFactory factory, UserContext userCtx)
					throws Exception {
				
				WMWorkItem item = facade.getWorkItem(handle, factory, processInstanceId, workItemId);
				List<ActivityVariable> variables = facade.getWorkItemVariables(handle, userCtx, factory, item);
				Entry<WMEntity,CmdbuildProcessInfo> procInfo = facade.getProcessInfo(handle, factory, item);
				Entry<WMEntity,CmdbuildActivityInfo> actInfo = facade.getActivityInfo(handle, factory, item, procInfo.getKey());
				
				ActivityDO activity = new ActivityDO(procInfo.getValue(),actInfo.getValue(),variables,
						item.getProcessInstanceId(),item.getId(),facade.getCmdbuildCardId(handle, userCtx, factory, item),false);
				activity.configureExtendedAttributes(handle, userCtx, factory, item);
				for( CmdbuildExtendedAttribute cmdbExtAttr : activity.cmdbExtAttrs ) {
					if(cmdbExtAttr.identifier().equals(extAttrIdentifier)) {
						cmdbExtAttr.react(handle, userCtx, factory, facade, item, activity, submissionParameters, advance);
						return true;
					}
				}
				
				return false;
			}
		};
		return execute(operation);
	}

	public boolean abortProcess( final String processInstanceId, final String workItemId ) {
		WorkflowOperation<Boolean> operation = new WorkflowOperation<Boolean>(){
			public Boolean execute(WMSessionHandle handle,
					SharkWSFactory factory, UserContext userCtx)
					throws Exception {
				WMWorkItem item = facade.getWorkItem(handle, factory, processInstanceId, workItemId);
				facade.abortProcess(handle, userCtx, factory, item);
				return true;
			}
		};
		return execute(operation);
	}
	
	public boolean suspendProcess( final String processInstanceId ) {
		WorkflowOperation<Boolean> operation = new WorkflowOperation<Boolean>() {
			public Boolean execute(WMSessionHandle handle,
					SharkWSFactory factory, UserContext userCtx)
					throws Exception {
				facade.suspendProcess(handle, userCtx, factory, processInstanceId);
				return true;
			}
		};
		return execute(operation);
	}

	public boolean resumeProcess( final String processInstanceId ) {
		WorkflowOperation<Boolean> operation = new WorkflowOperation<Boolean>() {
			public Boolean execute(WMSessionHandle handle,
					SharkWSFactory factory, UserContext userCtx)
					throws Exception {
				facade.resumeProcess(handle, userCtx, factory, processInstanceId);
				return true;
			}
		};
		return execute(operation);
	}
	
	public void removeAllInconsistentProcesses() {
		for(String cn : WorkflowCache.getInstance().getBindedClasses()) {
			removeInconsistentProcesses(cn);
		}
	}

	public void removeInconsistentProcesses(ITable table) {
		removeInconsistentProcesses(table.getName());
	}

	private void removeInconsistentProcesses(String className) {
		Log.WORKFLOW.debug("remove inconsistent processes for " + className);
		final ITable table = UserContext.systemContext().tables().get(className);
		final Lookup statusLookup = WorkflowService.getInstance().getStatusLookupFor(WorkflowConstants.StateOpenRunning);

		final WorkItemQuery query = new WorkItemQuery();
		query.setSharkFacade(facade);
		query.setPackageId(WorkflowCache.getInstance().getProcessInfoFromBindedClass(className).getKey().getPkgId());

		WorkflowOperation<Void> operation = new WorkflowOperation<Void>(){
			public Void execute(WMSessionHandle handle,
					SharkWSFactory factory, UserContext userCtx) throws Exception {

					WMWorkItem[] items = query.filter(handle, userCtx, factory);
					Set<Integer> cmdbIds = new HashSet<Integer>();
					for(WMWorkItem item : items) {
						cmdbIds.add(facade.getCmdbuildCardId(handle, userCtx, factory, item));
					}
					for(ICard crd : table.cards().list().filter(ProcessAttributes.FlowStatus.toString(), AttributeFilterType.EQUALS, statusLookup.getId()+"")){
						if(!cmdbIds.contains(crd.getId())) {
							Log.WORKFLOW.debug("found inconsistent card: " + crd.getId());
							// TODO: bulk delete?
							crd.delete();
						}
					}
				return null;
			}
		};
		executeAdmin(operation);
	}
	
	private class ByteArr {
		byte[] arr;
	}

	public byte[] downloadXPDL( String className, int version ) {
		
		final CmdbuildProcessInfo procInfo = WorkflowCache.getInstance().getProcessInfo(className, version).getValue();
		
		WorkflowOperation<ByteArr> operation = new WorkflowOperation<ByteArr>() {
			public ByteArr execute(WMSessionHandle handle,
					SharkWSFactory factory, UserContext userCtx)
					throws Exception {
				
				PackageAdministration padm = factory.getPackageAdministration();
				
				byte[] arr = padm.getPackageContent(handle, procInfo.getPackageId(), procInfo.getPackageVersion());
				ByteArr barr = new ByteArr();
				barr.arr = arr;
				return barr;
			}
		};
		return executeAdmin(operation).arr;
	}
	
	public void uploadUpdateXPDL( InputStream is,String className,boolean userStoppable ) throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		
		boolean foundProcClassName = false;
		boolean foundUserStop = false;
		NodeList nl = doc.getElementsByTagName("ExtendedAttribute");
		for(int i=0;i<nl.getLength();i++) {
			Node node = nl.item(i);
			String name = node.getAttributes().getNamedItem("Name").getNodeValue();
			if(null != node.getAttributes().getNamedItem("Value")) {
				String value = node.getAttributes().getNamedItem("Value").getNodeValue();
				if(name.equals("cmdbuildBindToClass")) {
					foundProcClassName = true;
					if(!(className.equals(value))) {
						throw WorkflowExceptionType.WF_WRONG_XPDL_CLASSNAME.createException();
					}
				}
				if(name.equals("userStoppable")) {
					foundUserStop = true;
					boolean theValue = Boolean.parseBoolean(value);
					if(theValue != userStoppable) {
						node.getAttributes().getNamedItem("Value").setNodeValue(userStoppable+"");
					}
				}
			}
		}
		if(!foundProcClassName) {
			throw WorkflowExceptionType.WF_XPDL_CLASSNAME_MISSING.createException();
		}
		if(!foundUserStop) {
			throw WorkflowExceptionType.WF_XPDL_USERSTOP_MISSING.createException();
		}
		
		DOMSource source = new DOMSource(doc);
		StringWriter xmlAsWriter = new StringWriter();
		StreamResult result = new StreamResult(xmlAsWriter);
		TransformerFactory.newInstance().newTransformer().transform(source, result);
		
		final String packId = doc.getDocumentElement().getAttribute("Id");
		final byte[] docBytes = xmlAsWriter.toString().getBytes();
		WorkflowOperation<Void> operation = new WorkflowOperation<Void>(){
			public Void execute(WMSessionHandle handle,
					SharkWSFactory factory, UserContext userCtx)
					throws Exception {
				PackageAdministration pa = factory.getPackageAdministration();
				try{
					String ver = pa.getCurrentPackageVersion(handle, packId);
					Log.WORKFLOW.info("Updating current package version: " + ver);
					pa.updatePackage(handle, packId, docBytes);
				} catch (Exception exp) {
					if("org.enhydra.shark.api.client.wfservice.PackageInvalid: Error in package".equals(exp.getMessage())) {
						Log.WORKFLOW.error("Errors in package!", exp);
						throw WorkflowExceptionType.WF_PACKAGE_ERROR.createException();
					} else {
						try{
							pa.uploadPackage(handle, docBytes);
						} catch(Exception exp1) {
							Log.WORKFLOW.error("Cannot upload package!", exp1);
							throw WorkflowExceptionType.WF_CANNOT_UPLOAD_PACKAGE.createException();
						}
					}
				}
				return null;
			}
		};
		executeAdmin(operation);
		WorkflowService.getInstance().reloadCache();
	}
}

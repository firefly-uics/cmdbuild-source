package org.cmdbuild.workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.legacywrappers.ProcessClassWrapper;
import org.cmdbuild.dao.legacywrappers.ProcessInstanceWrapper;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CMProcessInstance.CMProcessInstanceDefinition;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Wrapper for the CMWorkflowEngine on top of the legacy UserContext and
 * a load of Wrappers from the legacy DAO later to the new interfaces.
 */
public class WorkflowEngineWrapper implements ContaminatedWorkflowEngine {

	private static final CMWorkflowEngineListener NULL_EVENT_LISTENER = new NullWorkflowEngineListener();

	private final UserContext userCtx;
	private final CMWorkflowService workflowService;
	private final ProcessDefinitionManager processDefinitionManager;
	private CMWorkflowEngineListener eventListener;

	public WorkflowEngineWrapper(final UserContext userCtx, final CMWorkflowService workflowService,
			final ProcessDefinitionManager processDefinitionManager) {
		this.userCtx = userCtx;
		this.workflowService = workflowService;
		this.processDefinitionManager = processDefinitionManager;
		this.eventListener = NULL_EVENT_LISTENER;
	}

	@Override
	public CMProcessClass findProcessClass(Object idOrName) {
		Validate.notNull(idOrName);
		final ProcessType processType = findProcessType(idOrName);
		return wrap(processType);
	}

	@Override
	public CMProcessClass findProcessClassById(Object id) {
		Validate.notNull(id);
		final ProcessType processType = findProcessTypeById(id);
		return wrap(processType);
	}

	@Override
	public CMProcessClass findProcessClassByName(String name) {
		Validate.notNull(name);
		final ProcessType processType = findProcessTypeByName(name);
		return wrap(processType);
	}

	private ProcessType findProcessType(final Object idOrName) {
		if (idOrName instanceof String) {
			final String name = (String) idOrName;
			return findProcessTypeByName(name);
		} else {
			return findProcessTypeById(idOrName);
		}
	}

	private ProcessType findProcessTypeById(Object idObject) {
		final int id = ((Number) idObject).intValue();
		return userCtx.processTypes().get(id);
	}

	private ProcessType findProcessTypeByName(String name) {
		return userCtx.processTypes().get(name);
	}

	private CMProcessClass wrap(final ProcessType processType) {
		return new ProcessClassWrapper(userCtx, processType, processDefinitionManager);
	}

	private CMProcessInstance wrap(final ICard processCard) {
		return new ProcessInstanceWrapper(userCtx, processDefinitionManager, processCard);
	}
	
	@Override
	public Iterable<? extends CMProcessClass> findProcessClasses() {
		return Iterables.filter(findAllProcessClasses(), new Predicate<CMProcessClass>() {

			@Override
			public boolean apply(CMProcessClass input) {
				return input.isActive();
			}

		});
	}

	@Override
	public Iterable<? extends CMProcessClass> findAllProcessClasses() {
		return Iterables.transform(userCtx.processTypes().list(), new Function<ProcessType, CMProcessClass>() {

			@Override
			public CMProcessClass apply(ProcessType input) {
				return wrap(input);
			}

		});
	}

	@Override
	public CMProcessInstance startProcess(final CMProcessClass processClass) throws CMWorkflowException {
		final CMActivity startActivity = processClass.getStartActivity();
		if (startActivity == null) {
			return null;
		}
		final WSProcessInstInfo procInst = workflowService.startProcess(processClass.getPackageId(),
				processClass.getProcessDefinitionId());
		WSActivityInstInfo startActInstInfo = keepOnlyStartingActivityInstance(startActivity.getId(), procInst.getProcessInstanceId());
		
		CMProcessInstanceDefinition proc = newProcessInstance(processClass, procInst);
		proc.addActivity(startActInstInfo);
		return proc.save();
	}

	private WSActivityInstInfo keepOnlyStartingActivityInstance(final String startActivityId, final String procInstId)
			throws CMWorkflowException {
		WSActivityInstInfo startActInstInfo = null;
		final WSActivityInstInfo[] ais = workflowService.findOpenActivitiesForProcessInstance(procInstId);
		for (int i = 0; i < ais.length; ++i) {
			final String actDefId = ais[i].getActivityDefinitionId();
			if (startActivityId.equals(actDefId)) {
				startActInstInfo = ais[i];
			} else {
				workflowService.abortActivityInstance(procInstId, actDefId);
			}
		}
		return startActInstInfo;
	}

	private CMProcessInstanceDefinition newProcessInstance(final CMProcessClass processDefinition, final WSProcessInstInfo procInst) {
		return ProcessInstanceWrapper.createProcessInstance(userCtx,
				processDefinitionManager,
				findProcessTypeById(processDefinition.getId()),
				procInst);
	}

	@Override
	public void updateActivity(final CMActivityInstance activityInstance, final Map<String, Object> inputValues)
			throws CMWorkflowException {
		final Map<String, Object> nativeValues = new HashMap<String, Object>(inputValues.size());
		final CMProcessInstance procInst = activityInstance.getProcessInstance();
		final CMProcessInstanceDefinition procInstDef = modifyProcessInstance(procInst);
		for (final String key : inputValues.keySet()) {
			final Object value = inputValues.get(key);
			procInstDef.set(key, value);
			nativeValues.put(key, procInstDef.get(key));
		}
		// TODO Widgets: execute update and save output to nativeValues
		// Values should be native to CMDBuild, not to Shark
		workflowService.setProcessInstanceVariables(procInst.getProcessInstanceId(), nativeValues);
		procInstDef.save();
	}

	private CMProcessInstanceDefinition modifyProcessInstance(final CMProcessInstance processInstance) {
		return ProcessInstanceWrapper.readProcessInstance(userCtx,
				processDefinitionManager,
				findProcessTypeById(processInstance.getType().getId()),
				processInstance);
	}

	@Override
	public CMProcessInstance advanceActivity(final CMActivityInstance activityInstance) throws CMWorkflowException {
		// TODO Widgets: tell them that you are advancing the activity (send emails, ...)
		throw new UnsupportedOperationException("TODO Come on! Cut me some slack!");
	}

	/**
	 * It should extract CMProcessClass with findAllProcessClasses() but the
	 * new DAO is not here yet. If it wasn't for SQL, we would breathe hacks.
	 * @throws CMWorkflowException 
	 */
	@Override
	public void sync() throws CMWorkflowException {
		eventListener.syncStarted();
		userCtx.privileges().assureAdminPrivilege();
		for (final ProcessType processType : userCtx.processTypes().list()) {
			if (processType.isSuperClass())
				continue;
			syncProcess(processType);
		}
		eventListener.syncFinished();
	}

	private void syncProcess(final ProcessType processType) throws CMWorkflowException {
		final CMProcessClass processClass = wrap(processType);
		eventListener.syncProcessStarted(processClass);
		final Map<String, WSProcessInstInfo> wsInfo = queryWSOpenAndSuspended(processClass);
		final Iterable<CMProcessInstance> activeProcessInstances = queryDBOpenAndSuspended(processType);
		for (final CMProcessInstance processInstance : activeProcessInstances) {
			final String processInstanceId = processInstance.getProcessInstanceId();
			final WSProcessInstInfo processInstanceInfo = wsInfo.get(processInstanceId);
			if (processInstanceInfo == null) {
				eventListener.syncProcessInstanceNotFound(processInstance);
				removeOutOfSyncProcess(processInstance);
			} else {
				eventListener.syncProcessInstanceFound(processInstance);
				syncProcessStateAndActivities(processInstance, processInstanceInfo);
			}
		}
	}

	private Map<String, WSProcessInstInfo> queryWSOpenAndSuspended(final CMProcessClass processClass) throws CMWorkflowException {
		final Map<String, WSProcessInstInfo> wsInfo = new HashMap<String, WSProcessInstInfo>();
		for (WSProcessInstInfo pis : workflowService.listOpenProcessInstances(processClass.getProcessDefinitionId())) {
			wsInfo.put(pis.getProcessInstanceId(), pis);
		}
		return wsInfo;
	}

	@Legacy("Old DAO")
	private Iterable<CMProcessInstance> queryDBOpenAndSuspended(final ProcessType processType) {
		final int openFlowStatusId = ProcessInstanceWrapper.lookupForFlowStatus(WSProcessInstanceState.OPEN).getId();
		final int suspendedFlowStatusId = ProcessInstanceWrapper.lookupForFlowStatus(WSProcessInstanceState.SUSPENDED).getId();
		final CardQuery cardQuery = processType.cards().list()
				.filterUpdate(
						ProcessAttributes.FlowStatus.dbColumnName(),
						AttributeFilterType.EQUALS,
						openFlowStatusId, suspendedFlowStatusId);
		return query(cardQuery);
	}

	@Legacy("Old DAO")
	public Iterable<CMProcessInstance> query(final CardQuery cardQuery) {
		return Iterables.transform(cardQuery, new Function<ICard, CMProcessInstance>() {

			@Override
			public CMProcessInstance apply(ICard input) {
				return wrap(input);
			}

		});
	}

	private void removeOutOfSyncProcess(CMProcessInstance processInstance) {
		final CMProcessInstanceDefinition editableProcessInstance = modifyProcessInstance(processInstance);
		editableProcessInstance.setState(WSProcessInstanceState.UNSUPPORTED).save();
	}

	private void syncProcessStateAndActivities(CMProcessInstance processInstance, final WSProcessInstInfo processInstanceInfo) throws CMWorkflowException {
		final CMProcessInstanceDefinition editableProcessInstance = modifyProcessInstance(processInstance);
		editableProcessInstance.setState(processInstanceInfo.getStatus());
		editableProcessInstance.setUniqueProcessDefinition(processInstanceInfo);
		final WSActivityInstInfo[] activities = workflowService.findOpenActivitiesForProcessInstance(processInstance.getProcessInstanceId());
		editableProcessInstance.setActivities(activities);
		editableProcessInstance.save();
	}

	@Override
	public void setEventListener(final CMWorkflowEngineListener eventListener) {
		this.eventListener = eventListener;
	}
}

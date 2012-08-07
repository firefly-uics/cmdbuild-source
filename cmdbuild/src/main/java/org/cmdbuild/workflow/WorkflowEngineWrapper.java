package org.cmdbuild.workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.legacywrappers.ProcessClassWrapper;
import org.cmdbuild.dao.legacywrappers.ProcessInstanceWrapper;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.Process;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.services.auth.User;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CMProcessInstance.CMProcessInstanceDefinition;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.user.UserProcessInstance.UserProcessInstanceDefinition;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Wrapper for the CMWorkflowEngine on top of the legacy UserContext and a load
 * of Wrappers from the legacy DAO later to the new interfaces.
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
	public UserProcessClass findProcessClassById(final Long id) {
		Validate.notNull(id);
		final ProcessType processType = findProcessTypeById(id);
		return wrap(processType);
	}

	@Override
	public UserProcessClass findProcessClassByName(final String name) {
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

	private ProcessType findProcessTypeById(final Object idObject) {
		final int id = ((Number) idObject).intValue();
		return userCtx.processTypes().get(id);
	}

	private ProcessType findProcessTypeByName(final String name) {
		return userCtx.processTypes().get(name);
	}

	private UserProcessClass wrap(final ProcessType processType) {
		return new ProcessClassWrapper(userCtx, processType, processDefinitionManager);
	}

	private UserProcessInstance wrap(final ICard processCard) {
		return new ProcessInstanceWrapper(userCtx, processDefinitionManager, processCard);
	}

	@Override
	public Iterable<UserProcessClass> findProcessClasses() {
		return Iterables.filter(findAllProcessClasses(), new Predicate<UserProcessClass>() {

			@Override
			public boolean apply(final UserProcessClass input) {
				return input.isActive();
			}

		});
	}

	@Override
	public Iterable<UserProcessClass> findAllProcessClasses() {
		return Iterables.transform(userCtx.processTypes().list(), new Function<ProcessType, UserProcessClass>() {

			@Override
			public UserProcessClass apply(final ProcessType input) {
				return wrap(input);
			}

		});
	}

	@Override
	public UserProcessInstance startProcess(final CMProcessClass processClass) throws CMWorkflowException {
		final CMActivity startActivity = processClass.getStartActivity();
		if (startActivity == null) {
			return null;
		}
		final WSProcessInstInfo procInst = workflowService.startProcess(processClass.getPackageId(),
				processClass.getProcessDefinitionId());
		final WSActivityInstInfo startActInstInfo = keepOnlyStartingActivityInstance(startActivity.getId(),
				procInst.getProcessInstanceId());

		final UserProcessInstanceDefinition proc = newProcessInstance(processClass, procInst);
		proc.addActivity(startActInstInfo);
		return proc.save();
	}

	private WSActivityInstInfo keepOnlyStartingActivityInstance(final String startActivityId, final String procInstId)
			throws CMWorkflowException {
		WSActivityInstInfo startActInstInfo = null;
		final WSActivityInstInfo[] ais = workflowService.findOpenActivitiesForProcessInstance(procInstId);
		for (int i = 0; i < ais.length; ++i) {
			final WSActivityInstInfo ai = ais[i];
			final String actDefId = ai.getActivityDefinitionId();
			if (startActivityId.equals(actDefId)) {
				startActInstInfo = ai;
			} else {
				final String actInstId = ai.getActivityInstanceId();
				workflowService.abortActivityInstance(procInstId, actInstId);
			}
		}
		return startActInstInfo;
	}

	private UserProcessInstanceDefinition newProcessInstance(final CMProcessClass processDefinition,
			final WSProcessInstInfo procInst) {
		return ProcessInstanceWrapper.createProcessInstance(userCtx, processDefinitionManager,
				findProcessTypeById(processDefinition.getId()), procInst);
	}

	@Override
	public void abortProcessInstance(final CMProcessInstance processInstance) throws CMWorkflowException {
		workflowService.abortProcessInstance(processInstance.getProcessInstanceId());
		modifyProcessInstance(processInstance).setState(WSProcessInstanceState.ABORTED).save();
	}

	@Override
	public void updateActivity(final CMActivityInstance activityInstance, final Map<String, Object> inputValues,
			final Map<String, Object> widgetSubmission) throws CMWorkflowException {
		final Map<String, Object> nativeValues = new HashMap<String, Object>(inputValues.size());
		final CMProcessInstance procInst = activityInstance.getProcessInstance();
		final CMProcessInstanceDefinition procInstDef = modifyProcessInstance(procInst);
		for (final String key : inputValues.keySet()) {
			final Object value = inputValues.get(key);
			procInstDef.set(key, value);
			nativeValues.put(key, procInstDef.get(key));
		}
		saveWidgets(activityInstance, widgetSubmission, nativeValues);
		fillCustomProcessVariables(activityInstance, nativeValues);
		workflowService.setProcessInstanceVariables(procInst.getProcessInstanceId(), nativeValues);
		procInstDef.save();
	}

	private void fillCustomProcessVariables(final CMActivityInstance activityInstance,
			final Map<String, Object> nativeValues) {
		nativeValues.put(Constants.CURRENT_USER_VARIABLE, currentUserReference());
		nativeValues.put(Constants.CURRENT_GROUP_VARIABLE, currentGroupReference(activityInstance));
	}

	private CardReference currentUserReference() {
		final User currentUser = userCtx.getUser();
		return CardReference.newInstance(UserCard.USER_CLASS_NAME, Long.valueOf(currentUser.getId()),
				currentUser.getDescription());
	}

	private Object currentGroupReference(final CMActivityInstance activityInstance) {
		final GroupCard groupCard = GroupCard.getOrNull(activityInstance.getPerformerName());
		if (groupCard != null) {
			return CardReference.newInstance(GroupCard.GROUP_CLASS_NAME, Long.valueOf(groupCard.getId()),
					groupCard.getDescription());
		} else {
			return null;
		}
	}

	private void saveWidgets(final CMActivityInstance activityInstance, final Map<String, Object> widgetSubmission,
			final Map<String, Object> nativeValues) throws CMWorkflowException {
		for (final CMActivityWidget w : activityInstance.getWidgets()) {
			final Object submission = widgetSubmission.get(w.getId());
			if (submission == null)
				continue;
			try {
				w.save(activityInstance, submission, nativeValues);
			} catch (final Exception e) {
				throw new CMWorkflowException("Widget save failed", e);
			}
		}
	}

	private UserProcessInstanceDefinition modifyProcessInstance(final CMProcessInstance processInstance) {
		return ProcessInstanceWrapper.readProcessInstance(userCtx, processDefinitionManager,
				findProcessTypeById(processInstance.getType().getId()), processInstance);
	}

	@Override
	public UserProcessInstance advanceActivity(final CMActivityInstance activityInstance) throws CMWorkflowException {
		final CMProcessInstance procInst = activityInstance.getProcessInstance();
		final String procInstId = procInst.getProcessInstanceId();
		for (final CMActivityWidget w : activityInstance.getWidgets()) {
			w.advance(activityInstance);
		}
		workflowService.advanceActivityInstance(procInstId, activityInstance.getId());
		return retrieveChangedProcessInstanceFromDataStore(procInst);
	}

	private UserProcessInstance retrieveChangedProcessInstanceFromDataStore(final CMProcessInstance procInst)
			throws CMWorkflowException {
		// TODO After bidirectional communication, fetch it from the database,
		// because shark changed it
		final WSProcessInstInfo procInstInfo = workflowService.getProcessInstance(procInst.getProcessInstanceId());
		if (procInstInfo == null) {
			return completeProcess(procInst);
		} else {
			return syncProcessStateActivitiesAndVariables(procInst, procInstInfo);
		}
	}

	/**
	 * It should extract CMProcessClass with findAllProcessClasses() but the new
	 * DAO is not here yet. If it wasn't for SQL, we would breathe hacks.
	 * 
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
		final Iterable<? extends CMProcessInstance> activeProcessInstances = queryDBOpenAndSuspended(processType);
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

	private Map<String, WSProcessInstInfo> queryWSOpenAndSuspended(final CMProcessClass processClass)
			throws CMWorkflowException {
		final Map<String, WSProcessInstInfo> wsInfo = new HashMap<String, WSProcessInstInfo>();
		final String processDefinitionId = processClass.getProcessDefinitionId();
		if (processDefinitionId != null) {
			for (final WSProcessInstInfo pis : workflowService.listOpenProcessInstances(processDefinitionId)) {
				wsInfo.put(pis.getProcessInstanceId(), pis);
			}
		}
		return wsInfo;
	}

	@Legacy("Old DAO")
	private Iterable<UserProcessInstance> queryDBOpenAndSuspended(final ProcessType processType) {
		final int openFlowStatusId = ProcessInstanceWrapper.lookupForFlowStatus(WSProcessInstanceState.OPEN).getId();
		final int suspendedFlowStatusId = ProcessInstanceWrapper.lookupForFlowStatus(WSProcessInstanceState.SUSPENDED)
				.getId();
		final CardQuery cardQuery = processType
				.cards()
				.list()
				.filterUpdate(ProcessAttributes.FlowStatus.dbColumnName(), AttributeFilterType.EQUALS,
						openFlowStatusId, suspendedFlowStatusId);
		return query(cardQuery);
	}

	@Override
	@Legacy("Old DAO")
	public Iterable<UserProcessInstance> query(final CardQuery cardQuery) {
		return Iterables.transform(cardQuery, new Function<ICard, UserProcessInstance>() {

			@Override
			public UserProcessInstance apply(final ICard input) {
				return wrap(input);
			}

		});
	}

	private void removeOutOfSyncProcess(final CMProcessInstance processInstance) {
		modifyProcessInstance(processInstance).setState(WSProcessInstanceState.UNSUPPORTED).save();
	}

	private UserProcessInstance completeProcess(final CMProcessInstance processInstance) throws CMWorkflowException {
		final UserProcessInstanceDefinition editableProcessInstance = modifyProcessInstance(processInstance);
		editableProcessInstance.setState(WSProcessInstanceState.COMPLETED);
		editableProcessInstance.setActivities(new WSActivityInstInfo[0]);
		return editableProcessInstance.save();
	}

	private UserProcessInstance syncProcessStateActivitiesAndVariables(final CMProcessInstance processInstance,
			final WSProcessInstInfo processInstanceInfo) throws CMWorkflowException {
		return syncProcessStateActivitiesAndMaybeVariables(processInstance, processInstanceInfo, true);
	}

	private UserProcessInstance syncProcessStateAndActivities(final CMProcessInstance processInstance,
			final WSProcessInstInfo processInstanceInfo) throws CMWorkflowException {
		return syncProcessStateActivitiesAndMaybeVariables(processInstance, processInstanceInfo, false);
	}

	private UserProcessInstance syncProcessStateActivitiesAndMaybeVariables(final CMProcessInstance processInstance,
			final WSProcessInstInfo processInstanceInfo, final boolean syncVariables) throws CMWorkflowException {
		final UserProcessInstanceDefinition editableProcessInstance = modifyProcessInstance(processInstance);

		// TODO Sync variables (should be removed when bidirectional
		// communication is implemented)
		if (syncVariables) {
			final Map<String, Object> vars = workflowService.getProcessInstanceVariables(processInstance
					.getProcessInstanceId());
			for (final CMAttribute a : processInstance.getType().getAttributes()) {
				final String attributeName = a.getName();
				final Object newValue = vars.get(attributeName);
				editableProcessInstance.set(attributeName, newValue);
			}
		}
		editableProcessInstance.setState(processInstanceInfo.getStatus());
		editableProcessInstance.setUniqueProcessDefinition(processInstanceInfo);
		final WSActivityInstInfo[] activities = workflowService.findOpenActivitiesForProcessInstance(processInstance
				.getProcessInstanceId());
		editableProcessInstance.setActivities(activities);
		return editableProcessInstance.save();
	}

	@Override
	public UserProcessInstance findProcessInstance(final CMProcessClass processDefinition, final Long cardId) {
		final ProcessType processType = findProcessTypeById(processDefinition.getId());
		final Process processCard = processType.cards().get(cardId.intValue());
		return new ProcessInstanceWrapper(userCtx, processDefinitionManager, processCard);
	}

	@Override
	public void setEventListener(final CMWorkflowEngineListener eventListener) {
		this.eventListener = eventListener;
	}
}

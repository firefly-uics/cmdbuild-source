package org.cmdbuild.workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.legacywrappers.ProcessInstanceWrapper;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
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
public class WorkflowEngineWrapper extends LegacyWorkflowPersistence implements ContaminatedWorkflowEngine {

	private static final CMWorkflowEngineListener NULL_EVENT_LISTENER = new NullWorkflowEngineListener();

	private CMWorkflowEngineListener eventListener;

	public WorkflowEngineWrapper(final UserContext userCtx, final CMWorkflowService workflowService,
			final WorkflowTypesConverter variableConverter, final ProcessDefinitionManager processDefinitionManager) {
		super(userCtx, workflowService, variableConverter, processDefinitionManager);
		this.eventListener = NULL_EVENT_LISTENER;
	}

	@Override
	public void setEventListener(final CMWorkflowEngineListener eventListener) {
		this.eventListener = eventListener;
	}

	@Override
	public UserProcessClass findProcessClassById(final Long id) {
		Validate.notNull(id);
		return super.findProcessClassById(id);
	}

	@Override
	public UserProcessClass findProcessClassByName(final String name) {
		Validate.notNull(name);
		return super.findProcessClassByName(name);
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
		final WSProcessInstInfo procInstInfo = workflowService.startProcess(processClass.getPackageId(),
				processClass.getProcessDefinitionId());
		final WSActivityInstInfo startActInstInfo = keepOnlyStartingActivityInstance(startActivity.getId(),
				procInstInfo.getProcessInstanceId());

		final UserProcessInstanceDefinition proc = newProcessInstance(processClass, procInstInfo);
		final String group = userCtx.getDefaultGroup().getName();
		proc.addActivity(activityWithSpecificParticipant(startActInstInfo, group));
		final UserProcessInstance procInst = proc.save();

		fillCardInfoAndProcessInstanceIdOnProcessInstance(procInst);
		return refetchProcessInstance(procInst);
	}

	private WSActivityInstInfo activityWithSpecificParticipant(final WSActivityInstInfo wsActivityInstInfo,
			final String participant) {
		return new WSActivityInstInfo() {

			@Override
			public String getProcessInstanceId() {
				return wsActivityInstInfo.getProcessInstanceId();
			}

			@Override
			public String[] getParticipants() {
				return new String[] { participant };
			}

			@Override
			public String getActivityName() {
				return wsActivityInstInfo.getActivityName();
			}

			@Override
			public String getActivityInstanceId() {
				return wsActivityInstInfo.getActivityInstanceId();
			}

			@Override
			public String getActivityDescription() {
				return wsActivityInstInfo.getActivityDescription();
			}

			@Override
			public String getActivityDefinitionId() {
				return wsActivityInstInfo.getActivityDefinitionId();
			}

		};
	}

	private void fillCardInfoAndProcessInstanceIdOnProcessInstance(final UserProcessInstance procInst)
			throws CMWorkflowException {
		final String procInstId = procInst.getProcessInstanceId();
		final Map<String, Object> extraVars = new HashMap<String, Object>();
		extraVars.put(Constants.PROCESS_CARD_ID_VARIABLE, procInst.getCardId());
		extraVars.put(Constants.PROCESS_CLASSNAME_VARIABLE, procInst.getType().getName());
		extraVars.put(Constants.PROCESS_INSTANCE_ID_VARIABLE, procInstId);
		workflowService.setProcessInstanceVariables(procInstId, toWorkflowValues(procInst.getType(), extraVars));
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

	@Override
	public void abortProcessInstance(final CMProcessInstance processInstance) throws CMWorkflowException {
		workflowService.abortProcessInstance(processInstance.getProcessInstanceId());
	}

	@Override
	public void suspendProcessInstance(final CMProcessInstance processInstance) throws CMWorkflowException {
		workflowService.suspendProcessInstance(processInstance.getProcessInstanceId());
	}

	@Override
	public void resumeProcessInstance(final CMProcessInstance processInstance) throws CMWorkflowException {
		workflowService.resumeProcessInstance(processInstance.getProcessInstanceId());
	}

	@Override
	public void updateActivity(final CMActivityInstance activityInstance, final Map<String, ?> inputValues,
			final Map<String, Object> widgetSubmission) throws CMWorkflowException {
		final Map<String, Object> nativeValues = new HashMap<String, Object>();
		final CMProcessInstance procInst = activityInstance.getProcessInstance();
		final CMProcessInstanceDefinition procInstDef = modifyProcessInstance(procInst); // FIXME
		for (final String key : inputValues.keySet()) {
			final Object value = inputValues.get(key);
			procInstDef.set(key, value);
			nativeValues.put(key, procInstDef.get(key));
		}
		procInstDef.save();

		saveWidgets(activityInstance, widgetSubmission, nativeValues);
		fillCustomProcessVariables(activityInstance, nativeValues);
		workflowService.setProcessInstanceVariables(procInst.getProcessInstanceId(),
				toWorkflowValues(procInst.getType(), nativeValues));
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

	@Override
	public UserProcessInstance advanceActivity(final CMActivityInstance activityInstance) throws CMWorkflowException {
		final CMProcessInstance procInst = activityInstance.getProcessInstance();
		final String procInstId = procInst.getProcessInstanceId();
		for (final CMActivityWidget w : activityInstance.getWidgets()) {
			w.advance(activityInstance);
		}
		workflowService.advanceActivityInstance(procInstId, activityInstance.getId());
		return refetchProcessInstance(procInst);
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
		modifyProcessInstance(processInstance).setState(WSProcessInstanceState.ABORTED).save();
	}

	@Override
	public UserProcessInstance findProcessInstance(final CMProcessClass processDefinition, final Long cardId) {
		return super.findProcessInstance(processDefinition, cardId);
	}

}

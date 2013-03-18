package org.cmdbuild.workflow;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Builder;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.legacywrappers.ProcessInstanceWrapper;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Wrapper for the CMWorkflowEngine on top of the legacy UserContext and a load
 * of Wrappers from the legacy DAO later to the new interfaces.
 */
public class WorkflowEngineWrapper implements ContaminatedWorkflowEngine {

	public static class WorkflowEngineBuilder implements Builder<WorkflowEngineWrapper> {

		private OperationUser operationUser;
		private LegacyWorkflowPersistence persistence;
		private CMWorkflowService service;
		private WorkflowTypesConverter typesConverter;

		public WorkflowEngineBuilder withOperationUser(final OperationUser value) {
			this.operationUser = value;
			return this;
		}

		public WorkflowEngineBuilder withPersistence(final LegacyWorkflowPersistence value) {
			this.persistence = value;
			return this;
		}

		public WorkflowEngineBuilder withService(final CMWorkflowService value) {
			this.service = value;
			return this;
		}

		public WorkflowEngineBuilder withTypesConverter(final WorkflowTypesConverter value) {
			this.typesConverter = value;
			return this;
		}

		@Override
		public WorkflowEngineWrapper build() {
			return new WorkflowEngineWrapper(this);
		}

	}

	public static WorkflowEngineBuilder newInstance() {
		return new WorkflowEngineBuilder();
	}

	private static final CMWorkflowEngineListener NULL_EVENT_LISTENER = new NullWorkflowEngineListener();

	private final OperationUser operationUser;
	private final LegacyWorkflowPersistence persistence;
	private final CMWorkflowService service;
	private final WorkflowTypesConverter typesConverter;

	private CMWorkflowEngineListener eventListener;

	private WorkflowEngineWrapper(final WorkflowEngineBuilder builder) {
		this.operationUser = builder.operationUser;
		this.persistence = builder.persistence;
		this.service = builder.service;
		this.typesConverter = builder.typesConverter;
		this.eventListener = NULL_EVENT_LISTENER;
	}

	@Override
	public void setEventListener(final CMWorkflowEngineListener eventListener) {
		this.eventListener = eventListener;
	}

	@Override
	public UserProcessClass findProcessClassById(final Long id) {
		Validate.notNull(id);
		return persistence.findProcessClassById(id);
	}

	@Override
	public UserProcessClass findProcessClassByName(final String name) {
		Validate.notNull(name);
		return persistence.findProcessClassByName(name);
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
		return persistence.getAllProcessClasses();
	}

	@Override
	public UserProcessInstance startProcess(final CMProcessClass processClass) throws CMWorkflowException {
		final CMActivity startActivity = processClass.getStartActivity();
		if (startActivity == null) {
			return null;
		}
		final WSProcessInstInfo procInstInfo = service.startProcess(processClass.getPackageId(),
				processClass.getProcessDefinitionId());
		final WSActivityInstInfo startActInstInfo = keepOnlyStartingActivityInstance(startActivity.getId(),
				procInstInfo.getProcessInstanceId());

		final UserProcessInstance processInstance = persistence.newProcessInstance(processClass, procInstInfo) //
				.addActivity(activityWithSpecificParticipant( //
						startActInstInfo, //
						operationUser.getPreferredGroup().getName())) //
				.save();

		fillCardInfoAndProcessInstanceIdOnProcessInstance(processInstance);
		return persistence.refetchProcessInstance(processInstance);
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

	private WSActivityInstInfo keepOnlyStartingActivityInstance(final String startActivityId,
			final String processInstanceId) throws CMWorkflowException {
		WSActivityInstInfo startActivityInstanceInfo = null;
		final Iterable<WSActivityInstInfo> activityInstanceInfos = asList(service
				.findOpenActivitiesForProcessInstance(processInstanceId));
		for (final WSActivityInstInfo activityInstanceInfo : activityInstanceInfos) {
			final String activityDefinitionId = activityInstanceInfo.getActivityDefinitionId();
			if (startActivityId.equals(activityDefinitionId)) {
				startActivityInstanceInfo = activityInstanceInfo;
			} else {
				final String activityInstanceId = activityInstanceInfo.getActivityInstanceId();
				service.abortActivityInstance(processInstanceId, activityInstanceId);
			}
		}
		return startActivityInstanceInfo;
	}

	private void fillCardInfoAndProcessInstanceIdOnProcessInstance(final UserProcessInstance procInst)
			throws CMWorkflowException {
		final String procInstId = procInst.getProcessInstanceId();
		final Map<String, Object> extraVars = new HashMap<String, Object>();
		extraVars.put(Constants.PROCESS_CARD_ID_VARIABLE, procInst.getCardId());
		extraVars.put(Constants.PROCESS_CLASSNAME_VARIABLE, procInst.getType().getName());
		extraVars.put(Constants.PROCESS_INSTANCE_ID_VARIABLE, procInstId);
		service.setProcessInstanceVariables(procInstId, persistence.toWorkflowValues(procInst.getType(), extraVars));
	}

	@Override
	public void abortProcessInstance(final CMProcessInstance processInstance) throws CMWorkflowException {
		service.abortProcessInstance(processInstance.getProcessInstanceId());
	}

	@Override
	public void suspendProcessInstance(final CMProcessInstance processInstance) throws CMWorkflowException {
		service.suspendProcessInstance(processInstance.getProcessInstanceId());
	}

	@Override
	public void resumeProcessInstance(final CMProcessInstance processInstance) throws CMWorkflowException {
		service.resumeProcessInstance(processInstance.getProcessInstanceId());
	}

	@Override
	public void updateActivity(final CMActivityInstance activityInstance, final Map<String, ?> inputValues,
			final Map<String, Object> widgetSubmission) throws CMWorkflowException {
		final Map<String, Object> nativeValues = new HashMap<String, Object>();
		final CMProcessInstance procInst = activityInstance.getProcessInstance();
		final ProcessInstanceWrapper procInstDef = persistence.modifyProcessInstance(procInst); // FIXME
		for (final String key : inputValues.keySet()) {
			final Object value = inputValues.get(key);
			procInstDef.set(key, value);
			nativeValues.put(key, procInstDef.get(key));
		}
		procInstDef.save();

		saveWidgets(activityInstance, widgetSubmission, nativeValues);
		fillCustomProcessVariables(activityInstance, nativeValues);
		service.setProcessInstanceVariables(procInst.getProcessInstanceId(),
				persistence.toWorkflowValues(procInst.getType(), nativeValues));
	}

	private void fillCustomProcessVariables(final CMActivityInstance activityInstance,
			final Map<String, Object> nativeValues) {
		nativeValues.put(Constants.CURRENT_USER_VARIABLE, currentUserReference());
		nativeValues.put(Constants.CURRENT_GROUP_VARIABLE, currentGroupReference(activityInstance));
	}

	private CardReference currentUserReference() {
		final AuthenticatedUser authenticatedUser = operationUser.getAuthenticatedUser();
		return CardReference.newInstance( //
				UserCard.USER_CLASS_NAME, //
				authenticatedUser.getId(), //
				authenticatedUser.getDescription());
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
			final Object submission = widgetSubmission.get(w.getStringId());
			if (submission == null) {
				continue;
			}
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
		service.advanceActivityInstance(procInstId, activityInstance.getId());
		return persistence.refetchProcessInstance(procInst);
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
		for (final UserProcessClass processClass : persistence.getAllProcessClasses()) {
			if (processClass.isSuperclass()) {
				continue;
			}
			syncProcess(processClass);
		}
		eventListener.syncFinished();
	}

	private void syncProcess(final UserProcessClass processClass) throws CMWorkflowException {
		eventListener.syncProcessStarted(processClass);
		final Map<String, WSProcessInstInfo> wsInfo = queryWSOpenAndSuspended(processClass);
		final Iterable<? extends CMProcessInstance> activeProcessInstances = persistence
				.queryDBOpenAndSuspended(processClass);
		for (final CMProcessInstance processInstance : activeProcessInstances) {
			final String processInstanceId = processInstance.getProcessInstanceId();
			final WSProcessInstInfo processInstanceInfo = wsInfo.get(processInstanceId);
			if (processInstanceInfo == null) {
				eventListener.syncProcessInstanceNotFound(processInstance);
				removeOutOfSyncProcess(processInstance);
			} else {
				eventListener.syncProcessInstanceFound(processInstance);
				ProcessSynchronizer.of(service, persistence, typesConverter) //
						.syncProcessStateAndActivities(processInstance, processInstanceInfo);
			}
		}
	}

	private Map<String, WSProcessInstInfo> queryWSOpenAndSuspended(final CMProcessClass processClass)
			throws CMWorkflowException {
		final Map<String, WSProcessInstInfo> wsInfo = new HashMap<String, WSProcessInstInfo>();
		final String processDefinitionId = processClass.getProcessDefinitionId();
		if (processDefinitionId != null) {
			for (final WSProcessInstInfo pis : service.listOpenProcessInstances(processDefinitionId)) {
				wsInfo.put(pis.getProcessInstanceId(), pis);
			}
		}
		return wsInfo;
	}

	@Override
	@Legacy("Old DAO")
	public Iterable<UserProcessInstance> query(final CardQuery cardQuery) {
		return persistence.query(cardQuery);
	}

	private void removeOutOfSyncProcess(final CMProcessInstance processInstance) {
		persistence.modifyProcessInstance(processInstance).setState(WSProcessInstanceState.ABORTED).save();
	}

	@Override
	public UserProcessInstance findProcessInstance(final CMProcessClass processDefinition, final Long cardId) {
		return persistence.findProcessInstance(processDefinition, cardId);
	}

}

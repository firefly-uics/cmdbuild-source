package org.cmdbuild.workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.legacywrappers.ProcessClassWrapper;
import org.cmdbuild.dao.legacywrappers.ProcessInstanceWrapper;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.Process;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.user.UserProcessInstance.UserProcessInstanceDefinition;

/**
 * Abstract class to handle workflow object persistence using the old layer.
 */
public abstract class LegacyWorkflowPersistence {

	protected final UserContext userCtx;
	protected final CMWorkflowService workflowService;
	protected final ProcessDefinitionManager processDefinitionManager;
	private final WorkflowTypesConverter workflowVariableConverter;

	protected LegacyWorkflowPersistence( //
			final UserContext userCtx, //
			final CMWorkflowService workflowService, //
			final WorkflowTypesConverter workflowVariableConverter, //
			final ProcessDefinitionManager processDefinitionManager) {
		Validate.notNull(workflowVariableConverter);
		this.userCtx = userCtx;
		this.workflowService = workflowService;
		this.workflowVariableConverter = workflowVariableConverter;
		this.processDefinitionManager = processDefinitionManager;
	}

	protected final UserProcessInstance syncProcessStateActivitiesAndVariables(final CMProcessInstance processInstance,
			final WSProcessInstInfo processInstanceInfo) throws CMWorkflowException {
		return syncProcessStateActivitiesAndMaybeVariables(processInstance, processInstanceInfo, true);
	}

	protected final UserProcessInstance syncProcessStateAndActivities(final CMProcessInstance processInstance,
			final WSProcessInstInfo processInstanceInfo) throws CMWorkflowException {
		return syncProcessStateActivitiesAndMaybeVariables(processInstance, processInstanceInfo, false);
	}

	private UserProcessInstance syncProcessStateActivitiesAndMaybeVariables(final CMProcessInstance processInstance,
			final WSProcessInstInfo processInstanceInfo, final boolean syncVariables) throws CMWorkflowException {
		if (processInstanceInfo == null) {
			return completeProcess(processInstance);
		}

		final UserProcessInstanceDefinition editableProcessInstance = modifyProcessInstance(processInstance);

		if (syncVariables) {
			final Map<String, Object> workflowValues = workflowService.getProcessInstanceVariables(processInstance
					.getProcessInstanceId());
			final Map<String, Object> nativeValues = fromWorkflowValues(workflowValues);
			for (final CMAttribute a : processInstance.getType().getAttributes()) {
				final String attributeName = a.getName();
				final Object newValue = nativeValues.get(attributeName);
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

	private UserProcessInstance completeProcess(final CMProcessInstance processInstance) throws CMWorkflowException {
		final UserProcessInstanceDefinition editableProcessInstance = modifyProcessInstance(processInstance);
		editableProcessInstance.setState(WSProcessInstanceState.COMPLETED);
		editableProcessInstance.setActivities(new WSActivityInstInfo[0]);
		return editableProcessInstance.save();
	}

	protected final ProcessInstanceWrapper modifyProcessInstance(final CMProcessInstance processInstance) {
		return ProcessInstanceWrapper //
				.readProcessInstance( //
						userCtx, //
						processDefinitionManager, //
						findProcessTypeById(processInstance.getType().getId()), //
						processInstance);
	}

	protected final ProcessType findProcessTypeById(final Object idObject) {
		final int id = ((Number) idObject).intValue();
		return userCtx.processTypes().get(id);
	}

	protected final ProcessType findProcessTypeByName(final String name) {
		return userCtx.processTypes().get(name);
	}

	protected final UserProcessInstance findProcessInstance(final WSProcessInstInfo procInstInfo)
			throws CMWorkflowException {
		final String processClassName = processDefinitionManager.getProcessClassName(procInstInfo
				.getProcessDefinitionId());
		final ProcessType processType = findProcessTypeByName(processClassName);
		final ICard processCard = processType
				.cards()
				.list()
				.filter(ProcessAttributes.ProcessInstanceId.dbColumnName(), AttributeFilterType.EQUALS,
						procInstInfo.getProcessInstanceId()).get(false);
		return wrap(processCard);
	}

	protected final CMProcessInstance createProcessInstance(final WSProcessInstInfo procInstInfo)
			throws CMWorkflowException {
		final String processClassName = processDefinitionManager.getProcessClassName(procInstInfo
				.getProcessDefinitionId());
		final CMProcessClass processDefinition = findProcessClassByName(processClassName);
		return newProcessInstance(processDefinition, procInstInfo).save();
	}

	protected UserProcessInstanceDefinition newProcessInstance(final CMProcessClass processDefinition,
			final WSProcessInstInfo procInst) {
		return ProcessInstanceWrapper.createProcessInstance(userCtx, processDefinitionManager,
				findProcessTypeById(processDefinition.getId()), procInst);
	}

	protected UserProcessClass findProcessClassById(final Long id) {
		return wrap(findProcessTypeById(id));
	}

	protected UserProcessClass findProcessClassByName(final String name) {
		return wrap(findProcessTypeByName(name));
	}

	protected final UserProcessClass wrap(final ProcessType processType) {
		if (processType == null)
			return null;
		return new ProcessClassWrapper(userCtx, processType, processDefinitionManager);
	}

	protected UserProcessInstance refetchProcessInstance(final CMProcessInstance processInstance) {
		return findProcessInstance(processInstance.getType(), processInstance.getId());
	}

	protected UserProcessInstance findProcessInstance(final CMProcessClass processDefinition, final Long cardId) {
		final ProcessType processType = findProcessTypeById(processDefinition.getId());
		final Process processCard = processType.cards().get(cardId.intValue());
		return new ProcessInstanceWrapper(userCtx, processDefinitionManager, processCard);
	}

	protected final UserProcessInstance wrap(final ICard processCard) {
		if (processCard == null)
			return null;
		return new ProcessInstanceWrapper(userCtx, processDefinitionManager, processCard);
	}

	protected final Map<String, Object> toWorkflowValues(final CMProcessClass processClass,
			final Map<String, Object> nativeValues) {
		final Map<String, Object> workflowValues = new HashMap<String, Object>();
		for (Map.Entry<String, Object> nv : nativeValues.entrySet()) {
			final String attributeName = nv.getKey();
			CMAttributeType<?> attributeType;
			try {
				attributeType = processClass.getAttribute(attributeName).getType();
			} catch (IllegalArgumentException e) {
				attributeType = null;
			}
			workflowValues.put(attributeName, workflowVariableConverter.toWorkflowType(attributeType, nv.getValue()));
		}
		return workflowValues;
	}

	protected final Map<String, Object> fromWorkflowValues(final Map<String, Object> workflowValues) {
		return fromWorkflowValues(workflowValues, workflowVariableConverter);
	}

	/*
	 * FIXME AWFUL pre-release hack
	 */
	public static final Map<String, Object> fromWorkflowValues(final Map<String, Object> workflowValues,
			final WorkflowTypesConverter workflowVariableConverter) {
		final Map<String, Object> nativeValues = new HashMap<String, Object>();
		for (Map.Entry<String, Object> wv : workflowValues.entrySet()) {
			nativeValues.put(wv.getKey(), workflowVariableConverter.fromWorkflowType(wv.getValue()));
		}
		return nativeValues;
	}
}

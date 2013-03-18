package org.cmdbuild.workflow;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.logger.Log;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.user.UserProcessInstance.UserProcessInstanceDefinition;

public class ProcessSynchronizer {

	public static ProcessSynchronizer of(final CMWorkflowService service, final LegacyWorkflowPersistence persistence,
			final WorkflowTypesConverter typesConverter) {
		return new ProcessSynchronizer(service, persistence, typesConverter);
	}

	private final CMWorkflowService workflowService;
	private final LegacyWorkflowPersistence persistence;
	private final WorkflowTypesConverter typesConverter;

	private ProcessSynchronizer(final CMWorkflowService service, final LegacyWorkflowPersistence persistence,
			final WorkflowTypesConverter typesConverter) {
		this.workflowService = service;
		this.persistence = persistence;
		this.typesConverter = typesConverter;
	}

	public UserProcessInstance syncProcessStateActivitiesAndVariables(final CMProcessInstance processInstance,
			final WSProcessInstInfo processInstanceInfo) throws CMWorkflowException {
		return syncProcessStateActivitiesAndMaybeVariables(processInstance, processInstanceInfo, true);
	}

	public UserProcessInstance syncProcessStateAndActivities(final CMProcessInstance processInstance,
			final WSProcessInstInfo processInstanceInfo) throws CMWorkflowException {
		return syncProcessStateActivitiesAndMaybeVariables(processInstance, processInstanceInfo, false);
	}

	private UserProcessInstance syncProcessStateActivitiesAndMaybeVariables(final CMProcessInstance processInstance,
			final WSProcessInstInfo processInstanceInfo, final boolean syncVariables) throws CMWorkflowException {
		Log.WORKFLOW.info("synchronizing process state, activities and (maybe) variables");
		final UserProcessInstanceDefinition editableProcessInstance = persistence
				.modifyProcessInstance(processInstance);
		if (syncVariables) {
			Log.WORKFLOW.info("synchronizing variables");
			final Map<String, Object> workflowValues = workflowService.getProcessInstanceVariables(processInstance
					.getProcessInstanceId());
			final Map<String, Object> nativeValues = fromWorkflowValues(workflowValues);
			for (final CMAttribute a : processInstance.getType().getAttributes()) {
				final String attributeName = a.getName();
				final Object newValue = nativeValues.get(attributeName);
				Log.WORKFLOW.debug(format("synchronizing variable '%s' with value '%s'", attributeName, newValue));
				editableProcessInstance.set(attributeName, newValue);
			}
		}
		if (processInstanceInfo == null) {
			Log.WORKFLOW
					.warn("process instance info is null, setting process as completed (should never happen, but who knows...");
			editableProcessInstance.setState(WSProcessInstanceState.COMPLETED);
			editableProcessInstance.setActivities(new WSActivityInstInfo[0]);
		} else {
			editableProcessInstance.setUniqueProcessDefinition(processInstanceInfo);
			final WSActivityInstInfo[] activities = workflowService
					.findOpenActivitiesForProcessInstance(processInstance.getProcessInstanceId());
			editableProcessInstance.setActivities(activities);
			final WSProcessInstanceState actualState = processInstanceInfo.getStatus();
			editableProcessInstance.setState(actualState);
			if (actualState == WSProcessInstanceState.COMPLETED) {
				Log.WORKFLOW.info("process is completed, delete if from workflow service");
				workflowService.deleteProcessInstance(processInstanceInfo.getProcessInstanceId());
			}
		}
		return editableProcessInstance.save();
	}

	private final Map<String, Object> fromWorkflowValues(final Map<String, Object> workflowValues) {
		return fromWorkflowValues(workflowValues, typesConverter);
	}

	/*
	 * FIXME AWFUL pre-release hack
	 */
	public static final Map<String, Object> fromWorkflowValues(final Map<String, Object> workflowValues,
			final WorkflowTypesConverter workflowVariableConverter) {
		final Map<String, Object> nativeValues = new HashMap<String, Object>();
		for (final Map.Entry<String, Object> wv : workflowValues.entrySet()) {
			nativeValues.put(wv.getKey(), workflowVariableConverter.fromWorkflowType(wv.getValue()));
		}
		return nativeValues;
	}

}

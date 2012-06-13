package org.cmdbuild.workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.legacywrappers.ProcessClassWrapper;
import org.cmdbuild.dao.legacywrappers.ProcessInstanceWrapper;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CMProcessInstance.CMProcessInstanceDefinition;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSActivityInstInfo;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Wrapper for the CMWorkflowEngine on top of the legacy UserContext and
 * a load of Wrappers from the legacy DAO later to the new interfaces.
 */
public class WorkflowEngineWrapper implements CMWorkflowEngine {

	private final UserContext userCtx;
	private final CMWorkflowService workflowService;
	private final ProcessDefinitionManager processDefinitionManager;

	public WorkflowEngineWrapper(final UserContext userCtx, final CMWorkflowService workflowService,
			final ProcessDefinitionManager processDefinitionManager) {
		this.userCtx = userCtx;
		this.workflowService = workflowService;
		this.processDefinitionManager = processDefinitionManager;
	}

	@Override
	public CMProcessClass findProcessClass(Object idOrName) {
		Validate.notNull(idOrName);
		final ProcessType processType = findProcessType(idOrName);
		return new ProcessClassWrapper(userCtx, processType, processDefinitionManager);
	}

	@Override
	public CMProcessClass findProcessClassById(Object id) {
		Validate.notNull(id);
		final ProcessType processType = findProcessTypeById(id);
		return new ProcessClassWrapper(userCtx, processType, processDefinitionManager);
	}

	@Override
	public CMProcessClass findProcessClassByName(String name) {
		Validate.notNull(name);
		final ProcessType processType = findProcessTypeByName(name);
		return new ProcessClassWrapper(userCtx, processType, processDefinitionManager);
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
				return new ProcessClassWrapper(userCtx, input, processDefinitionManager);
			}

		});
	}

	@Override
	public CMProcessInstance startProcess(final CMProcessClass processDefinition) throws CMWorkflowException {
		final CMActivity startActivity = processDefinition.getStartActivity();
		if (startActivity == null) {
			return null;
		}
		final String procInstId = workflowService.startProcess(processDefinition.getPackageId(),
				processDefinition.getProcessDefinitionId());
		WSActivityInstInfo startActInstInfo = keepOnlyStartingActivityInstance(startActivity.getId(), procInstId);
		
		CMProcessInstanceDefinition proc = newProcessInstance(processDefinition, procInstId);
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

	private CMProcessInstanceDefinition newProcessInstance(final CMProcessClass processDefinition, final String procInstId) {
		return ProcessInstanceWrapper.createProcessInstance(userCtx,
				processDefinitionManager,
				findProcessTypeById(processDefinition.getId()),
				procInstId);
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

	@Override
	public void sync() {
		userCtx.privileges().assureAdminPrivilege();
		for (final CMProcessClass proc : findAllProcessClasses()) {
			// LOG
			syncProcess(proc);
		}
	}

	private void syncProcess(final CMProcessClass proc) {
		throw new UnsupportedOperationException("TODO Come on! Cut me some slack!");
	}
}

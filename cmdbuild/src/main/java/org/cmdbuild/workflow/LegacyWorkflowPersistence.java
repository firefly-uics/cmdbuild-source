package org.cmdbuild.workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.legacywrappers.ProcessClassWrapper;
import org.cmdbuild.dao.legacywrappers.ProcessInstanceWrapper;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.Process;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.user.UserProcessInstance.UserProcessInstanceDefinition;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Abstract class to handle workflow object persistence using the old layer.
 */
public abstract class LegacyWorkflowPersistence {

	protected final ProcessDefinitionManager processDefinitionManager;
	private final UserContext userCtx;
	private final WorkflowTypesConverter workflowVariableConverter;

	protected LegacyWorkflowPersistence( //
			final UserContext userCtx, //
			final CMWorkflowService workflowService, //
			final WorkflowTypesConverter workflowVariableConverter, //
			final ProcessDefinitionManager processDefinitionManager) {
		Validate.notNull(workflowVariableConverter);
		this.userCtx = userCtx;
		this.workflowVariableConverter = workflowVariableConverter;
		this.processDefinitionManager = processDefinitionManager;
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
		return UserOperations.from(userCtx).processTypes().get(id);
	}

	protected final ProcessType findProcessTypeByName(final String name) {
		return UserOperations.from(userCtx).processTypes().get(name);
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
		if (processType == null) {
			return null;
		}
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
		if (processCard == null) {
			return null;
		}
		return new ProcessInstanceWrapper(userCtx, processDefinitionManager, processCard);
	}

	protected final Map<String, Object> toWorkflowValues(final CMProcessClass processClass,
			final Map<String, Object> nativeValues) {
		final Map<String, Object> workflowValues = new HashMap<String, Object>();
		for (final Map.Entry<String, Object> nv : nativeValues.entrySet()) {
			final String attributeName = nv.getKey();
			CMAttributeType<?> attributeType;
			try {
				attributeType = processClass.getAttribute(attributeName).getType();
			} catch (final Exception e) {
				attributeType = null;
			}
			workflowValues.put(attributeName, workflowVariableConverter.toWorkflowType(attributeType, nv.getValue()));
		}
		return workflowValues;
	}

	public Iterable<UserProcessClass> getAllProcessClasses() {
		return Iterables.transform(UserOperations.from(userCtx).processTypes().list(),
				new Function<ProcessType, UserProcessClass>() {

					@Override
					public UserProcessClass apply(final ProcessType input) {
						return wrap(input);
					}

				});
	}

	public Iterable<? extends CMProcessInstance> queryDBOpenAndSuspended(final UserProcessClass processClass) {
		final int openFlowStatusId = ProcessInstanceWrapper.lookupForFlowStatus(WSProcessInstanceState.OPEN).getId();
		final int suspendedFlowStatusId = ProcessInstanceWrapper.lookupForFlowStatus(WSProcessInstanceState.SUSPENDED)
				.getId();
		final CardQuery cardQuery = UserOperations //
				.from(userCtx) //
				.processTypes().get(processClass.getName()) //
				.cards() //
				.list() //
				.filterUpdate( //
						ProcessAttributes.FlowStatus.dbColumnName(), //
						AttributeFilterType.EQUALS, //
						openFlowStatusId, suspendedFlowStatusId);
		return query(cardQuery);
	}

	public Iterable<UserProcessInstance> query(final CardQuery cardQuery) {
		return Iterables.transform(cardQuery, new Function<ICard, UserProcessInstance>() {

			@Override
			public UserProcessInstance apply(final ICard input) {
				return wrap(input);
			}

		});
	}

}

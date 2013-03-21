package org.cmdbuild.workflow;

import java.util.Map.Entry;

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
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Abstract class to handle workflow object persistence using the old layer.
 */
public abstract class LegacyWorkflowPersistence implements WorkflowPersistence {

	protected final ProcessDefinitionManager processDefinitionManager;
	private final UserContext userCtx;

	protected LegacyWorkflowPersistence( //
			final UserContext userCtx, //
			final ProcessDefinitionManager processDefinitionManager) {
		this.userCtx = userCtx;
		this.processDefinitionManager = processDefinitionManager;
	}

	protected final ProcessType findProcessTypeById(final Object idObject) {
		final int id = ((Number) idObject).intValue();
		return UserOperations.from(userCtx).processTypes().get(id);
	}

	protected final ProcessType findProcessTypeByName(final String name) {
		return UserOperations.from(userCtx).processTypes().get(name);
	}

	@Override
	public final UserProcessInstance findProcessInstance(final WSProcessInstInfo processInstInfo)
			throws CMWorkflowException {
		final String processClassName = processDefinitionManager.getProcessClassName(processInstInfo
				.getProcessDefinitionId());
		final ProcessType processType = findProcessTypeByName(processClassName);
		final ICard processCard = processType
				.cards()
				.list()
				.filter(ProcessAttributes.ProcessInstanceId.dbColumnName(), AttributeFilterType.EQUALS,
						processInstInfo.getProcessInstanceId()).get(false);
		return wrap(processCard);
	}

	@Override
	public UserProcessInstance createProcessInstance(final WSProcessInstInfo processInstInfo,
			final ProcessCreation processCreation) throws CMWorkflowException {
		final String processClassName = processDefinitionManager.getProcessClassName(processInstInfo
				.getProcessDefinitionId());
		final CMProcessClass processDefinition = findProcessClass(processClassName);
		final UserProcessInstance process = ProcessInstanceWrapper.createProcessInstance(userCtx,
				processDefinitionManager, findProcessTypeById(processDefinition.getId()), processInstInfo) //
				.save();
		return process;
	}

	@Override
	public UserProcessInstance createProcessInstance(final CMProcessClass processClass,
			final WSProcessInstInfo processInstInfo, final ProcessCreation processCreation) throws CMWorkflowException {
		final ProcessInstanceWrapper process = ProcessInstanceWrapper.createProcessInstance(userCtx,
				processDefinitionManager, findProcessTypeById(processClass.getId()), processInstInfo);
		return update(process, processCreation).save();
	}

	@Override
	public UserProcessInstance updateProcessInstance(CMProcessInstance processInstance, ProcessUpdate processUpdate)
			throws CMWorkflowException {
		final ProcessInstanceWrapper process = ProcessInstanceWrapper //
				.readProcessInstance( //
						userCtx, //
						processDefinitionManager, //
						findProcessTypeById(processInstance.getType().getId()), //
						processInstance);
		return update(process, processUpdate).save();
	}

	private ProcessInstanceWrapper update(final ProcessInstanceWrapper process, ProcessCreation processCreation)
			throws CMWorkflowException {
		if (processCreation.state() != ProcessUpdate.NO_STATE) {
			process.setState(processCreation.state());
		}
		if (processCreation.processInstanceInfo() != ProcessUpdate.NO_PROCESS_INSTANCE_INFO) {
			process.setUniqueProcessDefinition(processCreation.processInstanceInfo());
		}
		return process;
	}

	private ProcessInstanceWrapper update(final ProcessInstanceWrapper process, ProcessUpdate processUpdate)
			throws CMWorkflowException {
		update(process, ProcessCreation.class.cast(processUpdate));
		if (processUpdate.values() != ProcessUpdate.NO_VALUES) {
			for (final Entry<String, ?> entry : processUpdate.values().entrySet()) {
				process.set(entry.getKey(), entry.getValue());
			}
		}
		if (processUpdate.addActivities() != ProcessUpdate.NO_ACTIVITIES) {
			for (final WSActivityInstInfo activityInfo : processUpdate.addActivities()) {
				process.addActivity(activityInfo);
			}
		}
		if (processUpdate.activities() != ProcessUpdate.NO_ACTIVITIES) {
			process.setActivities(processUpdate.activities());
		}
		return process;
	}

	@Override
	public UserProcessClass findProcessClass(final Long id) {
		return wrap(findProcessTypeById(id));
	}

	@Override
	public UserProcessClass findProcessClass(final String name) {
		return wrap(findProcessTypeByName(name));
	}

	protected final UserProcessClass wrap(final ProcessType processType) {
		if (processType == null) {
			return null;
		}
		return new ProcessClassWrapper(userCtx, processType, processDefinitionManager);
	}

	@Override
	public UserProcessInstance findProcessInstance(final CMProcessInstance processInstance) {
		return findProcessInstance(processInstance.getType(), processInstance.getId());
	}

	@Override
	public UserProcessInstance findProcessInstance(final CMProcessClass processClass, final Long cardId) {
		final ProcessType processType = findProcessTypeById(processClass.getId());
		final Process processCard = processType.cards().get(cardId.intValue());
		return new ProcessInstanceWrapper(userCtx, processDefinitionManager, processCard);
	}

	protected final UserProcessInstance wrap(final ICard processCard) {
		if (processCard == null) {
			return null;
		}
		return new ProcessInstanceWrapper(userCtx, processDefinitionManager, processCard);
	}

	@Override
	public Iterable<UserProcessClass> getAllProcessClasses() {
		return Iterables.transform(UserOperations.from(userCtx).processTypes().list(),
				new Function<ProcessType, UserProcessClass>() {

					@Override
					public UserProcessClass apply(final ProcessType input) {
						return wrap(input);
					}

				});
	}

	@Override
	public Iterable<? extends UserProcessInstance> queryOpenAndSuspended(final UserProcessClass processClass) {
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

	@Override
	public Iterable<UserProcessInstance> query(final CardQuery cardQuery) {
		return Iterables.transform(cardQuery, new Function<ICard, UserProcessInstance>() {

			@Override
			public UserProcessInstance apply(final ICard input) {
				return wrap(input);
			}

		});
	}

}

package org.cmdbuild.workflow;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entry.LazyValueSet;
import org.cmdbuild.logger.Log;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.context.ApplicationContext;

class ActivityInstanceImpl implements UserActivityInstance {

	// FIXME remove this ASAP
	private static ApplicationContext applicationContext = applicationContext();

	private static final Marker marker = MarkerFactory.getMarker(ActivityInstanceImpl.class.getName());
	private static final Logger logger = Log.WORKFLOW;

	private final OperationUser operationUser;
	private final UserProcessInstance processInstance;
	private final String activityInstanceId;
	private final String activityInstancePerformer;
	private final CMActivity activity;

	public ActivityInstanceImpl(final OperationUser operationUser, final UserProcessInstance processInstance,
			final CMActivity activity, final String activityInstanceId, final String activityInstancePerformer) {
		this.operationUser = operationUser;
		this.processInstance = processInstance;
		this.activity = activity;
		this.activityInstanceId = activityInstanceId;
		this.activityInstancePerformer = activityInstancePerformer;
	}

	@Override
	public UserProcessInstance getProcessInstance() {
		return processInstance;
	}

	@Override
	public String getId() {
		return activityInstanceId;
	}

	@Override
	public CMActivity getDefinition() throws CMWorkflowException {
		return activity;
	}

	@Override
	public String getPerformerName() {
		return activityInstancePerformer;
	}

	@Override
	public boolean isWritable() {
		if (operationUser.hasAdministratorPrivileges()) {
			return true;
		}
		for (final String name : operationUser.getAuthenticatedUser().getGroupNames()) {
			if (name.equals(activityInstancePerformer)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<CMActivityWidget> getWidgets() throws CMWorkflowException {
		return getDefinition().getWidgets(serverSideValues());
	}

	private CMValueSet serverSideValues() {
		/*
		 * FIXME remove dependency from global object
		 */
		return new LazyValueSet() {
			@Override
			protected Map<String, Object> load() {
				try {
					logger.info(marker, "loading variables for process '{}'", processInstance.getProcessInstanceId());
					final CMWorkflowService workflowService = applicationContext.getBean(CMWorkflowService.class);
					final WorkflowTypesConverter typesConverter = applicationContext
							.getBean(WorkflowTypesConverter.class);
					final Map<String, Object> workflowRawTypes = workflowService
							.getProcessInstanceVariables(processInstance.getProcessInstanceId());
					return ProcessSynchronizer.fromWorkflowValues(workflowRawTypes, typesConverter);
				} catch (final CMWorkflowException e) {
					throw new IllegalStateException("error getting process variables", e);
				}
			}
		};
	}

}

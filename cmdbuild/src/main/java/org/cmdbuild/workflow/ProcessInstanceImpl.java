package org.cmdbuild.workflow;

import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.ActivityDefinitionId;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.ActivityInstanceId;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.CurrentActivityPerformers;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.ProcessInstanceId;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.UniqueProcessDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.legacywrappers.ProcessInstanceWrapper;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.workflow.service.WSProcessDefInfo;
import org.cmdbuild.workflow.service.WSProcessDefInfoImpl;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.joda.time.DateTime;

class ProcessInstanceImpl implements UserProcessInstance {

	public static class ProcessInstanceBuilder implements Builder<ProcessInstanceImpl> {

		private OperationUser operationUser;
		private ProcessDefinitionManager processDefinitionManager;
		private CMCard card;
		private Map<Long, String> flowStatusesCodesById;

		@Override
		public ProcessInstanceImpl build() {
			return new ProcessInstanceImpl(this);
		}

		public ProcessInstanceBuilder withOperationUser(final OperationUser value) {
			operationUser = value;
			return this;
		}

		public ProcessInstanceBuilder withProcessDefinitionManager(final ProcessDefinitionManager value) {
			processDefinitionManager = value;
			return this;
		}

		public ProcessInstanceBuilder withCard(final CMCard value) {
			card = value;
			return this;
		}

		public ProcessInstanceBuilder withFlowStatusesCodesById(final Map<Long, String> value) {
			flowStatusesCodesById = value;
			return this;
		}

	}

	public static ProcessInstanceBuilder newInstance() {
		return new ProcessInstanceBuilder();
	}

	private final OperationUser operationUser;
	private final ProcessDefinitionManager processDefinitionManager;
	private final CMCard card;
	private final Map<Long, String> flowStatusesCodesById;

	private ProcessInstanceImpl(final ProcessInstanceBuilder builder) {
		this.operationUser = builder.operationUser;
		this.processDefinitionManager = builder.processDefinitionManager;
		this.card = builder.card;
		this.flowStatusesCodesById = builder.flowStatusesCodesById;
	}

	@Override
	public CMProcessClass getType() {
		return new ProcessClassImpl(operationUser, card.getType(), processDefinitionManager);
	}

	@Override
	public WSProcessInstanceState getState() {
		final Long id = Long.valueOf(card.get(ProcessAttributes.FlowStatus.dbColumnName(), Integer.class));
		return ProcessInstanceWrapper.getFlowStatusForLookup(flowStatusesCodesById.get(id));
	}

	@Override
	public WSProcessDefInfo getUniqueProcessDefinition() {
		final String value = card.get(UniqueProcessDefinition.dbColumnName(), String.class);
		if (value != null) {
			final String[] components = value.split("#");
			if (components.length == 3) {
				return WSProcessDefInfoImpl.newInstance(components[0], components[1], components[2]);
			}
		}
		return null;
	}

	@Override
	public Long getId() {
		return card.getId();
	}

	@Override
	public Object getCode() {
		return card.getCode();
	}

	@Override
	public Object getDescription() {
		return card.getDescription();
	}

	@Override
	public Object get(final String key) {
		return card.get(key);
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType) {
		return card.get(key, requiredType);
	}

	@Override
	public Iterable<Entry<String, Object>> getValues() {
		return card.getValues();
	}

	@Override
	public String getUser() {
		return card.getUser();
	}

	@Override
	public DateTime getBeginDate() {
		return card.getBeginDate();
	}

	@Override
	public DateTime getEndDate() {
		return card.getEndDate();
	}

	@Override
	public Long getCardId() {
		return card.getId();
	}

	@Override
	public String getProcessInstanceId() {
		return card.get(ProcessInstanceId.dbColumnName(), String.class);
	}

	@Override
	public List<UserActivityInstance> getActivities() {
		final List<UserActivityInstance> out = new ArrayList<UserActivityInstance>();
		final String[] activityInstanceIds = card.get(ActivityInstanceId.dbColumnName(), String[].class);
		final String[] activityDefinitionIds = card.get(ActivityDefinitionId.dbColumnName(), String[].class);
		final String[] perfs = card.get(CurrentActivityPerformers.dbColumnName(), String[].class);
		for (int i = 0; i < activityInstanceIds.length; ++i) {
			try {
				final CMActivity activity = processDefinitionManager.getActivity(this, activityDefinitionIds[i]);
				out.add(new ActivityInstanceImpl(operationUser, this, activity, activityInstanceIds[i], perfs[i]));
			} catch (final CMWorkflowException e) {
				// TODO do in another way
				throw new Error(e);
			}
		}
		return out;
	}

	@Override
	public UserActivityInstance getActivityInstance(final String activityInstanceId) {
		for (final UserActivityInstance activityInstance : getActivities()) {
			if (activityInstance.getId().equals(activityInstanceId)) {
				return activityInstance;
			}
		}
		return null;
	}

}

package org.cmdbuild.workflow;

import static java.lang.String.format;
import static org.apache.commons.lang.ArrayUtils.indexOf;
import static org.apache.commons.lang.ArrayUtils.remove;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.cmdbuild.common.utils.Arrays.addDistinct;
import static org.cmdbuild.common.utils.Arrays.append;
import static org.cmdbuild.dao.legacywrappers.ProcessInstanceWrapper.lookupForFlowStatus;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.ActivityDefinitionId;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.ActivityInstanceId;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.AllActivityPerformers;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.CurrentActivityPerformers;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.FlowStatus;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.ProcessInstanceId;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.UniqueProcessDefinition;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.workflow.WorkflowPersistence.ProcessCreation;
import org.cmdbuild.workflow.WorkflowPersistence.ProcessUpdate;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

class WorkflowUpdateHelper {

	private static final Marker marker = MarkerFactory.getMarker(WorkflowUpdateHelper.class.getName());
	private static final Logger logger = Log.PERSISTENCE;

	public static class WorkflowUpdateHelperBuilder implements Builder<WorkflowUpdateHelper> {

		private WSProcessInstInfo processInstInfo;
		private CMCard card;
		private CMCardDefinition cardDefinition;
		private CMProcessInstance processInstance;
		private ProcessDefinitionManager processDefinitionManager;

		@Override
		public WorkflowUpdateHelper build() {
			return new WorkflowUpdateHelper(this);
		}

		public WorkflowUpdateHelperBuilder withProcessInstInfo(final WSProcessInstInfo value) {
			processInstInfo = value;
			return this;
		}

		public WorkflowUpdateHelperBuilder withCardDefinition(final CMCardDefinition value) {
			cardDefinition = value;
			return this;
		}

		public WorkflowUpdateHelperBuilder withCard(final CMCard value) {
			card = value;
			return this;
		}

		public WorkflowUpdateHelperBuilder withProcessInstance(final CMProcessInstance value) {
			processInstance = value;
			return this;
		}

		public WorkflowUpdateHelperBuilder withProcessDefinitionManager(final ProcessDefinitionManager value) {
			processDefinitionManager = value;
			return this;
		}

	}

	public static WorkflowUpdateHelperBuilder newInstance() {
		return new WorkflowUpdateHelperBuilder();
	}

	private static final String UNRESOLVABLE_PARTICIPANT_GROUP = EMPTY;

	private final WSProcessInstInfo processInstInfo;
	private final CMCard card;
	private final CMCardDefinition cardDefinition;
	private final CMProcessInstance processInstance;
	private final ProcessDefinitionManager processDefinitionManager;

	private WorkflowUpdateHelper(final WorkflowUpdateHelperBuilder builder) {
		this.processInstInfo = builder.processInstInfo;
		this.card = builder.card;
		this.cardDefinition = builder.cardDefinition;
		this.processInstance = builder.processInstance;
		this.processDefinitionManager = builder.processDefinitionManager;
	}

	public CMCard save() {
		return cardDefinition.save();
	}

	public WorkflowUpdateHelper initialize() {
		cardDefinition.set(ProcessInstanceId.dbColumnName(), processInstInfo.getProcessInstanceId());
		cardDefinition.set(ActivityInstanceId.dbColumnName(), ArrayUtils.EMPTY_STRING_ARRAY);
		cardDefinition.set(ActivityDefinitionId.dbColumnName(), ArrayUtils.EMPTY_STRING_ARRAY);
		cardDefinition.set(CurrentActivityPerformers.dbColumnName(), ArrayUtils.EMPTY_STRING_ARRAY);
		cardDefinition.set(AllActivityPerformers.dbColumnName(), ArrayUtils.EMPTY_STRING_ARRAY);
		return this;
	}

	public WorkflowUpdateHelper fillForCreation(final ProcessCreation processCreation) throws CMWorkflowException {
		logger.info(marker, "filling process card for creation");
		updateCreationData(processCreation);
		return this;
	}

	private void updateCreationData(final ProcessCreation processCreation) {
		if (processCreation.state() != ProcessCreation.NO_STATE) {
			logger.debug(marker, "updating state");
			final Object id = Long.valueOf(lookupForFlowStatus(processCreation.state()).getId());
			cardDefinition.set(FlowStatus.dbColumnName(), id);
		}

		if (processCreation.processInstanceInfo() != ProcessCreation.NO_PROCESS_INSTANCE_INFO) {
			logger.debug(marker, "updating process instance info");
			final WSProcessInstInfo info = processCreation.processInstanceInfo();
			final String value = format("%s#%s#%s", info.getPackageId(), info.getPackageVersion(),
					info.getProcessDefinitionId());
			cardDefinition.set(UniqueProcessDefinition.dbColumnName(), value);
		}
	}

	public WorkflowUpdateHelper fillForModification(final ProcessUpdate processUpdate) throws CMWorkflowException {
		logger.info(marker, "filling process card for modification");
		updateModificationData(processUpdate);
		return this;
	}

	private void updateModificationData(final ProcessUpdate processUpdate) throws CMWorkflowException {
		updateCreationData(processUpdate);
		if (processUpdate.values() != ProcessUpdate.NO_VALUES) {
			logger.debug(marker, "updating values");
			for (final Entry<String, ?> entry : processUpdate.values().entrySet()) {
				cardDefinition.set(entry.getKey(), entry.getValue());
			}
		}
		if (processUpdate.addActivities() != ProcessUpdate.NO_ACTIVITIES) {
			logger.debug(marker, "adding activities");
			for (final WSActivityInstInfo activityInstanceInfo : processUpdate.addActivities()) {
				logger.debug(marker, "adding activity '{}' '{}'", //
						activityInstanceInfo.getActivityDefinitionId(), //
						activityInstanceInfo.getActivityInstanceId());
				addActivity(activityInstanceInfo);
			}
		}
		if (processUpdate.activities() != ProcessUpdate.NO_ACTIVITIES) {
			logger.debug(marker, "setting activities");
			final WSActivityInstInfo[] activityInfos = processUpdate.activities();
			removeClosedActivities(activityInfos);
			addNewActivities(activityInfos);
			updateCodeWithOneRandomActivityInfo();
		}
	}

	private void addActivity(final WSActivityInstInfo activityInfo) throws CMWorkflowException {
		Validate.notNull(activityInfo);
		Validate.notNull(activityInfo.getActivityInstanceId());
		final String participantGroup = extractActivityParticipantGroup(activityInfo);
		if (participantGroup != UNRESOLVABLE_PARTICIPANT_GROUP) {
			cardDefinition.set(ActivityInstanceId.dbColumnName(),
					append(activityInstanceIds(), activityInfo.getActivityInstanceId()));
			cardDefinition.set(ActivityDefinitionId.dbColumnName(),
					append(activityDefinitionIds(), activityInfo.getActivityDefinitionId()));

			cardDefinition.set(CurrentActivityPerformers.dbColumnName(),
					append(activityInstancePerformers(), participantGroup));
			cardDefinition.set(AllActivityPerformers.dbColumnName(),
					addDistinct(allActivityPerformers(), participantGroup));

			updateCodeWithOneRandomActivityInfo();
		}
	}

	private String extractActivityParticipantGroup(final WSActivityInstInfo activityInfo) throws CMWorkflowException {
		final CMActivity activity = processDefinitionManager.getActivity(processInstance,
				activityInfo.getActivityDefinitionId());
		final ActivityPerformer performer = activity.getFirstNonAdminPerformer();
		final String group;
		switch (performer.getType()) {
		case ROLE:
			group = performer.getValue();
			break;
		case EXPRESSION:
			final String expression = performer.getValue();
			final Set<String> names = evaluatorFor(expression).getNames();
			if (activityInfo.getParticipants().length == 0) {
				/*
				 * an arbitrary expression in a non-starting activity, so should
				 * be a single name
				 */
				final Iterator<String> namesItr = names.iterator();
				group = namesItr.hasNext() ? namesItr.next() : UNRESOLVABLE_PARTICIPANT_GROUP;
			} else {
				final String maybeParticipantGroup = activityInfo.getParticipants()[0];
				group = names.contains(maybeParticipantGroup) ? maybeParticipantGroup : UNRESOLVABLE_PARTICIPANT_GROUP;
			}
			break;
		default:
			group = UNRESOLVABLE_PARTICIPANT_GROUP;
		}
		return group;
	}

	private ActivityPerformerExpressionEvaluator evaluatorFor(final String expression) throws CMWorkflowException {
		final ActivityPerformerExpressionEvaluator evaluator = new BshActivityPerformerExpressionEvaluator(expression);
		final Map<String, Object> rawWorkflowVars = TemporaryObjectsBeforeSpringDI.getWorkflowService() //
				.getProcessInstanceVariables(processInstance.getProcessInstanceId());
		evaluator.setVariables(rawWorkflowVars);
		return evaluator;
	}

	private void removeClosedActivities(final WSActivityInstInfo[] activityInfos) throws CMWorkflowException {
		final Set<String> newActivityInstInfoIds = new HashSet<String>(activityInfos.length);
		for (final WSActivityInstInfo ai : activityInfos) {
			newActivityInstInfoIds.add(ai.getActivityInstanceId());
		}
		for (final String oldActInstId : activityInstanceIds()) {
			if (newActivityInstInfoIds.contains(oldActInstId)) {
				continue;
			}
			removeActivity(oldActInstId);
		}
	}

	public void removeActivity(final String activityInstanceId) throws CMWorkflowException {
		final String[] activityInstanceIds = card.get(ActivityInstanceId.dbColumnName(), String[].class);
		final int index = indexOf(activityInstanceIds, activityInstanceId);
		if (index != ArrayUtils.INDEX_NOT_FOUND) {
			cardDefinition.set(ActivityInstanceId.dbColumnName(), remove(activityInstanceIds, index));
			cardDefinition.set(ActivityDefinitionId.dbColumnName(), remove(activityDefinitionIds(), index));
			cardDefinition.set(CurrentActivityPerformers.dbColumnName(), remove(activityInstancePerformers(), index));
			updateCodeWithOneRandomActivityInfo();
		}
	}

	private void addNewActivities(final WSActivityInstInfo[] activityInfos) throws CMWorkflowException {
		final Set<String> oldActivityInstanceIds = new HashSet<String>();
		for (final String aiid : activityInstanceIds()) {
			oldActivityInstanceIds.add(aiid);
		}
		for (final WSActivityInstInfo ai : activityInfos) {
			if (oldActivityInstanceIds.contains(ai.getActivityInstanceId())) {
				continue;
			}
			addActivity(ai);
		}
	}

	private void updateCodeWithOneRandomActivityInfo() throws CMWorkflowException {
		final List<? extends CMActivityInstance> activities = processInstance.getActivities();
		final String code;
		if (activities.isEmpty()) {
			code = null;
		} else {
			final CMActivity randomActivity = activities.get(0).getDefinition();
			final String randomActivityLabel = defaultIfBlank(randomActivity.getDescription(), EMPTY);
			if (activities.size() > 1) {
				code = format("%s, ...", randomActivityLabel);
			} else {
				code = randomActivityLabel;
			}
		}
		cardDefinition.setCode(code);
	}

	/*
	 * Utilities
	 */

	private String[] activityInstanceIds() {
		return card.get(ActivityInstanceId.dbColumnName(), String[].class);
	}

	private String[] activityDefinitionIds() {
		return card.get(ActivityDefinitionId.dbColumnName(), String[].class);
	}

	private String[] activityInstancePerformers() {
		return card.get(CurrentActivityPerformers.dbColumnName(), String[].class);
	}

	private String[] allActivityPerformers() {
		return card.get(AllActivityPerformers.dbColumnName(), String[].class);
	}

}

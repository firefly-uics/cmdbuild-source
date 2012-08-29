package org.cmdbuild.dao.legacywrappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.dao.entry.LazyValueSet;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.Process;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.ActivityPerformer;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityWidget;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessDefInfo;
import org.cmdbuild.workflow.service.WSProcessDefInfoImpl;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.user.UserProcessInstance.UserProcessInstanceDefinition;
import org.enhydra.shark.api.common.SharkConstants;

import bsh.EvalError;

public class ProcessInstanceWrapper extends CardWrapper implements UserProcessInstance, UserProcessInstanceDefinition {

	private static final String UNRESOLVABLE_PARTICIPANT_GROUP = StringUtils.EMPTY;

	private static final String FLOW_STATUS_LOOKUP = "FlowStatus";

	private class ActivityInstanceImpl implements UserActivityInstance {

		final String activityInstanceId;
		final String activityInstancePerformer;
		final String activityDefinitionId;

		public ActivityInstanceImpl(final String activityInstanceId, final String activityInstancePerformer,
				final String activityDefinitionId) {
			this.activityInstanceId = activityInstanceId;
			this.activityInstancePerformer = activityInstancePerformer;
			this.activityDefinitionId = activityDefinitionId;
		}

		@Override
		public UserProcessInstance getProcessInstance() {
			return ProcessInstanceWrapper.this;
		}

		@Override
		public String getId() {
			return activityInstanceId;
		}

		@Override
		public CMActivity getDefinition() throws CMWorkflowException {
			return findActivity(activityDefinitionId);
		}

		@Override
		public String getPerformerName() {
			return activityInstancePerformer;
		}

		@Override
		public boolean isWritable() {
			return userCtx.privileges().isAdmin() || userCtx.belongsTo(activityInstancePerformer);
		}

		@Override
		public List<CMActivityWidget> getWidgets() throws CMWorkflowException {
			return getDefinition().getWidgets(new LazyValueSet() {
				@Override
				protected Map<String, Object> load() {
					try {
						return workflowService.getProcessInstanceVariables(getProcessInstanceId());
					} catch (final CMWorkflowException exception) {
						throw new IllegalStateException("Process server unreachable", exception);
					}
				}
			});
		}
	}

	@SuppressWarnings("serial")
	protected static final Set<String> processSystemAttributes = new HashSet<String>() {
		{
			addAll(cardSystemAttributes);
			for (final ProcessAttributes a : ProcessAttributes.values()) {
				add(a.dbColumnName());
			}
		}
	};

	@SuppressWarnings("serial")
	private static final Map<String, WSProcessInstanceState> stateCodeToEnumMap = new HashMap<String, WSProcessInstanceState>() {
		{
			put(SharkConstants.STATE_OPEN_RUNNING, WSProcessInstanceState.OPEN);
			put(SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED, WSProcessInstanceState.SUSPENDED);
			put(SharkConstants.STATE_CLOSED_COMPLETED, WSProcessInstanceState.COMPLETED);
			put(SharkConstants.STATE_CLOSED_TERMINATED, WSProcessInstanceState.TERMINATED);
			put(SharkConstants.STATE_CLOSED_ABORTED, WSProcessInstanceState.ABORTED);
		}
	};

	private final UserContext userCtx;
	private final ProcessDefinitionManager processDefinitionManager;
	private final CMWorkflowService workflowService;

	public ProcessInstanceWrapper(final UserContext userCtx, final ProcessDefinitionManager processDefinitionManager,
			final ICard process) {
		super(process);
		this.userCtx = userCtx;
		this.processDefinitionManager = processDefinitionManager;
		this.workflowService = TemporaryObjectsBeforeSpringDI.getWorkflowService();
	}

	@Override
	protected boolean isUserAttributeName(final String name) {
		return !processSystemAttributes.contains(name);
	}

	@Override
	public Long getCardId() {
		return getId();
	}

	@Override
	public String getProcessInstanceId() {
		return card.getAttributeValue(ProcessAttributes.ProcessInstanceId.dbColumnName()).getString();
	}

	private String[] getActivityInstanceIds() {
		return card.getAttributeValue(ProcessAttributes.ActivityInstanceId.dbColumnName()).getStringArrayValue();
	}

	private String[] getActivityDefinitionIds() {
		return card.getAttributeValue(ProcessAttributes.ActivityDefinitionId.dbColumnName()).getStringArrayValue();
	}

	private String[] getActivityInstancePerformers() {
		return card.getAttributeValue(ProcessAttributes.CurrentActivityPerformers.dbColumnName()).getStringArrayValue();
	}

	private String[] getAllActivityPerformers() {
		return card.getAttributeValue(ProcessAttributes.AllActivityPerformers.dbColumnName()).getStringArrayValue();
	}

	@Override
	public CMProcessClass getType() {
		return new ProcessClassWrapper(userCtx, card.getSchema(), processDefinitionManager);
	}

	@Override
	public List<UserActivityInstance> getActivities() {
		final List<UserActivityInstance> out = new ArrayList<UserActivityInstance>();
		final String[] ais = getActivityInstanceIds();
		final String[] ads = getActivityDefinitionIds();
		final String[] perfs = getActivityInstancePerformers();
		for (int i = 0; i < ais.length; ++i) {
			out.add(new ActivityInstanceImpl(ais[i], perfs[i], ads[i]));
		}
		return out;
	}

	@Override
	public UserActivityInstance getActivityInstance(final String activityInstanceId) {
		for (final UserActivityInstance ai : getActivities()) {
			if (ai.getId().equals(activityInstanceId)) {
				return ai;
			}
		}

		return null;
	}

	/*
	 * CMProcessInstanceDefinition
	 */

	/**
	 * Sets only non-system values
	 */
	@Override
	public UserProcessInstanceDefinition set(final String key, final Object value) {
		setOnly(key, value);
		return this;
	}

	@Override
	public UserProcessInstance save() {
		card.save();
		return this;
	}

	@Override
	public UserProcessInstanceDefinition setActivities(final WSActivityInstInfo[] activityInfos)
			throws CMWorkflowException {
		removeClosedActivities(activityInfos);
		addNewActivities(activityInfos);
		updateCodeWithOneRandomActivityInfo();
		return this;
	}

	private void removeClosedActivities(final WSActivityInstInfo[] activityInfos) throws CMWorkflowException {
		final Set<String> newActivityInstInfoIds = new HashSet<String>(activityInfos.length);
		for (final WSActivityInstInfo ai : activityInfos) {
			newActivityInstInfoIds.add(ai.getActivityInstanceId());
		}
		for (final String oldActInstId : getActivityInstanceIds()) {
			if (newActivityInstInfoIds.contains(oldActInstId))
				continue;
			removeActivity(oldActInstId);
		}
	}

	private void addNewActivities(final WSActivityInstInfo[] activityInfos) throws CMWorkflowException {
		final Set<String> oldActivityInstanceIds = new HashSet<String>();
		for (final String aiid : getActivityInstanceIds()) {
			oldActivityInstanceIds.add(aiid);
		}
		for (final WSActivityInstInfo ai : activityInfos) {
			if (oldActivityInstanceIds.contains(ai.getActivityInstanceId()))
				continue;
			addActivity(ai);
		}
	}

	private void updateCodeWithOneRandomActivityInfo() throws CMWorkflowException {
		final List<UserActivityInstance> activities = getActivities();
		final String code;
		if (activities.isEmpty()) {
			code = null;
		} else {
			final CMActivity randomActivity = activities.get(0).getDefinition();
			final String randomActivityLabel;
			if (randomActivity.getDescription() != null) {
				randomActivityLabel = randomActivity.getDescription();
			} else {
				randomActivityLabel = StringUtils.EMPTY;
			}
			if (activities.size() > 1) {
				code = String.format("%s, ...", randomActivityLabel);
			} else {
				code = randomActivityLabel;
			}
		}
		card.setCode(code);
	}

	@Override
	public void addActivity(final WSActivityInstInfo activityInfo) throws CMWorkflowException {
		Validate.notNull(activityInfo);
		Validate.notNull(activityInfo.getActivityInstanceId());
		final String participantGroup = extractActivityParticipantGroup(activityInfo);
		if (participantGroup != UNRESOLVABLE_PARTICIPANT_GROUP) {
			card.setValue(ProcessAttributes.ActivityInstanceId.dbColumnName(),
					addToBack(getActivityInstanceIds(), activityInfo.getActivityInstanceId()));
			card.setValue(ProcessAttributes.ActivityDefinitionId.dbColumnName(),
					addToBack(getActivityDefinitionIds(), activityInfo.getActivityDefinitionId()));
	
			card.setValue(ProcessAttributes.CurrentActivityPerformers.dbColumnName(),
					addToBack(getActivityInstancePerformers(), participantGroup));
			card.setValue(ProcessAttributes.AllActivityPerformers.dbColumnName(),
					addDistinct(getAllActivityPerformers(), participantGroup));
	
			updateCodeWithOneRandomActivityInfo();
		}
	}

	@Override
	public void removeActivity(final String activityInstanceId) throws CMWorkflowException {
		final int index = ArrayUtils.indexOf(getActivityInstanceIds(), activityInstanceId);
		if (index == ArrayUtils.INDEX_NOT_FOUND) {
			return;
		}
		card.setValue(ProcessAttributes.ActivityInstanceId.dbColumnName(),
				ArrayUtils.remove(getActivityInstanceIds(), index));
		card.setValue(ProcessAttributes.ActivityDefinitionId.dbColumnName(),
				ArrayUtils.remove(getActivityDefinitionIds(), index));

		card.setValue(ProcessAttributes.CurrentActivityPerformers.dbColumnName(),
				ArrayUtils.remove(getActivityInstancePerformers(), index));

		updateCodeWithOneRandomActivityInfo();
	}

	private String extractActivityParticipantGroup(final WSActivityInstInfo activityInfo) throws CMWorkflowException {
		final CMActivity activity = findActivity(activityInfo.getActivityDefinitionId());
		final ActivityPerformer performer = activity.getFirstNonAdminPerformer();
		switch (performer.getType()) {
		case ROLE:
			return performer.getValue();
		case EXPRESSION:
			try {
				return evaluatePerformerExpression(performer.getValue());
			} catch (final Exception e) {
				// LOG and ignore
			}
		}
		return UNRESOLVABLE_PARTICIPANT_GROUP;
	}

	/*
	 * It can be extracted to a strategy, optimized, etc.
	 */
	private String evaluatePerformerExpression(final String expression) throws CMWorkflowException, EvalError {
		final String procInstId = getProcessInstanceId();
		final Map<String, Object> rawWorkflowVars = workflowService.getProcessInstanceVariables(procInstId);
		final bsh.Interpreter interpreter = new bsh.Interpreter();
		for (final Map.Entry<String, Object> entry : rawWorkflowVars.entrySet()) {
			interpreter.set(entry.getKey(), entry.getValue());
		}
		return interpreter.eval(expression).toString();
	}

	private CMActivity findActivity(final String activityDefinitionId) throws CMWorkflowException {
		return processDefinitionManager.getActivity(this, activityDefinitionId);
	}

	private String[] addToBack(final String[] original, final String element) {
		return (String[]) ArrayUtils.add(original, element);
	}

	private String[] addDistinct(final String[] original, final String element) {
		if (element == null) {
			return original;
		}
		for (final String origElement : original) {
			if (element.equals(origElement)) {
				return original;
			}
		}
		return addToBack(original, element);
	}

	@Override
	public UserProcessInstanceDefinition setState(final WSProcessInstanceState state) {
		final Lookup flowStatusLookup = lookupForFlowStatus(state);
		card.setValue(ProcessAttributes.FlowStatus.dbColumnName(), flowStatusLookup);
		return this;
	}

	@Override
	public WSProcessInstanceState getState() {
		final Lookup flowStatusLookup = card.getAttributeValue(ProcessAttributes.FlowStatus.dbColumnName()).getLookup();
		return getFlowStatusForLookup(flowStatusLookup);
	}

	@Override
	public WSProcessDefInfo getUniqueProcessDefinition() {
		final String value = card.getAttributeValue(ProcessAttributes.UniqueProcessDefinition.dbColumnName())
				.getString();
		if (value != null) {
			final String[] components = value.split("#");
			if (components.length == 3) {
				return WSProcessDefInfoImpl.newInstance(components[0], components[1], components[2]);
			}
		}
		return null;
	}

	@Override
	public UserProcessInstanceDefinition setUniqueProcessDefinition(final WSProcessDefInfo info) {
		final String value = String.format("%s#%s#%s", info.getPackageId(), info.getPackageVersion(),
				info.getProcessDefinitionId());
		card.setValue(ProcessAttributes.UniqueProcessDefinition.dbColumnName(), value);
		return this;
	}

	/*
	 * From the Proterozoic Eon
	 */
	public static Lookup lookupForFlowStatus(final WSProcessInstanceState state) {
		final String flowStatusLookupCode;
		switch (state) {
		case OPEN:
			flowStatusLookupCode = SharkConstants.STATE_OPEN_RUNNING;
			break;
		case SUSPENDED:
			flowStatusLookupCode = SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED;
			break;
		case COMPLETED:
			flowStatusLookupCode = SharkConstants.STATE_CLOSED_COMPLETED;
			break;
		case TERMINATED:
			flowStatusLookupCode = SharkConstants.STATE_CLOSED_TERMINATED;
			break;
		case ABORTED:
			flowStatusLookupCode = SharkConstants.STATE_CLOSED_ABORTED;
			break;
		default:
			flowStatusLookupCode = null;
		}
		return lookupForFlowStatusCode(flowStatusLookupCode);
	}

	public static Lookup lookupForFlowStatusCode(final String flowStatusLookupCode) {
		return CMBackend.INSTANCE.getFirstLookupByCode(FLOW_STATUS_LOOKUP, flowStatusLookupCode);
	}

	private static WSProcessInstanceState getFlowStatusForLookup(final Lookup flowStatusLookup) {
		if (flowStatusLookup == null) {
			return null;
		}
		final String flowStatusLookupCode = flowStatusLookup.getCode();
		final WSProcessInstanceState state = stateCodeToEnumMap.get(flowStatusLookupCode);
		if (state == null) {
			return WSProcessInstanceState.UNSUPPORTED;
		} else {
			return state;
		}
	}

	public static ProcessInstanceWrapper createProcessInstance(final UserContext userCtx,
			final ProcessDefinitionManager processDefinitionManager, final ProcessType processType,
			final WSProcessInstInfo procInst) {
		final Process process = processType.cards().create();
		process.setValue(ProcessAttributes.ProcessInstanceId.dbColumnName(), procInst.getProcessInstanceId());
		process.setValue(ProcessAttributes.ActivityInstanceId.dbColumnName(), ArrayUtils.EMPTY_STRING_ARRAY);
		process.setValue(ProcessAttributes.ActivityDefinitionId.dbColumnName(), ArrayUtils.EMPTY_STRING_ARRAY);
		process.setValue(ProcessAttributes.CurrentActivityPerformers.dbColumnName(), ArrayUtils.EMPTY_STRING_ARRAY);
		process.setValue(ProcessAttributes.AllActivityPerformers.dbColumnName(), ArrayUtils.EMPTY_STRING_ARRAY);
		final ProcessInstanceWrapper wrapper = new ProcessInstanceWrapper(userCtx, processDefinitionManager, process);
		wrapper.setState(WSProcessInstanceState.OPEN);
		wrapper.setUniqueProcessDefinition(procInst);
		return wrapper;
	}

	public static UserProcessInstanceDefinition readProcessInstance(final UserContext userCtx,
			final ProcessDefinitionManager processDefinitionManager, final ProcessType processType,
			final CMProcessInstance processInstance) {
		final int cardId = Integer.valueOf(processInstance.getCardId().toString()).intValue();
		final Process process = processType.cards().get(cardId);
		return new ProcessInstanceWrapper(userCtx, processDefinitionManager, process);
	}

}

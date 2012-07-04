package org.cmdbuild.dao.legacywrappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.Process;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMProcessInstance.CMProcessInstanceDefinition;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessDefInfo;
import org.cmdbuild.workflow.service.WSProcessDefInfoImpl;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.enhydra.shark.api.common.SharkConstants;

public class ProcessInstanceWrapper extends CardWrapper implements CMProcessInstance, CMProcessInstanceDefinition {

	private static final String FLOW_STATUS_LOOKUP = "FlowStatus";

	private class ActivityInstanceImpl implements CMActivityInstance {

		final String activityInstanceId;
		final String activityInstancePerformer;
		final String activityDefinitionId;

		public ActivityInstanceImpl(final String activityInstanceId, final String activityInstancePerformer, final String activityDefinitionId) {
			this.activityInstanceId = activityInstanceId;
			this.activityInstancePerformer = activityInstancePerformer;
			this.activityDefinitionId = activityDefinitionId;
		}

		@Override
		public CMProcessInstance getProcessInstance() {
			return ProcessInstanceWrapper.this;
		}

		@Override
		public String getId() {
			return activityInstanceId;
		}

		@Override
		public CMActivity getDefinition() throws CMWorkflowException {
			return getActivity(activityDefinitionId);
		}

		@Override
		public String getPerformerName() {
			return activityInstancePerformer;
		}
		
	}

	@SuppressWarnings("serial")
	protected static final Set<String> processSystemAttributes = new HashSet<String>() {{
		addAll(cardSystemAttributes);
		for (final ProcessAttributes a : ProcessAttributes.values()) {
			add(a.dbColumnName());
		}
	}};

	@SuppressWarnings("serial")
	private static final Map<String, WSProcessInstanceState> stateCodeToEnumMap = new HashMap<String, WSProcessInstanceState>() {{
		put(SharkConstants.STATE_OPEN_RUNNING, WSProcessInstanceState.OPEN);
		put(SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED, WSProcessInstanceState.SUSPENDED);
		put(SharkConstants.STATE_CLOSED_COMPLETED, WSProcessInstanceState.COMPLETED);
		put(SharkConstants.STATE_CLOSED_TERMINATED, WSProcessInstanceState.TERMINATED);
		put(SharkConstants.STATE_CLOSED_ABORTED, WSProcessInstanceState.ABORTED);
	}};


	private final UserContext userCtx;
	private final ProcessDefinitionManager processDefinitionManager;

	public ProcessInstanceWrapper(final UserContext userCtx,
			final ProcessDefinitionManager processDefinitionManager,
			final ICard process) {
		super(process);
		this.userCtx = userCtx;
		this.processDefinitionManager = processDefinitionManager;
	}

	protected boolean isUserAttributeName(final String name) {
		return !processSystemAttributes.contains(name);
	}

	@Override
	public Object getCardId() {
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
	public List<CMActivityInstance> getActivities() {
		final List<CMActivityInstance> out = new ArrayList<CMActivityInstance>();
		final String[] ais = getActivityInstanceIds();
		final String[] ads = getActivityDefinitionIds();
		final String[] perfs = getActivityInstancePerformers();
		for (int i = 0; i < ais.length; ++i) {
			out.add(new ActivityInstanceImpl(ais[i], perfs[i], ads[i]));
		}
		return out;
	}

	/*
	 * CMProcessInstanceDefinition
	 */

	/**
	 * Sets only non-system values
	 */
	@Override
	public CMProcessInstanceDefinition set(final String key, final Object value) {
		if (isUserAttributeName(key)) {
			card.setValue(key, value);
		}
		return this;
	}

	@Override
	public CMProcessInstance save() {
		card.save();
		return this;
	}

	@Override
	public CMProcessInstanceDefinition setActivities(WSActivityInstInfo[] activityInfos) throws CMWorkflowException {
		removeClosedActivities(activityInfos);
		addNewActivities(activityInfos);
		return this;
	}

	private void removeClosedActivities(WSActivityInstInfo[] activityInfos) {
		final Set<String> newActivityInstInfoIds = new HashSet<String>(activityInfos.length);
		for (final WSActivityInstInfo ai : activityInfos) {
			newActivityInstInfoIds.add(ai.getActivityInstanceId());
		}
		for (final String oldActInstId : getActivityInstanceIds()) {
			if (newActivityInstInfoIds.contains(oldActInstId))
				return;
			removeActivity(oldActInstId);
		}
	}

	private void addNewActivities(WSActivityInstInfo[] activityInfos) throws CMWorkflowException {
		final Set<String> oldActivityInstanceIds = new HashSet<String>();
		for (final String aiid : getActivityInstanceIds()) {
			oldActivityInstanceIds.add(aiid);
		}
		for (final WSActivityInstInfo ai : activityInfos) {
			if (oldActivityInstanceIds.contains(ai.getActivityInstanceId()))
				return;
			addActivity(ai);
		}
	}

	@Override
	public void addActivity(final WSActivityInstInfo activityInfo) throws CMWorkflowException {
		Validate.notNull(activityInfo);
		Validate.notNull(activityInfo.getActivityInstanceId());
		card.setValue(ProcessAttributes.ActivityInstanceId.dbColumnName(),
				addToBack(getActivityInstanceIds(), activityInfo.getActivityInstanceId()));
		card.setValue(ProcessAttributes.ActivityDefinitionId.dbColumnName(),
				addToBack(getActivityDefinitionIds(), activityInfo.getActivityDefinitionId()));

		final String participantGroup = getActivityParticipantGroup(activityInfo);
		card.setValue(ProcessAttributes.CurrentActivityPerformers.dbColumnName(),
				addToBack(getActivityInstancePerformers(), participantGroup));
		card.setValue(ProcessAttributes.AllActivityPerformers.dbColumnName(),
				addDistinct(getAllActivityPerformers(), participantGroup));
	}

	@Override
	public void removeActivity(final String activityInstanceId) {
		int index = ArrayUtils.indexOf(getActivityInstanceIds(), activityInstanceId);
		if (index == ArrayUtils.INDEX_NOT_FOUND) {
			return;
		}
		card.setValue(ProcessAttributes.ActivityInstanceId.dbColumnName(),
				ArrayUtils.remove(getActivityInstanceIds(), index));
		card.setValue(ProcessAttributes.ActivityDefinitionId.dbColumnName(),
				ArrayUtils.remove(getActivityDefinitionIds(), index));

		card.setValue(ProcessAttributes.CurrentActivityPerformers.dbColumnName(),
				ArrayUtils.remove(getActivityInstancePerformers(), index));
	}

	private String getActivityParticipantGroup(WSActivityInstInfo activityInfo) throws CMWorkflowException {
		final CMActivity activity = getActivity(activityInfo.getActivityDefinitionId());
		// TODO Check if participant is a role in the xpdl or not!
		return activity.getFirstRolePerformer().getName();
	}

	private CMActivity getActivity(final String activityInstanceId) throws CMWorkflowException {
		return processDefinitionManager.getActivity(this, activityInstanceId);
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
	public CMProcessInstanceDefinition setState(final WSProcessInstanceState state) {
		final Lookup flowStatusLookup = lookupForFlowStatus(state);
		card.setValue(ProcessAttributes.FlowStatus.dbColumnName(), flowStatusLookup);
		return this;
	}

	@Override
	public WSProcessInstanceState getState() {
		Lookup flowStatusLookup = card.getAttributeValue(ProcessAttributes.FlowStatus.dbColumnName()).getLookup();
		return getFlowStatusForLookup(flowStatusLookup);
	}


	@Override
	public WSProcessDefInfo getUniqueProcessDefinition() {
		final String value = card.getAttributeValue(ProcessAttributes.UniqueProcessDefinition.dbColumnName()).getString();
		if (value != null) {
			final String[] components = value.split("#");
			if (components.length == 3) {
				return WSProcessDefInfoImpl.newInstance(components[0], components[1], components[2]);
			}
		}
		return null;
	}

	@Override
	public CMProcessInstanceDefinition setUniqueProcessDefinition(final WSProcessDefInfo info) {
		final String value = String.format("%s#%s#%s", info.getPackageId(), info.getPackageVersion(), info.getProcessDefinitionId());
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

	public static ProcessInstanceWrapper createProcessInstance(
			final UserContext userCtx,
			final ProcessDefinitionManager processDefinitionManager,
			final ProcessType processType,
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

	public static CMProcessInstanceDefinition readProcessInstance(
			final UserContext userCtx,
			final ProcessDefinitionManager processDefinitionManager,
			final ProcessType processType,
			final CMProcessInstance processInstance) {
		int cardId = Integer.valueOf(processInstance.getCardId().toString()).intValue();
		final Process process = processType.cards().get(cardId);
		return new ProcessInstanceWrapper(userCtx, processDefinitionManager, process);
	}

}

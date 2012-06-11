package org.cmdbuild.dao.legacywrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.Process;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMProcessInstance.CMProcessInstanceDefinition;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.enhydra.shark.api.common.SharkConstants;

public class ProcessInstanceWrapper extends CardWrapper implements CMProcessInstance, CMProcessInstanceDefinition {

	private static final String FLOW_STATUS_LOOKUP = "FlowStatus";

	private class ActivityInstanceImpl implements CMActivityInstance {

		final String activityInstanceId;

		public ActivityInstanceImpl(final String activityInstanceId) {
			this.activityInstanceId = activityInstanceId;
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
		public CMActivity getDefinition() {
			throw new UnsupportedOperationException("Is it really needed?");
		}
		
	}

	protected static final Set<String> processSystemAttributes;

	static {
		processSystemAttributes = new HashSet<String>();
		processSystemAttributes.addAll(cardSystemAttributes);
		for (final ProcessAttributes a : ProcessAttributes.values()) {
			processSystemAttributes.add(a.dbColumnName());
		}
	}

	private final UserContext userCtx;
	private final ProcessDefinitionManager processDefinitionManager;
	private final Process process;

	public ProcessInstanceWrapper(final UserContext userCtx,
			final ProcessDefinitionManager processDefinitionManager,
			final Process process) {
		super(process);
		this.userCtx = userCtx;
		this.processDefinitionManager = processDefinitionManager;
		this.process = process;
	}

	protected boolean isUserAttributeName(final String name) {
		return processSystemAttributes.contains(name);
	}

	@Override
	public Object getCardId() {
		return getId();
	}

	@Override
	public String getProcessInstanceId() {
		return process.getAttributeValue(ProcessAttributes.ProcessInstanceId.dbColumnName()).getString();
	}

	private String[] getActivityInstanceIds() {
		return process.getAttributeValue(ProcessAttributes.ActivityInstanceId.dbColumnName()).getStringArrayValue();
	}

	private String[] getActivityDefinitionNames() {
		return process.getAttributeValue(ProcessAttributes.ActivityDefinitionName.dbColumnName()).getStringArrayValue();
	}

	private String[] getActivityPerformers() {
		return process.getAttributeValue(ProcessAttributes.CurrentActivityPerformers.dbColumnName()).getStringArrayValue();
	}

	@Override
	public CMProcessClass getType() {
		return new ProcessClassWrapper(userCtx, process.getSchema(), processDefinitionManager);
	}

	@Override
	public List<CMActivityInstance> getActivities() {
		final List<CMActivityInstance> ais = new ArrayList<CMActivityInstance>();
		for (final String aid : getActivityInstanceIds()) {
			ais.add(new ActivityInstanceImpl(aid));
		}
		return ais;
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
			process.setValue(key, value);
		}
		return this;
	}

	@Override
	public CMProcessInstance save() {
		process.save();
		return this;
	}

	@Override
	public void addActivity(final WSActivityInstInfo activityInfo) {
		process.setValue(ProcessAttributes.ActivityInstanceId.dbColumnName(),
				addToBack(getActivityInstanceIds(), activityInfo.getActivityInstanceId()));
		// TODO ActivityDefinitionNames, ActivityPerformers
	}

	public String[] addToBack(final String[] original, final String element) {
		String[] out = Arrays.copyOf(original, original.length+1);
		out[original.length] = element;
		return out;
	}

	public void setState(final WSProcessInstanceState state) {
		final Lookup flowStatusLookup = lookupForFlowStatus(state);
		process.setValue(ProcessAttributes.FlowStatus.dbColumnName(), flowStatusLookup);
	}

	/*
	 * From the Proterozoic Eon
	 */
	private Lookup lookupForFlowStatus(final WSProcessInstanceState state) {
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

	public static ProcessInstanceWrapper newInstance(
			final UserContext userCtx,
			final ProcessDefinitionManager processDefinitionManager,
			final ProcessType processType,
			final String procInstId) {
		final Process process = processType.cards().create();
		process.setValue(ProcessAttributes.ProcessInstanceId.dbColumnName(), procInstId);
		process.setValue(ProcessAttributes.ActivityInstanceId.dbColumnName(), ArrayUtils.EMPTY_STRING_ARRAY);
		process.setValue(ProcessAttributes.ActivityDefinitionName.dbColumnName(), ArrayUtils.EMPTY_STRING_ARRAY);
		process.setValue(ProcessAttributes.CurrentActivityPerformers.dbColumnName(), ArrayUtils.EMPTY_STRING_ARRAY);
		process.setValue(ProcessAttributes.AllActivityPerformers.dbColumnName(), ArrayUtils.EMPTY_STRING_ARRAY);
		final ProcessInstanceWrapper wrapper = new ProcessInstanceWrapper(userCtx, processDefinitionManager, process);
		wrapper.setState(WSProcessInstanceState.OPEN);
		return wrapper;
	}

}

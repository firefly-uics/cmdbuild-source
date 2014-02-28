package org.cmdbuild.workflow;

import java.util.List;

import org.cmdbuild.dao.entry.ForwardingCard;
import org.cmdbuild.workflow.service.WSProcessDefInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;

public class ForwardingProcessInstance extends ForwardingCard implements CMProcessInstance {

	private final CMProcessInstance inner;

	public ForwardingProcessInstance(final CMProcessInstance inner) {
		super(inner);
		this.inner = inner;
	}

	@Override
	public CMProcessClass getType() {
		return inner.getType();
	}

	@Override
	public Long getCardId() {
		return inner.getCardId();
	}

	@Override
	public String getProcessInstanceId() {
		return inner.getProcessInstanceId();
	}

	@Override
	public WSProcessInstanceState getState() {
		return inner.getState();
	}

	@Override
	public List<? extends CMActivityInstance> getActivities() {
		return inner.getActivities();
	}

	@Override
	public CMActivityInstance getActivityInstance(final String activityInstanceId) {
		return inner.getActivityInstance(activityInstanceId);
	}

	@Override
	public WSProcessDefInfo getUniqueProcessDefinition() {
		return inner.getUniqueProcessDefinition();
	}

}

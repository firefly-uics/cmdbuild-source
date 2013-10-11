package org.cmdbuild.workflow.user;

import java.util.List;

import org.cmdbuild.workflow.ForwardingProcessInstance;

public class ForwardingUserProcessInstance extends ForwardingProcessInstance implements UserProcessInstance {

	private final UserProcessInstance inner;

	public ForwardingUserProcessInstance(final UserProcessInstance inner) {
		super(inner);
		this.inner = inner;
	}

	@Override
	public List<UserActivityInstance> getActivities() {
		return inner.getActivities();
	}

	@Override
	public UserActivityInstance getActivityInstance(final String activityInstanceId) {
		return inner.getActivityInstance(activityInstanceId);
	}

}

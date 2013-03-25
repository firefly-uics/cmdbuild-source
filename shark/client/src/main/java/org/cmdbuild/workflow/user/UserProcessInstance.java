package org.cmdbuild.workflow.user;

import java.util.List;

import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessDefInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;

/**
 * Process instance used by a user.
 */
public interface UserProcessInstance extends CMProcessInstance {

	/**
	 * {@link UserProcessInstance} mutator object.
	 */
	interface UserProcessInstanceDefinition extends CMProcessInstanceDefinition {

		@Override
		UserProcessInstanceDefinition set(String key, Object value);

		@Override
		UserProcessInstanceDefinition setActivities(WSActivityInstInfo[] activityInfos) throws CMWorkflowException;

		@Override
		UserProcessInstanceDefinition addActivity(WSActivityInstInfo activityInfo) throws CMWorkflowException;

		@Override
		UserProcessInstanceDefinition removeActivity(String activityInstanceId) throws CMWorkflowException;

		@Override
		UserProcessInstanceDefinition setState(WSProcessInstanceState state);

		@Override
		UserProcessInstanceDefinition setUniqueProcessDefinition(WSProcessDefInfo info);

		@Override
		UserProcessInstance save();

	}

	@Override
	List<UserActivityInstance> getActivities();

	@Override
	UserActivityInstance getActivityInstance(String activityInstanceId);

}

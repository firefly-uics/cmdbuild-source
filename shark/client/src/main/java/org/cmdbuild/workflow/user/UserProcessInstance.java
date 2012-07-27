package org.cmdbuild.workflow.user;

import java.util.List;

import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessDefInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;

/**
 * Process instance used by a user
 */
public interface UserProcessInstance extends CMProcessInstance {

	interface UserProcessInstanceDefinition extends CMProcessInstanceDefinition {
		UserProcessInstanceDefinition set(String key, Object value);
		UserProcessInstanceDefinition setActivities(WSActivityInstInfo[] activityInfos) throws CMWorkflowException;
		UserProcessInstanceDefinition setState(WSProcessInstanceState state);
		UserProcessInstanceDefinition setUniqueProcessDefinition(WSProcessDefInfo info);
		UserProcessInstance save();
	}

	List<UserActivityInstance> getActivities();
	UserActivityInstance getActivityInstance(String activityInstanceId);
}

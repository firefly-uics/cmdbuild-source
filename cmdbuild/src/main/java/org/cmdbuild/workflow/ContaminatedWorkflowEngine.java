package org.cmdbuild.workflow;

import org.cmdbuild.common.utils.PaginatedElements;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.user.UserWorkflowEngine;

// TODO find a better name
public interface ContaminatedWorkflowEngine extends UserWorkflowEngine {

	PaginatedElements<UserProcessInstance> query(String className, QueryOptions queryOptions);

}

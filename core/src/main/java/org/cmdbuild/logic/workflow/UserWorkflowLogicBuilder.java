package org.cmdbuild.logic.workflow;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.common.Builder;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.workflow.DefaultWorkflowEngine;

public class UserWorkflowLogicBuilder extends WorkflowLogicBuilder {

	public UserWorkflowLogicBuilder( //
			final PrivilegeContext privilegeContext, //
			final Builder<DefaultWorkflowEngine> workflowEngineBuilder, //
			final CMDataView dataView, //
			final WorkflowConfiguration configuration, //
			final FilesStore filesStore) {
		super(privilegeContext, workflowEngineBuilder, dataView, configuration, filesStore);
	}

}

package org.cmdbuild.logic.workflow;

import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.LockLogic;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.workflow.DefaultWorkflowEngine;

public class WebserviceWorkflowLogicBuilder extends WorkflowLogicBuilder {

	public WebserviceWorkflowLogicBuilder( //
			final OperationUser operationUser, //
			final PrivilegeContext privilegeContext, //
			final Builder<DefaultWorkflowEngine> workflowEngineBuilder, //
			final CMDataView dataView, //
			final WorkflowConfiguration configuration, //
			final FilesStore filesStore, //
			final LockLogic lockLogic //
	) {
		super(operationUser, privilegeContext, workflowEngineBuilder, dataView, configuration, filesStore, lockLogic);
	}

}

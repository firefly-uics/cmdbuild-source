package org.cmdbuild.logic.workflow;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.common.Builder;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.workflow.DefaultWorkflowEngine;
import org.springframework.beans.factory.annotation.Autowired;

public class SystemWorkflowLogicBuilder extends WorkflowLogicBuilder {

	@Autowired
	public SystemWorkflowLogicBuilder( //
			final PrivilegeContext privilegeContext, //
			final Builder<DefaultWorkflowEngine> workflowEngineBuilder, //
			final CMDataView dataView, //
			final CMDataView systemDataView, //
			final LookupStore lookupStore, //
			final WorkflowConfiguration configuration, //
			final FilesStore filesStore) {
		super(privilegeContext, workflowEngineBuilder, dataView, systemDataView, lookupStore, configuration, filesStore);
	}

}

package org.cmdbuild.logic.workflow;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.common.Builder;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.spring.annotations.LogicComponent;
import org.cmdbuild.workflow.QueryableUserWorkflowEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@LogicComponent
public class SystemWorkflowLogicBuilder extends WorkflowLogicBuilder {

	@Autowired
	public SystemWorkflowLogicBuilder( //
			@Qualifier("system") final PrivilegeContext privilegeContext, //
			@Qualifier("system") final Builder<QueryableUserWorkflowEngine> workflowEngineBuilder, //
			@Qualifier("system") final CMDataView dataView, //
			@Qualifier("system") final CMDataView systemDataView, //
			final LookupStore lookupStore, //
			final WorkflowConfiguration configuration, //
			final FilesStore filesStore) {
		super(privilegeContext, workflowEngineBuilder, dataView, systemDataView, lookupStore, configuration, filesStore);
	}

}

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
public class UserWorkflowLogicBuilder extends WorkflowLogicBuilder {

	@Autowired
	public UserWorkflowLogicBuilder( //
			@Qualifier("user") final PrivilegeContext privilegeContext, //
			@Qualifier("user") final Builder<QueryableUserWorkflowEngine> workflowEngineBuilder, //
			@Qualifier("user") final CMDataView dataView, //
			@Qualifier("system") final CMDataView systemDataView, //
			final LookupStore lookupStore, //
			final WorkflowConfiguration configuration, //
			final FilesStore filesStore) {
		super(privilegeContext, workflowEngineBuilder, dataView, systemDataView, lookupStore, configuration, filesStore);
	}

}

package org.cmdbuild.logic.email.rules;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.email.rules.StartWorkflow.Configuration;
import org.cmdbuild.logic.workflow.WorkflowLogic;

public class StartWorkflowFactory implements RuleFactory<StartWorkflow> {

	private final WorkflowLogic workflowLogic;
	private final CMDataView dataView;
	private final Configuration confguration;

	public StartWorkflowFactory( //
			final WorkflowLogic workflowLogic, //
			final CMDataView dataView //
	) {
		this(workflowLogic, dataView, null);
	}

	public StartWorkflowFactory( //
			final WorkflowLogic workflowLogic, //
			final CMDataView dataView, //
			final Configuration configuration //
	) {
		this.workflowLogic = workflowLogic;
		this.dataView = dataView;
		this.confguration = configuration;
	}

	@Override
	public StartWorkflow create() {
		return create(confguration);
	}

	public StartWorkflow create(final Configuration mapper) {
		Validate.notNull(workflowLogic, "null workflow logic");
		Validate.notNull(mapper, "null configuration");
		return new StartWorkflow(workflowLogic, dataView, mapper);
	}

}

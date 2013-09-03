package org.cmdbuild.logic.email.rules;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.email.rules.StartWorkflow.Configuration;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.services.email.EmailPersistence;

public class StartWorkflowFactory implements RuleFactory<StartWorkflow> {

	private final WorkflowLogic workflowLogic;
	private final CMDataView dataView;
	private final EmailPersistence persistence;
	private final Configuration confguration;

	public StartWorkflowFactory( //
			final WorkflowLogic workflowLogic, //
			final CMDataView dataView, //
			final EmailPersistence persistence //
	) {
		this(workflowLogic, dataView, persistence, null);
	}

	public StartWorkflowFactory( //
			final WorkflowLogic workflowLogic, //
			final CMDataView dataView, //
			final EmailPersistence persistence, //
			final Configuration configuration //
	) {
		this.workflowLogic = workflowLogic;
		this.dataView = dataView;
		this.persistence = persistence;
		this.confguration = configuration;
	}

	@Override
	public StartWorkflow create() {
		return create(confguration);
	}

	public StartWorkflow create(final Configuration mapper) {
		Validate.notNull(workflowLogic, "null workflow logic");
		Validate.notNull(mapper, "null configuration");
		return new StartWorkflow(workflowLogic, dataView, persistence, mapper);
	}

}

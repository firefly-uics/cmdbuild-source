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
	private final AttachmentStoreFactory attachmentStoreFactory;

	public StartWorkflowFactory( //
			final WorkflowLogic workflowLogic, //
			final CMDataView dataView, //
			final EmailPersistence persistence, //
			final AttachmentStoreFactory attachmentStoreFactory //
	) {
		this(workflowLogic, dataView, persistence, attachmentStoreFactory, null);
	}

	public StartWorkflowFactory( //
			final WorkflowLogic workflowLogic, //
			final CMDataView dataView, //
			final EmailPersistence persistence, //
			final AttachmentStoreFactory attachmentStoreFactory, //
			final Configuration configuration //
	) {
		this.workflowLogic = workflowLogic;
		this.dataView = dataView;
		this.persistence = persistence;
		this.attachmentStoreFactory = attachmentStoreFactory;
		this.confguration = configuration;
	}

	@Override
	public StartWorkflow create() {
		return create(confguration);
	}

	public StartWorkflow create(final Configuration configuration) {
		Validate.notNull(workflowLogic, "null workflow logic");
		Validate.notNull(dataView, "null data view");
		Validate.notNull(persistence, "null persistence");
		Validate.notNull(attachmentStoreFactory, "null attachment store factory");
		Validate.notNull(configuration, "null configuration");
		return new StartWorkflow(workflowLogic, dataView, persistence, configuration, attachmentStoreFactory);
	}

}

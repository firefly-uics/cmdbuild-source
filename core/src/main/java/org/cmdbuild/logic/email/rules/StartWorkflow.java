package org.cmdbuild.logic.email.rules;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailCallbackHandler.RuleAction;
import org.cmdbuild.services.email.EmailPersistence;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.slf4j.Logger;

import com.google.common.collect.Maps;

public class StartWorkflow implements Rule {

	public static interface Configuration {

		Logger logger = StartWorkflow.logger;

		String getClassName();

		Mapper getMapper();

		boolean advance();

		boolean saveAttachments();

	}

	public static interface Mapper {

		Logger logger = StartWorkflow.logger;

		Object NULL_VALUE = null;

		Object getValue(String name);

	}

	private static final Logger logger = Logic.logger;

	private final WorkflowLogic workflowLogic;
	private final CMDataView dataView;
	private final EmailPersistence persistence;
	private final Configuration configuration;
	private final AttachmentStoreFactory attachmentStoreFactory;

	public StartWorkflow( //
			final WorkflowLogic workflowLogic, //
			final CMDataView dataView, //
			final EmailPersistence persistence, //
			final Configuration configuration, //
			final AttachmentStoreFactory attachmentStoreFactory //
	) {
		this.workflowLogic = workflowLogic;
		this.dataView = dataView;
		this.persistence = persistence;
		this.configuration = configuration;
		this.attachmentStoreFactory = attachmentStoreFactory;
	}

	@Override
	public boolean applies(final Email email) {
		return true;
	}

	@Override
	public Email adapt(final Email email) {
		return email;
	}

	@Override
	public RuleAction action(final Email email) {
		return new RuleAction() {

			private final Map<String, Object> NULL_WIDGET_SUBMISSIONS = Collections.<String, Object> emptyMap();

			private final Configuration _configuration = new ForwardingConfiguration(configuration) {

				@Override
				public Mapper getMapper() {
					return new ResolverMapper(configuration.getMapper(), new EmailLookupMapper(email), dataView);
				}

			};

			@Override
			public void execute() {
				logger.info("starting process instance for class '{}'", _configuration.getClassName());

				try {
					final UserProcessInstance processInstance = workflowLogic.startProcess( //
							_configuration.getClassName(), //
							variables(), //
							NULL_WIDGET_SUBMISSIONS, //
							_configuration.advance());

					email.setActivityId(processInstance.getCardId());
					persistence.save(email);

					if (_configuration.saveAttachments()) {
						final AttachmentStore attachmentStore = attachmentStoreFactory.create( //
								_configuration.getClassName(), //
								processInstance.getCardId());
						attachmentStore.store(email.getAttachments());
					}
				} catch (final CMWorkflowException e) {
					logger.error("error accessing workflow's api", e);
				}
			}

			private Map<String, Object> variables() throws CMWorkflowException {
				final CMActivity startActivity = workflowLogic.getStartActivity(_configuration.getClassName());
				final Iterable<CMActivityVariableToProcess> activityVariables = startActivity.getVariables();

				final Map<String, Object> variables = Maps.newHashMap();

				final Mapper mapper = _configuration.getMapper();
				for (final CMActivityVariableToProcess variable : activityVariables) {
					final String name = variable.getName();
					variables.put(name, mapper.getValue(name));
				}
				return variables;
			}

		};
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("classname", configuration.getClassName()) //
				.toString();
	}

}
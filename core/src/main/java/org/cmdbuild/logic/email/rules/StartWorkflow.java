package org.cmdbuild.logic.email.rules;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
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

	public StartWorkflow( //
			final WorkflowLogic workflowLogic, //
			final CMDataView dataView, //
			final EmailPersistence persistence, //
			final Configuration configuration //
	) {
		this.workflowLogic = workflowLogic;
		this.dataView = dataView;
		this.persistence = persistence;
		this.configuration = configuration;
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

			private final Configuration _configuration = new Configuration() {

				@Override
				public String getClassName() {
					return configuration.getClassName();
				}

				@Override
				public Mapper getMapper() {
					return new ResolverMapper(configuration.getMapper(), new EmailLookupMapper(email), dataView);
				}

			};

			@Override
			public void execute() {
				logger.info("starting process instance for class '{}'", _configuration.getClassName());

				try {
					final CMActivity startActivity = workflowLogic.getStartActivity(_configuration.getClassName());
					final Iterable<CMActivityVariableToProcess> activityVariables = startActivity.getVariables();

					final Map<String, Object> variables = Maps.newHashMap();

					final Mapper mapper = _configuration.getMapper();
					for (final CMActivityVariableToProcess variable : activityVariables) {
						final String name = variable.getName();
						variables.put(name, mapper.getValue(name));
					}
					final UserProcessInstance processInstance = workflowLogic.startProcess( //
							_configuration.getClassName(), //
							variables, //
							Collections.<String, Object> emptyMap(), //
							true);

					email.setActivityId(processInstance.getCardId());
					persistence.save(email);
				} catch (final CMWorkflowException e) {
					logger.error("error accessing workflow's api", e);
				}
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
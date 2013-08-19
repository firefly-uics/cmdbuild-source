package org.cmdbuild.logic.email.rules;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.logic.workflow.WorkflowLogicBuilder;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailCallbackHandler.RuleAction;
import org.cmdbuild.spring.annotations.LogicComponent;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.common.collect.Maps;

@LogicComponent
// TODO change into something else
public class StartWorkflow implements Rule {

	private static final Logger logger = Logic.logger;

	private static final Pattern pattern = Pattern.compile("[^\\[]*\\[\\s*(\\w+)\\s*\\](\\s+)?(.*)");

	private static final int CLASSNAME_GROUP = 1;

	private final WorkflowLogic workflowLogic;

	private Matcher matcher;
	private String className;

	@Autowired
	public StartWorkflow( //
			@Qualifier("system") final WorkflowLogicBuilder workflowLogicBuilder //
	) {
		this.workflowLogic = workflowLogicBuilder.build();
	}

	@Override
	public boolean applies(final Email email) {
		matcher = pattern.matcher(defaultIfBlank(email.getSubject(), EMPTY));
		matcher.find();

		if (!matcher.matches()) {
			return false;
		}

		className = matcher.group(CLASSNAME_GROUP);

		if (StringUtils.isBlank(className)) {
			return false;
		}

		return true;
	}

	@Override
	public Email adapt(final Email email) {
		return email;
	}

	@Override
	public RuleAction action(final Email email) {
		return new RuleAction() {

			@Override
			public void execute() {
				logger.info("starting process instance for class '{}'", className);

				try {
					final CMActivity startActivity = workflowLogic.getStartActivity(className);
					final Iterable<CMActivityVariableToProcess> activityVariables = startActivity.getVariables();

					final Properties properties = new Properties();
					properties.load(new StringReader(email.getContent()));

					final Map<String, Object> variables = Maps.newHashMap();

					for (final CMActivityVariableToProcess variable : activityVariables) {
						final String name = variable.getName();
						final String value = properties.getProperty(name);
						variables.put(name, value);
					}
					workflowLogic.startProcess( //
							className, //
							variables, //
							Collections.<String, Object> emptyMap(), //
							true);
				} catch (final CMWorkflowException e) {
					logger.error("error accessing workflow's api", e);
				} catch (final IOException e) {
					logger.error("error parsing email's content", e);
				}
			}

		};
	}

}
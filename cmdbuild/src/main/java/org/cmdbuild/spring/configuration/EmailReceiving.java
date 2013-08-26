package org.cmdbuild.spring.configuration;

import java.util.Arrays;

import org.cmdbuild.logic.email.EmailReceivingLogic;
import org.cmdbuild.logic.email.rules.AnswerToExistingMail;
import org.cmdbuild.logic.email.rules.StartWorkflow;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * 
 * Needed for cut circular dependency between {@link Workflow} and {@link Email}
 * .
 * 
 */
@ConfigurationComponent
public class EmailReceiving extends Email {

	@Autowired
	private Notifier notifier;

	@Autowired
	private SystemWorkflowLogicBuilder systemWorkflowLogicBuilder;

	@Bean
	protected Rule answerToExistingMail() {
		return new AnswerToExistingMail( //
				emailService(), //
				emailPersistence(), //
				subjectHandler(), //
				emailRecipientTemplateResolver());
	}

	@Bean
	protected Rule startWorkflow() {
		return new StartWorkflow(systemWorkflowLogicBuilder);
	}

	@Bean
	@Scope("prototype")
	public EmailReceivingLogic emailReceivingLogic() {
		return new EmailReceivingLogic( //
				emailService(), //
				Arrays.asList(answerToExistingMail(), startWorkflow()), //
				notifier);
	}

}

package org.cmdbuild.spring.configuration;

import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.logic.email.rules.AnswerToExistingMailFactory;
import org.cmdbuild.logic.email.rules.DownloadAttachmentsFactory;
import org.cmdbuild.logic.email.rules.StartWorkflowFactory;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

/**
 * 
 * Needed for cut circular dependency between {@link Workflow} and {@link Email}
 * .
 * 
 */
@ConfigurationComponent
public class EmailReceiving {

	@Autowired
	private Data data;

	@Autowired
	private Dms dms;

	@Autowired
	private DmsConfiguration dmsConfiguration;

	@Autowired
	private Email email;

	@Autowired
	private Notifier notifier;

	@Autowired
	private Workflow workflow;

	@Bean
	@Qualifier("default")
	public AnswerToExistingMailFactory answerToExistingFactory() {
		return new AnswerToExistingMailFactory( //
				email.defaultEmailService(), //
				email.emailPersistence(), //
				email.subjectHandler(), //
				email.emailRecipientTemplateResolver());
	}

	@Bean
	public DownloadAttachmentsFactory downloadAttachmentsFactory() {
		return new DownloadAttachmentsFactory( //
				dmsConfiguration, //
				dms.documentCreatorFactory(), //
				dms.dmsService(), //
				data.systemDataView());
	}

	@Bean
	public StartWorkflowFactory startWorkflowFactory() {
		return new StartWorkflowFactory( //
				workflow.systemWorkflowLogicBuilder().build(), //
				data.systemDataView());
	}

}

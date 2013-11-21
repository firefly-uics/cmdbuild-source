package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.DEFAULT;

import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.logic.email.rules.AnswerToExistingMailFactory;
import org.cmdbuild.logic.email.rules.AttachmentStoreFactory;
import org.cmdbuild.logic.email.rules.DefaultAttachmentStoreFactory;
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

	private static final String USER_FOR_UPLOADS = "system";

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
	@Qualifier(DEFAULT)
	public AnswerToExistingMailFactory answerToExistingFactory() {
		return new AnswerToExistingMailFactory( //
				email.defaultEmailService(), //
				email.emailPersistence(), //
				email.subjectHandler(), //
				email.dataFacade(), //
				data.systemDataView(), //
				data.lookupStore());
	}

	@Bean
	protected DownloadAttachmentsFactory downloadAttachmentsFactory() {
		return new DownloadAttachmentsFactory(attachmentStoreFactory());
	}

	@Bean
	public StartWorkflowFactory startWorkflowFactory() {
		return new StartWorkflowFactory( //
				workflow.systemWorkflowLogicBuilder().build(), //
				data.systemDataView(), //
				email.emailPersistence(), //
				attachmentStoreFactory());
	}

	@Bean
	protected AttachmentStoreFactory attachmentStoreFactory() {
		return new DefaultAttachmentStoreFactory( //
				data.systemDataView(), //
				dms.documentCreatorFactory(), //
				dmsConfiguration, //
				dms.dmsService(), USER_FOR_UPLOADS);
	}

}

package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.DEFAULT;

import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.logic.scheduler.DefaultSchedulerLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.scheduler.SchedulerExeptionFactory;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.quartz.QuartzSchedulerService;
import org.cmdbuild.services.scheduler.DefaultSchedulerExeptionFactory;
import org.cmdbuild.services.scheduler.reademail.AnswerToExistingMailFactory;
import org.cmdbuild.services.scheduler.reademail.AttachmentStoreFactory;
import org.cmdbuild.services.scheduler.reademail.DefaultAttachmentStoreFactory;
import org.cmdbuild.services.scheduler.reademail.DownloadAttachmentsFactory;
import org.cmdbuild.services.scheduler.reademail.StartWorkflowFactory;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class Scheduler {
	
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
	public SchedulerLogic defaultSchedulerLogic() {
		return new DefaultSchedulerLogic(defaultSchedulerService());
	}

	@Bean
	public SchedulerService defaultSchedulerService() {
		return new QuartzSchedulerService(schedulerExeptionFactory());
	}

	@Bean
	protected SchedulerExeptionFactory schedulerExeptionFactory() {
		return new DefaultSchedulerExeptionFactory();
	}
	
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

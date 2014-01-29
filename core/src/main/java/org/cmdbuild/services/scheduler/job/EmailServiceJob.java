package org.cmdbuild.services.scheduler.job;

import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.spring.SpringIntegrationUtils;
import org.slf4j.Logger;

public class EmailServiceJob extends AbstractJob {

	private static final Logger logger = Log.EMAIL;

	public EmailServiceJob(final Long id) {
		super(id);
	}

	@Override
	public void execute() {
		logger.info("starting synchronization job");
		// TODO inject is some other way
		SpringIntegrationUtils.applicationContext() //
				.getBean(EmailLogic.class) //
				.retrieveEmailsFromServer();
		logger.info("finishing synchronization job");
	}

}

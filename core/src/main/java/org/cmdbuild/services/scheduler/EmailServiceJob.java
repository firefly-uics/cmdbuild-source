package org.cmdbuild.services.scheduler;

import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.email.EmailReceivingLogic;
import org.cmdbuild.scheduler.AbstractJob;
import org.cmdbuild.spring.SpringIntegrationUtils;
import org.slf4j.Logger;

public class EmailServiceJob extends AbstractJob {

	private static final Logger logger = Log.EMAIL;

	public EmailServiceJob(final String name) {
		super(name);
	}

	@Override
	public void execute() {
		logger.info("starting synchronization job");
		// TODO inject is some other way
		SpringIntegrationUtils.applicationContext() //
				.getBean(EmailReceivingLogic.class) //
				.receive();
		logger.info("finishing synchronization job");
	}

}

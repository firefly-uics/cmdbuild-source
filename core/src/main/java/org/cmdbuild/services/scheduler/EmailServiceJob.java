package org.cmdbuild.services.scheduler;

import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.email.EmailReceivingLogic;
import org.cmdbuild.scheduler.AbstractJob;
import org.slf4j.Logger;

public class EmailServiceJob extends AbstractJob {

	private static final Logger logger = Log.EMAIL;

	private final EmailReceivingLogic emailReceivingLogic;

	public EmailServiceJob(final String name, final EmailReceivingLogic emailReceivingLogic) {
		super(name);
		this.emailReceivingLogic = emailReceivingLogic;
	}

	@Override
	public void execute() {
		logger.info("starting synchronization job");
		emailReceivingLogic.receive();
		logger.info("finishing synchronization job");
	}

}

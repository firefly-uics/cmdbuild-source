package org.cmdbuild.services.scheduler.job;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EmailServiceJob extends AbstractJob {

	public EmailServiceJob(final Long id) {
		super(id);
	}

	@Override
	public void execute() {
		final Date now = new Date();
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		System.out.println(String.format("[%s] - EMAIL SERVICE EXECUTION", formatter.format(now)));
	}

}

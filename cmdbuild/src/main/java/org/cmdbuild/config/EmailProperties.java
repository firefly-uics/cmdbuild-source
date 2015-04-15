package org.cmdbuild.config;

import java.io.IOException;

import org.cmdbuild.services.Settings;

public class EmailProperties extends DefaultProperties implements EmailConfiguration {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "email";

	private static final String QUEUE_TIME = "email.queue.time";

	public EmailProperties() {
		super();
		setProperty(QUEUE_TIME, "0");
	}

	public static EmailProperties getInstance() {
		return (EmailProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public long getQueueTime() {
		return Long.valueOf(getProperty(QUEUE_TIME));
	}

	@Override
	public void setQueueTime(long value) {
		setProperty(QUEUE_TIME, Long.toString(value));
	}

	public void save() {
		try {
			store();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	};

}

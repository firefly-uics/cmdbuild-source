package org.cmdbuild.common.mail;

import javax.mail.Message;
import javax.mail.MessagingException;

class JavaxMailUtils {

	private static final String MESSAGE_ID = "Message-ID";

	private JavaxMailUtils() {
		// prevents instantiation
	}

	public static String messageIdOf(final Message message) throws MessagingException {
		return message.getHeader(MESSAGE_ID)[0];
	}

}

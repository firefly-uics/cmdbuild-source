package org.cmdbuild.common.api.mail;

public interface NewMailQueue {

	/**
	 * Creates a new mail.
	 * 
	 * @return a new mail object.
	 */
	QueueableNewMail newMail();

	/**
	 * Sends all new mails.
	 */
	void sendAll();

}

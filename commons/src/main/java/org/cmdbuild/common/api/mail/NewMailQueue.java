package org.cmdbuild.common.api.mail;

public interface NewMailQueue {

	interface Callback {

		void added(int index);

		void sent(int index);

	}

	NewMailQueue withCallback(Callback callback);

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

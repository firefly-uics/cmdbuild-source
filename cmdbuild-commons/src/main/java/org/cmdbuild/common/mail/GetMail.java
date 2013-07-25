package org.cmdbuild.common.mail;

import java.net.URL;

/**
 * Get mail interface.
 */
public interface GetMail extends FetchedMail {

	/**
	 * Mail attachment interface.
	 */
	interface Attachment {

		/**
		 * Gets the attachment's URL.
		 * 
		 * @return the attachment's URL.
		 */
		URL getUrl();

	}

	/**
	 * Gets mail's content.
	 * 
	 * @return the mail's content.
	 */
	String getContent();

	/**
	 * Gets mail's attachments.
	 * 
	 * @return all mail's attachments.
	 */
	Iterable<Attachment> getAttachments();

}

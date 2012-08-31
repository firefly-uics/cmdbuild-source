package org.cmdbuild.common.mail;

import java.net.URL;

/**
 * New mail class.
 */
public interface NewMail {

	/**
	 * Adds a FROM recipient.
	 * 
	 * @param from
	 *            is the mail address of a FROM recipient
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withFrom(String from);

	/**
	 * Adds a TO recipient.
	 * 
	 * @param to
	 *            is the mail address of a TO recipient
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withTo(String to);

	/**
	 * Adds a CC recipient.
	 * 
	 * @param cc
	 *            is the mail address of a CC recipient
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withCc(String cc);

	/**
	 * Adds a BCC recipient.
	 * 
	 * @param to
	 *            is the mail address of a BCC recipient
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withBcc(String bcc);

	/**
	 * Sets the subject.
	 * 
	 * @param subject
	 *            is the subject of the mail
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withSubject(String subject);

	/**
	 * Sets the content.
	 * 
	 * @param content
	 *            is the content of the mail
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withContent(String content);

	/**
	 * Sets the content-type.
	 * 
	 * @param contentType
	 *            is the content-type od the body
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withContentType(String contentType);

	/**
	 * Adds an attachment
	 * 
	 * @param url
	 *            is the {@link URL} of the attachment
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withAttachment(URL url);

	/**
	 * Sends the new mail.
	 */
	void send();

}

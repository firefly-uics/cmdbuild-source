package org.cmdbuild.services.email;

import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.EmailTemplate;
import org.slf4j.Logger;

/**
 * Service for coordinate e-mail operations and persistence.
 */
public interface EmailService {

	Logger logger = Log.EMAIL;

	/**
	 * Sends the specified mail.
	 * 
	 * @param email
	 * 
	 * @throws EmailServiceException
	 *             if there is any problem.
	 */
	void send(Email email) throws EmailServiceException;

	void receive(EmailCallbackHandler callback) throws EmailServiceException;

	/**
	 * Retrieves mails from mailbox and stores them.
	 * 
	 * @throws EmailServiceException
	 *             if there is any problem.
	 */
	Iterable<Email> receive() throws EmailServiceException;

	/**
	 * Gets all email templates associated with specified email.
	 * 
	 * @param email
	 * 
	 * @return all templates.
	 */
	Iterable<EmailTemplate> getEmailTemplates(Email email);

	void save(Email email);

	void delete(Email email);

	Iterable<Email> getEmails(Long processId);

	Iterable<Email> getOutgoingEmails(Long processId);

}

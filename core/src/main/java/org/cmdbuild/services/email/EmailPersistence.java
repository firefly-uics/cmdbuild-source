package org.cmdbuild.services.email;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.EmailTemplate;

public interface EmailPersistence {

	/**
	 * Gets the process' card defined whose class/id are defined in the
	 * specified subject.
	 * 
	 * @param subject
	 * 
	 * @return the process' card.
	 * 
	 * @throws IllegalArgumentException
	 *             if there is a problem.
	 */
	CMCard getProcessCardFrom(String subject) throws IllegalArgumentException;

	/**
	 * Gets the process' card related with the specified email.
	 * 
	 * @param email
	 * 
	 * @return the process' card.
	 * 
	 * @throws IllegalArgumentException
	 *             if there is a problem.
	 */
	CMCard getProcessCardFrom(Email email) throws IllegalArgumentException;

	/**
	 * Gets all mail templates.
	 * 
	 * @return all mail templates.
	 */
	Iterable<EmailTemplate> getEmailTemplates();

	/**
	 * Gets all outgoing emails for the specified process' id.
	 * 
	 * @param processId
	 * 
	 * @return all outgoing emails.
	 */
	Iterable<Email> getOutgoingEmails(Long processId);

	/**
	 * Creates a new {@link Email} and returns the stored one (so with
	 * {@code Id} also).
	 * 
	 * @param email
	 *            is the {@link Email} that needs to be created.
	 * 
	 * @return the created {@link Email}.
	 */
	Email create(Email email);

	/**
	 * Saves (create or updates) the specified email.
	 * 
	 * @param email
	 */
	void save(Email email);

	/**
	 * Deletes the specified email.
	 * 
	 * @param email
	 */
	void delete(Email email);

	/**
	 * Gets all emails for the specified process' id.
	 * 
	 * @param processId
	 * 
	 * @return all email for the specified process' id.
	 */
	Iterable<Email> getEmails(Long processId);

}

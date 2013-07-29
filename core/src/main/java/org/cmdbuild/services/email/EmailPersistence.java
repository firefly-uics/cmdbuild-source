package org.cmdbuild.services.email;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.EmailTemplate;

public interface EmailPersistence {

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

	CMCard getActivityCardFrom(String subject);

	/**
	 * Gets all mail templates.
	 * 
	 * @return all mail templates.
	 */
	Iterable<EmailTemplate> getEmailTemplates();

	/**
	 * Gets all valued e-mails for the user with the specified name.
	 * 
	 * @param user
	 * 
	 * @return all the valued e-mails (does not returns empty or blank ones).
	 */
	Iterable<String> getEmailsForUser(String user);

	/**
	 * Gets all valued e-mails for the group with the specified name.
	 * 
	 * @param group
	 * 
	 * @return all the valued e-mails (does not returns empty or blank ones).
	 */
	Iterable<String> getEmailsForGroup(String group);

	/**
	 * Gets all valued e-mails for the users of the group with the specified
	 * name.
	 * 
	 * @param group
	 * 
	 * @return all the valued e-mails (does not returns empty or blank ones).
	 */
	Iterable<String> getEmailsForGroupUsers(String group);

}

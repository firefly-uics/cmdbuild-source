package org.cmdbuild.logic.email;

import java.util.List;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.EmailTemplate;

public interface EmailTemplateLogic extends Logic {

	/**
	 * Return all the email templates defined for the given EntryType, and the
	 * templates that have no owner EntryType
	 */
	List<EmailTemplate> readForEntryTypeName(final String entryTypeName);

	/**
	 * Store a new email template
	 */
	void create(final EmailTemplate emailTemplate);

	/**
	 * Update the given email template
	 */
	void update(final EmailTemplate emailTemplate);

	/**
	 * Remove the email template with the given name
	 */
	void delete(final String emailTemplateName);

}

package org.cmdbuild.logic.email;

import java.util.List;

import org.cmdbuild.data.store.email.EmailTemplateStore;
import org.cmdbuild.model.email.EmailTemplate;

public class DefaultEmailTemplateLogic implements EmailTemplateLogic {

	private final EmailTemplateStore store;

	public DefaultEmailTemplateLogic(final EmailTemplateStore store) {
		this.store = store;
	}

	@Override
	public List<EmailTemplate> readForEntryTypeName(final String entryTypeName) {
		return store.readForEntryType(entryTypeName);
	}

	@Override
	public void create(final EmailTemplate emailTemplate) {
		store.create(emailTemplate);
	}

	@Override
	public void update(final EmailTemplate emailTemplate) {
		store.update(emailTemplate);
	}

	@Override
	public void delete(final String emailTemplateName) {
		final EmailTemplate emailTemplate = new EmailTemplate();
		emailTemplate.setName(emailTemplateName);

		store.delete(emailTemplate);
	}

}

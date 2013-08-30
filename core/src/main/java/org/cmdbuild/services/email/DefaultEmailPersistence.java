package org.cmdbuild.services.email;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.EmailConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.data.store.email.EmailTemplateStorableConverter;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.model.email.EmailTemplate;
import org.slf4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class DefaultEmailPersistence implements EmailPersistence {

	private static final Logger logger = Log.PERSISTENCE;

	private final CMDataView dataView;
	private final LookupStore lookupStore;

	public DefaultEmailPersistence(final CMDataView dataView, final LookupStore lookupStore) {
		this.dataView = dataView;
		this.lookupStore = lookupStore;
	}

	@Override
	public Iterable<EmailTemplate> getEmailTemplates() {
		logger.info("getting all email templates");
		final EmailTemplateStorableConverter converter = new EmailTemplateStorableConverter();
		final Store<EmailTemplate> store = new DataViewStore<EmailTemplate>(dataView, converter);
		return store.list();
	}

	@Override
	public Iterable<Email> getOutgoingEmails(final Long processId) {
		logger.info("getting all outgoing emails for process with id '{}'", processId);
		return FluentIterable.from(emailStore(processId).list()).filter(new Predicate<Email>() {
			@Override
			public boolean apply(final Email input) {
				return (EmailStatus.DRAFT.equals(input.getStatus()) || EmailStatus.OUTGOING.equals(input.getStatus()));
			};
		});
	}

	@Override
	public Email save(final Email email) {
		logger.info("saving email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		final Store<Email> emailStore = emailStore(email.getActivityId());
		final Email storedEmail;
		if (email.getId() == null) {
			logger.debug("creating new email");
			final Storable storable = emailStore.create(email);
			storedEmail = emailStore.read(storable);
			storedEmail.setAttachments(email.getAttachments());
		} else {
			logger.debug("updating existing email");
			emailStore.update(email);
			storedEmail = email;
		}
		return storedEmail;
	}

	@Override
	public void delete(final Email email) {
		logger.info("deleting email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		emailStore(email.getActivityId()).delete(email);
	}

	@Override
	public Email getEmail(final Long emailId) {
		logger.info("getting email with id '{}'", emailId);
		final Email email = emailStore().read(new Storable() {

			@Override
			public String getIdentifier() {
				return emailId.toString();
			}

		});
		return email;
	}

	@Override
	public Iterable<Email> getEmails(final Long processId) {
		logger.info("getting all emails for process' id '{}'", processId);
		return emailStore(processId).list();
	}

	/*
	 * utilities
	 */

	private Store<Email> emailStore() {
		logger.trace("getting email store for all emails");
		return new DataViewStore<Email>(dataView, emailConverter());
	}

	private Store<Email> emailStore(final Long processId) {
		logger.trace("getting email store for process' id '{}'", processId);
		return new DataViewStore<Email>(dataView, emailConverter(processId));
	}

	private EmailConverter emailConverter() {
		logger.trace("getting email converter for all emails");
		return new EmailConverter(lookupStore);
	}

	private EmailConverter emailConverter(final Long processId) {
		logger.trace("getting email converter for process' id '{}'", processId);
		return new EmailConverter(lookupStore, processId);
	}

}

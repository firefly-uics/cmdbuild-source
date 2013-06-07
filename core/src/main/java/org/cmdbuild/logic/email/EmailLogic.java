package org.cmdbuild.logic.email;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.data.converter.EmailConverter.EMAIL_CLASS_NAME;
import static org.cmdbuild.data.converter.EmailConverter.EMAIL_STATUS_ATTRIBUTE;
import static org.cmdbuild.data.converter.EmailConverter.PROCESS_ID_ATTRIBUTE;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.EmailConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.Email;
import org.cmdbuild.model.Email.EmailStatus;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.EmailService;

import com.google.common.collect.Lists;

public class EmailLogic implements Logic {

	private final CMDataView view;
	private final EmailConfiguration configuration;
	private final EmailService service;
	private final Notifier notifier;

	public EmailLogic(final CMDataView view, final EmailConfiguration configuration, final EmailService service,
			final Notifier notifier) {
		this.view = view;
		this.configuration = configuration;
		this.service = service;
		this.notifier = notifier;
	}

	public Iterable<Email> getEmails(final Long processCardId) {
		retrieveEmailsFromServer();
		final List<Email> emails = Lists.newArrayList();
		for (final Email email : emailStore(processCardId).list()) {
			emails.add(email);
		}
		return emails;
	}

	/**
	 * It fetches mails from mailserver and store them into the cmdbuild
	 * database
	 */
	private void retrieveEmailsFromServer() {
		try {
			service.syncEmail();
		} catch (final CMDBException e) {
			notifier.warn(e);
		} catch (final IOException e) {
			throw new RuntimeException("Error rietrieving emails", e);
		}
	}

	public void sendOutgoingAndDraftEmails(final Long processCardId) {
		final CMClass emailClass = view.findClass(EMAIL_CLASS_NAME);
		final CMQueryResult result = view.select(anyAttribute(emailClass)) //
				.from(emailClass) //
				.where(and( //
						condition(attribute(emailClass, PROCESS_ID_ATTRIBUTE), //
								eq(processCardId)), //
						condition(attribute(emailClass, EMAIL_STATUS_ATTRIBUTE), //
								in(lookupId(EmailStatus.DRAFT), lookupId(EmailStatus.OUTGOING)))) //
				) //
				.run();
		final List<CMCard> emailCardsToBeSent = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard cardToSend = row.getCard(emailClass);
			emailCardsToBeSent.add(cardToSend);
		}

		final Store<Email> emailStore = emailStore(processCardId);
		final LookupStore lookupStore = applicationContext().getBean(LookupStore.class);
		final StorableConverter<Email> converter = new EmailConverter(lookupStore, processCardId.intValue());
		for (final CMCard emailCard : emailCardsToBeSent) {
			final Email emailToSend = converter.convert(emailCard);
			emailToSend.setFromAddress(configuration.getEmailAddress());
			try {
				service.sendEmail(emailToSend);
				emailToSend.setStatus(EmailStatus.SENT);
			} catch (final CMDBException ex) {
				notifier.warn(ex);
				emailToSend.setStatus(EmailStatus.OUTGOING);
			}
			emailStore.update(emailToSend);
		}
	}

	private Integer lookupId(final EmailStatus emailStatus) {
		final LookupStore lookupStore = applicationContext().getBean(LookupStore.class);
		final Iterable<Lookup> elements = lookupStore.listForType(LookupType.newInstance() //
				.withName(EmailStatus.LOOKUP_TYPE) //
				.build());
		for (final Lookup lookup : elements) {
			if (emailStatus.getLookupName().equals(lookup.description)) {
				return lookup.getId().intValue();
			}
		}
		logger.error("lookup not found for type '{}' and description '{}'", EmailStatus.LOOKUP_TYPE, emailStatus);
		throw new NoSuchElementException();
	}

	public void deleteEmail(final Long processCardId, final Long emailId) {
		emailStore(processCardId).delete(new Storable() {
			@Override
			public String getIdentifier() {
				return emailId.toString();
			}
		});
	}

	public void saveEmail(final Long processCardId, final Email email) {
		final Store<Email> emailStore = emailStore(processCardId);
		email.setActivityId(processCardId.intValue());
		if (email.getId() == null) {
			email.setStatus(EmailStatus.DRAFT);
			emailStore.create(email);
		} else {
			emailStore.update(email);
		}
	}

	private Store<Email> emailStore(final Long processId) {
		final LookupStore lookupStore = applicationContext().getBean(LookupStore.class);
		final StorableConverter<Email> converter = new EmailConverter(lookupStore, processId.intValue());
		return new DataViewStore<Email>(view, converter);
	}

}

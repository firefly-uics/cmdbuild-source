package org.cmdbuild.services.email;

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

import java.util.List;
import java.util.NoSuchElementException;

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
import org.cmdbuild.data.store.email.EmailTemplateStorableConverter;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.model.email.EmailTemplate;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

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
		final List<Email> emails = Lists.newArrayList();
		final CMClass emailClass = dataView.findClass(EMAIL_CLASS_NAME);
		final CMQueryResult result = dataView.select(anyAttribute(emailClass)) //
				.from(emailClass) //
				.where(and( //
						condition(attribute(emailClass, PROCESS_ID_ATTRIBUTE), eq(processId)), //
						condition(attribute(emailClass, EMAIL_STATUS_ATTRIBUTE), //
								in(lookupId(EmailStatus.DRAFT), lookupId(EmailStatus.OUTGOING)))) //
				) //
				.run();
		final StorableConverter<Email> converter = emailConverter(processId);
		for (final CMQueryRow row : result) {
			final CMCard card = row.getCard(emailClass);
			final Email email = converter.convert(card);
			emails.add(email);
		}
		return emails;
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

	@Override
	public Email create(final Email email) {
		logger.info("saving email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		final Store<Email> emailStore = emailStore(email.getActivityId());
		final Storable storable = emailStore.create(email);
		final Email storedEmail = emailStore.read(storable);
		return storedEmail;
	}

	@Override
	public void save(final Email email) {
		logger.info("saving email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		final Integer processCardId = email.getActivityId();
		final Store<Email> emailStore = emailStore(processCardId);
		email.setActivityId(processCardId.intValue());
		if (email.getId() == null) {
			logger.debug("creating new email");
			email.setStatus(EmailStatus.DRAFT);
			emailStore.create(email);
		} else {
			logger.debug("updating existing email");
			emailStore.update(email);
		}
	}

	@Override
	public void delete(final Email email) {
		logger.info("deleting email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		emailStore(email.getActivityId()).delete(email);
	}

	@Override
	public Email getEmail(final Long emailId) {
		return emailStore().read(new Storable() {

			@Override
			public String getIdentifier() {
				// TODO Auto-generated method stub
				return emailId.toString();
			}

		});
	}

	@Override
	public Iterable<Email> getEmails(final Long processId) {
		logger.info("getting all emails for process' id '{}'", processId);
		return emailStore(processId).list();
	}

	private Store<Email> emailStore(final Integer processId) {
		logger.trace("getting email store for process' id '{}'", processId);
		return emailStore(processId.longValue());
	}

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

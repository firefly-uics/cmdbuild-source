package org.cmdbuild.logic.email;

import static java.lang.String.format;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
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
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreator;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.Attachment;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.EmailService;

import com.google.common.collect.Lists;

public class EmailLogic implements Logic {

	private static final String USER_FOR_ATTACHMENTS_UPLOAD = "system";

	private final CMDataView view;
	private final EmailConfiguration configuration;
	private final EmailService service;
	private final DmsConfiguration dmsConfiguration;
	private final DmsService dmsService;
	private final DocumentCreatorFactory documentCreatorFactory;
	private final Notifier notifier;

	public EmailLogic( //
			final CMDataView view, //
			final EmailConfiguration configuration, //
			final EmailService service,//
			final DmsConfiguration dmsConfiguration, //
			final DmsService dmsService, //
			final DocumentCreatorFactory documentCreatorFactory, //
			final Notifier notifier //
	) {
		this.view = view;
		this.configuration = configuration;
		this.service = service;
		this.dmsConfiguration = dmsConfiguration;
		this.dmsService = dmsService;
		this.documentCreatorFactory = documentCreatorFactory;
		this.notifier = notifier;
	}

	public Iterable<Email> getEmails(final Long processCardId) {
		final List<Email> emails = Lists.newArrayList();
		for (final Email email : emailStore(processCardId).list()) {
			emails.add(email);
		}
		return emails;
	}

	// TODO move in another component
	public void retrieveEmailsFromServer() {
		try {
			final Iterable<Email> emails = service.receive();
			storeAttachmentsOf(emails);
		} catch (final CMDBException e) {
			notifier.warn(e);
		}
	}

	private void storeAttachmentsOf(final Iterable<Email> emails) {
		if (dmsConfiguration.isEnabled()) {
			for (final Email email : emails) {
				try {
					storeAttachmentsOf(email);
				} catch (final Exception e) {
					logger.warn(format("error storing attachments of email with id '{}'", email.getId()), e);
				}
			}
		} else {
			logger.warn("dms service not enabled");
		}
	}

	private void storeAttachmentsOf(final Email email) {
		final DocumentCreator documentFactory = createDocumentFactory(EMAIL_CLASS_NAME);
		for (final Attachment attachment : email.getAttachments()) {
			InputStream inputStream = null;
			try {
				logger.debug("uploading attachment '{}'", attachment.getName());
				inputStream = new FileInputStream(new File(attachment.getUrl().toURI()));
				final StorableDocument document = documentFactory.createStorableDocument( //
						USER_FOR_ATTACHMENTS_UPLOAD, //
						EMAIL_CLASS_NAME, //
						email.getId().intValue(), //
						inputStream, //
						attachment.getName(), //
						dmsConfiguration.getLookupNameForAttachments(), //
						attachment.getName());
				dmsService.upload(document);
			} catch (final Exception e) {
				logger.warn("error uploading attachment to dms", e);
			} finally {
				if (inputStream != null) {
					IOUtils.closeQuietly(inputStream);
				}
			}
		}
	}

	private DocumentCreator createDocumentFactory(final String className) {
		final CMClass fetchedClass = view.findClass(className);
		documentCreatorFactory.setClass(fetchedClass);
		return documentCreatorFactory.create();
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
				service.send(emailToSend);
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

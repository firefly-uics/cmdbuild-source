package org.cmdbuild.services.email;

import static org.cmdbuild.data.converter.EmailConverter.EMAIL_CLASS_NAME;

import java.io.InputStream;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.commons.io.IOUtils;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.EmailConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.data.store.email.EmailTemplateStorableConverter;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreator;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Attachment;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.model.email.EmailTemplate;
import org.cmdbuild.spring.annotations.RepositoryComponent;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

@RepositoryComponent(value = "emailPersistence")
public class DefaultEmailPersistence implements EmailPersistence {

	private static final Logger logger = Log.PERSISTENCE;

	private static final String USER_FOR_ATTACHMENTS_UPLOAD = "system";

	private final CMDataView dataView;
	private final LookupStore lookupStore;
	private final DmsService dmsService;
	private final DmsConfiguration dmsConfiguration;
	private final DocumentCreatorFactory documentCreatorFactory;

	@Autowired
	public DefaultEmailPersistence( //
			@Qualifier("system") final CMDataView dataView, //
			final LookupStore lookupStore, //
			final DmsService dmsService, //
			final DmsConfiguration dmsConfiguration, //
			final DocumentCreatorFactory documentCreatorFactory //
	) {
		this.dataView = dataView;
		this.lookupStore = lookupStore;
		this.dmsService = dmsService;
		this.dmsConfiguration = dmsConfiguration;
		this.documentCreatorFactory = documentCreatorFactory;
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
		storeAttachmentsOf(storedEmail);
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
		return mergeWithAttachments(email);
	}

	@Override
	public Iterable<Email> getEmails(final Long processId) {
		logger.info("getting all emails for process' id '{}'", processId);
		return FluentIterable.from(emailStore(processId).list()) //
				.transform(new Function<Email, Email>() {
					@Override
					public Email apply(final Email input) {
						return mergeWithAttachments(input);
					}

				});
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

	private void storeAttachmentsOf(final Email email) {
		logger.info("storing attachments for email with id '{}'", email.getId());

		if (!dmsConfiguration.isEnabled()) {
			logger.warn("dms service not enabled");
			return;
		}

		final CMClass fetchedClass = dataView.findClass(EMAIL_CLASS_NAME);
		documentCreatorFactory.setClass(fetchedClass);
		final DocumentCreator documentFactory = documentCreatorFactory.create();

		for (final Attachment attachment : email.getAttachments()) {
			InputStream inputStream = null;
			try {
				logger.debug("uploading attachment '{}'", attachment.getName());
				inputStream = attachment.getDataHandler().getInputStream();
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
				logger.warn("error storing attachment into dms", e);
			} finally {
				if (inputStream != null) {
					IOUtils.closeQuietly(inputStream);
				}
			}
		}
	}

	private Email mergeWithAttachments(final Email email) {
		logger.info("getting attachments for email with id '{}'", email.getId());

		if (!dmsConfiguration.isEnabled()) {
			logger.warn("dms service not enabled");
			return email;
		}

		final CMClass fetchedClass = dataView.findClass(EMAIL_CLASS_NAME);
		documentCreatorFactory.setClass(fetchedClass);
		final DocumentCreator documentFactory = documentCreatorFactory.create();

		try {
			final DocumentSearch documentSearch = documentFactory.createDocumentSearch( //
					EMAIL_CLASS_NAME, //
					email.getId().intValue());
			final Iterable<StoredDocument> storedDocuments = dmsService.search(documentSearch);

			final List<Attachment> attachments = Lists.newArrayList();

			for (final StoredDocument storedDocument : storedDocuments) {
				final DocumentDownload documentDownload = documentFactory.createDocumentDownload( //
						EMAIL_CLASS_NAME, //
						email.getId().intValue(), //
						storedDocument.getName());
				final DataHandler dataHandler = dmsService.download(documentDownload);

				attachments.add(Attachment.newInstance() //
						.withName(storedDocument.getName()) //
						.withDataHandler(dataHandler) //
						.build());
			}

			email.setAttachments(attachments);
		} catch (final Exception e) {
			logger.warn("error getting attachments from dms", e);
		}

		return email;
	}
}

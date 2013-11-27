package org.cmdbuild.logic.email;

import static com.google.common.collect.FluentIterable.*;
import static com.google.common.collect.Iterables.isEmpty;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.cmdbuild.data.converter.EmailConverter.EMAIL_CLASS_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreator;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.exception.DmsError;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.DmsException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.Attachment;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.model.email.EmailConstants;
import org.cmdbuild.model.email.EmailTemplate;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.EmailService;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EmailLogic implements Logic {

	public static class UploadableAttachment {

		public static class Builder implements org.cmdbuild.common.Builder<UploadableAttachment> {

			private String identifier;
			private DataHandler dataHandler;
			private boolean temporary;

			private Builder() {
				// prevents direct instantiation
			}

			@Override
			public UploadableAttachment build() {
				validate();
				return new UploadableAttachment(this);
			}

			private void validate() {
				Validate.notNull(dataHandler, "invalid data handler");
			}

			public Builder withIdentifier(final String identifier) {
				this.identifier = identifier;
				return this;
			}

			public Builder withDataHandler(final DataHandler dataHandler) {
				this.dataHandler = dataHandler;
				return this;
			}

			public Builder withTemporaryStatus(final boolean temporary) {
				this.temporary = temporary;
				return this;
			}

		}

		public static Builder uploadableAttachment() {
			return new Builder();
		}

		public final String identifier;
		public final DataHandler dataHandler;
		public final boolean temporary;

		private UploadableAttachment(final Builder builder) {
			this.identifier = builder.identifier;
			this.dataHandler = builder.dataHandler;
			this.temporary = builder.temporary;
		}

	}

	public static class DeleteableAttachment {

		public static class Builder implements org.cmdbuild.common.Builder<DeleteableAttachment> {

			private String identifier;
			private String fileName;
			private boolean temporary;

			private Builder() {
				// prevents direct instantiation
			}

			@Override
			public DeleteableAttachment build() {
				validate();
				return new DeleteableAttachment(this);
			}

			private void validate() {
				Validate.notNull(fileName, "invalid file name");
				Validate.notEmpty(fileName, "invalid file name");
			}

			public Builder withIdentifier(final String identifier) {
				this.identifier = identifier;
				return this;
			}

			public Builder withFileName(final String fileName) {
				this.fileName = fileName;
				return this;
			}

			public Builder withTemporaryStatus(final boolean temporary) {
				this.temporary = temporary;
				return this;
			}

		}

		public static Builder deleteableAttachment() {
			return new Builder();
		}

		public final String identifier;
		public final String fileName;
		public final boolean temporary;

		private DeleteableAttachment(final Builder builder) {
			this.identifier = builder.identifier;
			this.fileName = builder.fileName;
			this.temporary = builder.temporary;
		}

	}

	public static class EmailWithAttachmentNames {

		private final Email email;
		private final Iterable<String> attachmentNames;

		EmailWithAttachmentNames(final Email email, final Iterable<String> attachmentNames) {
			this.email = email;
			this.attachmentNames = attachmentNames;
		}

		public Email getEmail() {
			return email;
		}

		public Iterable<String> getAttachmentNames() {
			return attachmentNames;
		}

	}

	public static class EmailSubmission extends Email {

		private String temporaryId;

		public EmailSubmission() {
			super();
		}

		public EmailSubmission(final long id) {
			super(id);
		}

		public String getTemporaryId() {
			return temporaryId;
		}

		public void setTemporaryId(final String temporaryId) {
			this.temporaryId = temporaryId;
		}

	}

	private static final Function<Email, Long> EMAIL_ID_FUNCTION = new Function<Email, Long>() {

		@Override
		public Long apply(final Email input) {
			return input.getId();
		}

	};

	private static final String DUMMY_CLASSNAME_FOR_TEMPORARY = "tmp";

	// TODO do in a better way
	private static final boolean TEMPORARY = true;
	private static final boolean FINAL = false;

	private static final EmailStatus MISSING_STATUS = null;

	private static final Collection<EmailStatus> SAVEABLE_STATUSES = Arrays.asList(EmailStatus.DRAFT, MISSING_STATUS);

	private static final String USER_FOR_ATTACHMENTS_UPLOAD = "system";

	private final CMDataView view;
	private final EmailConfiguration configuration;
	private final EmailService service;
	private final DmsConfiguration dmsConfiguration;
	private final DmsService dmsService;
	private final DocumentCreatorFactory documentCreatorFactory;
	private final Notifier notifier;
	private final OperationUser operationUser;

	public EmailLogic( //
			final CMDataView view, //
			final EmailConfiguration configuration, //
			final EmailService service,//
			final DmsConfiguration dmsConfiguration, //
			final DmsService dmsService, //
			final DocumentCreatorFactory documentCreatorFactory, //
			final Notifier notifier, //
			final OperationUser operationUser //
	) {
		this.view = view;
		this.configuration = configuration;
		this.service = service;
		this.dmsConfiguration = dmsConfiguration;
		this.dmsService = dmsService;
		this.documentCreatorFactory = documentCreatorFactory;
		this.notifier = notifier;
		this.operationUser = operationUser;
	}

	public Iterable<EmailWithAttachmentNames> getEmails(final Long processCardId) {
		final Function<Email, EmailWithAttachmentNames> TO_EMAIL_WITH_ATTACHMENT_NAMES = new Function<Email, EmailWithAttachmentNames>() {

			@Override
			public EmailWithAttachmentNames apply(final Email input) {
				final List<String> attachmentNames = Lists.newArrayList();
				try {
					final Entry<String, DocumentCreator> entry = classNameAndDocumentCreator(FINAL);
					final DocumentSearch allDocuments = entry.getValue() //
							.createDocumentSearch(entry.getKey(), input.getIdentifier());
					for (final StoredDocument document : dmsService.search(allDocuments)) {
						attachmentNames.add(document.getName());
					}
				} catch (DmsError e) {
					logger.warn("error getting attachments for email '{}', ignoring it", input.getId());
					logger.warn("... cause was", e);
				}
				return new EmailWithAttachmentNames(input, attachmentNames);
			}

		};
		return from(service.getEmails(processCardId)) //
				.transform(TO_EMAIL_WITH_ATTACHMENT_NAMES);
	}

	// TODO move in another component
	public void retrieveEmailsFromServer() {
		try {
			final Iterable<Email> emails = service.receive();
			storeAttachmentsOf(emails);
			sendNotifications(emails);
		} catch (final CMDBException e) {
			notifier.warn(e);
		}
	}

	private void storeAttachmentsOf(final Iterable<Email> emails) {
		logger.info("storing attachments for emails");
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
						email.getId().toString(), //
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
		return documentCreatorFactory.create(fetchedClass);
	}

	private void sendNotifications(final Iterable<Email> emails) {
		logger.info("sending notifications for emails");
		for (final Email email : emails) {
			try {
				sendNotificationFor(email);
			} catch (final Exception e) {
				logger.warn(format("error storing attachments of email with id '{}'", email.getId()), e);
			}
		}
	}

	private void sendNotificationFor(final Email email) {
		logger.debug("sending notification for email with id '{}'", email.getId());
		try {
			for (final EmailTemplate emailTemplate : service.getEmailTemplates(email)) {
				final Email notification = resolve(emailTemplate);
				service.send(notification);
			}
		} catch (final Exception e) {
			logger.warn("error sending notification", e);
		}
	}

	private Email resolve(final EmailTemplate emailTemplate) {
		final Email email = new Email();
		email.setToAddresses(resolveRecipients(emailTemplate.getToAddresses()));
		email.setCcAddresses(resolveRecipients(emailTemplate.getCCAddresses()));
		email.setBccAddresses(resolveRecipients(emailTemplate.getBCCAddresses()));
		email.setSubject(emailTemplate.getSubject());
		email.setContent(emailTemplate.getBody());
		return email;
	}

	private String resolveRecipients(final Iterable<String> recipients) {
		return StringUtils.join(service.resolveRecipients(recipients).iterator(), EmailConstants.ADDRESSES_SEPARATOR);
	}

	public void sendOutgoingAndDraftEmails(final Long processCardId) {
		for (final Email email : service.getOutgoingEmails(processCardId)) {
			email.setFromAddress(configuration.getEmailAddress());
			try {
				service.send(email, attachmentsOf(email));
				email.setStatus(EmailStatus.SENT);
			} catch (final CMDBException ex) {
				notifier.warn(ex);
				email.setStatus(EmailStatus.OUTGOING);
			}
			service.save(email);
		}
	}

	private Map<URL, String> attachmentsOf(final Email email) {
		logger.debug("getting attachments of email {}", email.getId());
		final Map<URL, String> attachments = Maps.newHashMap();
		try {
			final Entry<String, DocumentCreator> target = classNameAndDocumentCreator(FINAL);
			final String className = target.getKey();
			final DocumentCreator documentCreator = target.getValue();
			final String emailId = email.getId().toString();
			final DocumentSearch allDocuments = documentCreator //
					.createDocumentSearch( //
							className, //
							emailId);
			for (final StoredDocument storedDocument : dmsService.search(allDocuments)) {
				logger.debug("downloading attachment with name '{}'", storedDocument.getName());
				final DocumentDownload document = documentCreator //
						.createDocumentDownload( //
								className, //
								emailId, //
								storedDocument.getName());
				final DataHandler dataHandler = dmsService.download(document);
				final TempDataSource tempDataSource = TempDataSource.create(storedDocument.getName());
				copy(dataHandler, tempDataSource);
				final URL url = tempDataSource.getFile().toURI().toURL();
				attachments.put(url, storedDocument.getName());
			}
		} catch (final DmsError e) {
			logger.error("error getting attachment from DMS", e);
		} catch (final IOException e) {
			logger.error("i/o error", e);
		}
		return attachments;
	}

	private void copy(final DataHandler from, final DataSource to) throws IOException {
		final InputStream inputStream = from.getInputStream();
		final OutputStream outputStream = to.getOutputStream();
		IOUtils.copy(inputStream, outputStream);
		IOUtils.closeQuietly(inputStream);
		IOUtils.closeQuietly(outputStream);
	}

	/**
	 * Deletes all {@link Email}s with the specified id and for the specified
	 * process' id. Only draft mails can be deleted.
	 */
	public void deleteEmails(final Long processCardId, final Iterable<Long> emailIds) {
		if (isEmpty(emailIds)) {
			return;
		}
		final Map<Long, Email> storedEmails = storedEmailsById(processCardId);
		for (final Long emailId : emailIds) {
			final Email found = storedEmails.get(emailId);
			Validate.notNull(found, "email not found");
			Validate.isTrue(SAVEABLE_STATUSES.contains(found.getStatus()), "specified email have no compatible status");
			service.delete(found);
		}
	}

	/**
	 * Saves all specified {@link Email}s for the specified process' id. Only
	 * draft mails can be saved, others are skipped.
	 */
	public void saveEmails(final Long processCardId, final Iterable<EmailSubmission> emails) {
		if (isEmpty(emails)) {
			return;
		}
		final Map<Long, Email> storedEmails = storedEmailsById(processCardId);
		for (final EmailSubmission emailSubmission : emails) {
			final Email alreadyStoredEmailSubmission = storedEmails.get(emailSubmission.getId());
			final boolean alreadyStored = (alreadyStoredEmailSubmission != null);
			final Email maybeUpdateable = alreadyStored ? alreadyStoredEmailSubmission : emailSubmission;
			if (SAVEABLE_STATUSES.contains(maybeUpdateable.getStatus())) {
				maybeUpdateable.setActivityId(processCardId.intValue());
				final Long savedId = service.save(maybeUpdateable);
				if (!alreadyStored) {
					moveAttachmentsFromTemporaryToFinalPosition(emailSubmission.getTemporaryId(), savedId.toString());
				}
			}

		}
	}

	private void moveAttachmentsFromTemporaryToFinalPosition(final String sourceIdentifier,
			final String destinationIdentifier) {
		logger.debug("moving attachments from temporary '{}' to final '{}' position");
		try {
			final String temporaryId = sourceIdentifier;
			final Entry<String, DocumentCreator> source = classNameAndDocumentCreator(TEMPORARY);
			final Entry<String, DocumentCreator> target = classNameAndDocumentCreator(FINAL);
			final DocumentSearch from = source.getValue() //
					.createDocumentSearch(source.getKey(), temporaryId);
			final DocumentSearch to = target.getValue() //
					.createDocumentSearch(target.getKey(), destinationIdentifier);
			dmsService.create(to);
			for (final StoredDocument storedDocument : dmsService.search(from)) {
				dmsService.move(storedDocument, from, to);
			}
		} catch (final DmsError e) {
			logger.error("error moving attachments");
		}
	}

	private Map<Long, Email> storedEmailsById(final Long processCardId) {
		return Maps.uniqueIndex(service.getEmails(processCardId), EMAIL_ID_FUNCTION);
	}

	public String upload( //
			final UploadableAttachment.Builder builder//
	) throws IOException, CMDBException {
		return upload(builder.build());
	}

	public String upload( //
			final UploadableAttachment uploadAttachment //
	) throws IOException, CMDBException {
		InputStream inputStream = null;
		try {
			inputStream = uploadAttachment.dataHandler.getInputStream();
			final String usableIdentifier = (uploadAttachment.identifier == null) ? generateIdentifier()
					: uploadAttachment.identifier;
			final Entry<String, DocumentCreator> classNameAndDocumentCreator = classNameAndDocumentCreator(uploadAttachment.temporary);
			final StorableDocument document = classNameAndDocumentCreator.getValue().createStorableDocument( //
					operationUser.getAuthenticatedUser().getUsername(), //
					classNameAndDocumentCreator.getKey(), //
					usableIdentifier, //
					inputStream, //
					uploadAttachment.dataHandler.getName(), //
					dmsConfiguration.getLookupNameForAttachments(), //
					EMPTY);
			dmsService.upload(document);
			return usableIdentifier;
		} catch (final Exception e) {
			logger.error("error uploading document");
			throw DmsException.Type.DMS_ATTACHMENT_UPLOAD_ERROR.createException();
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	public void delete( //
			final DeleteableAttachment.Builder builder //
	) throws CMDBException {
		deleteAttachment(builder.build());
	}

	public void deleteAttachment( //
			final DeleteableAttachment deleteAttachment //
	) throws CMDBException {
		try {
			final String usableIdentifier = (deleteAttachment.identifier == null) ? generateIdentifier()
					: deleteAttachment.identifier;
			final Entry<String, DocumentCreator> classNameAndDocumentCreator = classNameAndDocumentCreator(deleteAttachment.temporary);
			final DocumentDelete document = classNameAndDocumentCreator.getValue().createDocumentDelete( //
					classNameAndDocumentCreator.getKey(), //
					usableIdentifier, //
					deleteAttachment.fileName);
			dmsService.delete(document);
		} catch (final Exception e) {
			logger.error("error deleting document");
			throw DmsException.Type.DMS_ATTACHMENT_DELETE_ERROR.createException();
		}
	}

	private String generateIdentifier() {
		return UUID.randomUUID().toString();
	}

	private Entry<String, DocumentCreator> classNameAndDocumentCreator(final boolean temporary) {
		final String className = temporary ? DUMMY_CLASSNAME_FOR_TEMPORARY : EMAIL_CLASS_NAME;
		final DocumentCreator documentCreator;
		if (temporary) {
			documentCreator = documentCreatorFactory.create(className);
		} else {
			final CMClass emailClass = view.findClass(className);
			documentCreator = documentCreatorFactory.create(emailClass);
		}
		return new SimpleEntry<String, DocumentCreator>(className, documentCreator);
	}

}

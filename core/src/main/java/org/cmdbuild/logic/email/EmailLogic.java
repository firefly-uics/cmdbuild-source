package org.cmdbuild.logic.email;

import static java.lang.String.format;
import static org.cmdbuild.data.converter.EmailConverter.EMAIL_CLASS_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
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
import org.cmdbuild.model.email.EmailConstants;
import org.cmdbuild.model.email.EmailTemplate;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.EmailRecipientTemplateResolver;
import org.cmdbuild.services.email.EmailService;

public class EmailLogic implements Logic {

	private static final String USER_FOR_ATTACHMENTS_UPLOAD = "system";

	private final CMDataView view;
	private final EmailConfiguration configuration;
	private final EmailService service;
	private final DmsConfiguration dmsConfiguration;
	private final DmsService dmsService;
	private final DocumentCreatorFactory documentCreatorFactory;
	private final Notifier notifier;
	private final EmailRecipientTemplateResolver templateResolver;

	public EmailLogic( //
			final CMDataView view, //
			final EmailConfiguration configuration, //
			final EmailService service,//
			final DmsConfiguration dmsConfiguration, //
			final DmsService dmsService, //
			final DocumentCreatorFactory documentCreatorFactory, //
			final Notifier notifier, //
			final EmailRecipientTemplateResolver templateResolver //
	) {
		this.view = view;
		this.configuration = configuration;
		this.service = service;
		this.dmsConfiguration = dmsConfiguration;
		this.dmsService = dmsService;
		this.documentCreatorFactory = documentCreatorFactory;
		this.notifier = notifier;
		this.templateResolver = templateResolver;
	}

	public Iterable<Email> getEmails(final Long processCardId) {
		return service.getEmails(processCardId);
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
		return StringUtils.join(templateResolver.resolve(recipients).iterator(), EmailConstants.ADDRESSES_SEPARATOR);
	}

	public void sendOutgoingAndDraftEmails(final Long processCardId) {
		for (final Email email : service.getOutgoingEmails(processCardId)) {
			email.setFromAddress(configuration.getEmailAddress());
			try {
				service.send(email);
				email.setStatus(EmailStatus.SENT);
			} catch (final CMDBException ex) {
				notifier.warn(ex);
				email.setStatus(EmailStatus.OUTGOING);
			}
			service.save(email);
		}
	}

	public void deleteEmail(final Long processCardId, final Long emailId) {
		final Email email = new Email(emailId);
		email.setActivityId(processCardId.intValue());
		service.delete(email);
	}

	public void saveEmail(final Long processCardId, final Email email) {
		email.setActivityId(processCardId.intValue());
		service.save(email);
	}

}

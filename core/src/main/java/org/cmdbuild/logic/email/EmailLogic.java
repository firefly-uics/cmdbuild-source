package org.cmdbuild.logic.email;

import static java.lang.String.format;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.model.email.EmailConstants;
import org.cmdbuild.model.email.EmailTemplate;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.EmailRecipientTemplateResolver;
import org.cmdbuild.services.email.EmailService;

public class EmailLogic implements Logic {

	private final EmailConfiguration configuration;
	private final EmailService service;
	private final Notifier notifier;
	private final EmailRecipientTemplateResolver templateResolver;

	public EmailLogic( //
			final EmailConfiguration configuration, //
			final EmailService service,//
			final Notifier notifier, //
			final EmailRecipientTemplateResolver templateResolver //
	) {
		this.configuration = configuration;
		this.service = service;
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
			sendNotifications(emails);
		} catch (final CMDBException e) {
			notifier.warn(e);
		}
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
		email.setActivityId(processCardId);
		service.delete(email);
	}

	public void saveEmail(final Long processCardId, final Email email) {
		email.setActivityId(processCardId);
		service.save(email);
	}

}

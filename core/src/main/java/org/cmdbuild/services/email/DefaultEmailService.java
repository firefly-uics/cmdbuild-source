package org.cmdbuild.services.email;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.collect.Iterables.unmodifiableIterable;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.common.api.mail.Configuration;
import org.cmdbuild.common.api.mail.FetchedMail;
import org.cmdbuild.common.api.mail.GetMail;
import org.cmdbuild.common.api.mail.MailApi;
import org.cmdbuild.common.api.mail.MailApiFactory;
import org.cmdbuild.common.api.mail.MailException;
import org.cmdbuild.common.api.mail.NewMail;
import org.cmdbuild.common.api.mail.SelectMail;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.data.store.email.EmailTemplate;
import org.cmdbuild.model.email.Attachment;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.model.email.EmailConstants;
import org.slf4j.Logger;

import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

public class DefaultEmailService implements EmailService {

	private static class MailApiSupplier implements Supplier<MailApi> {

		private final Supplier<EmailConfiguration> emailConfigurationSupplier;
		private final MailApiFactory mailApiFactory;

		public MailApiSupplier(final Supplier<EmailConfiguration> emailConfigurationSupplier,
				final MailApiFactory mailApiFactory) {
			this.emailConfigurationSupplier = emailConfigurationSupplier;
			this.mailApiFactory = mailApiFactory;
		}

		@Override
		public MailApi get() {
			final EmailConfiguration configuration = emailConfigurationSupplier.get();
			return mailApiFactory.create(transform(configuration));
		}

		private Configuration.All transform(final EmailConfiguration configuration) {
			logger.trace("configuring mail API with configuration:", configuration);
			return new Configuration.All() {

				@Override
				public boolean isDebug() {
					// TODO use a system property
					return false;
				}

				@Override
				public Logger getLogger() {
					return logger;
				}

				@Override
				public String getOutputProtocol() {
					final boolean useSsl = Boolean.valueOf(configuration.smtpNeedsSsl());
					return useSsl ? PROTOCOL_SMTPS : PROTOCOL_SMTP;
				}

				@Override
				public String getOutputHost() {
					return configuration.getSmtpServer();
				}

				@Override
				public Integer getOutputPort() {
					return configuration.getSmtpPort();
				}

				@Override
				public boolean isStartTlsEnabled() {
					return false;
				}

				@Override
				public String getOutputUsername() {
					return configuration.getEmailUsername();
				}

				@Override
				public String getOutputPassword() {
					return configuration.getEmailPassword();
				}

				@Override
				public List<String> getOutputFromRecipients() {
					return Arrays.asList(configuration.getEmailAddress());
				}

				@Override
				public String getInputProtocol() {
					final boolean useSsl = Boolean.valueOf(configuration.imapNeedsSsl());
					return useSsl ? PROTOCOL_IMAPS : PROTOCOL_IMAP;
				}

				@Override
				public String getInputHost() {
					return configuration.getImapServer();
				}

				@Override
				public Integer getInputPort() {
					return configuration.getImapPort();
				}

				@Override
				public String getInputUsername() {
					return configuration.getEmailUsername();
				}

				@Override
				public String getInputPassword() {
					return configuration.getEmailPassword();
				}

			};
		}

	}

	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

	private static final Map<URL, String> NO_ATTACHMENTS = Collections.emptyMap();

	private final Supplier<EmailConfiguration> emailConfigurationSupplier;
	private final Supplier<MailApi> mailApiSupplier;
	private final EmailPersistence persistence;

	DefaultEmailService( //
			final Supplier<EmailConfiguration> emailConfigurationSupplier, //
			final MailApiFactory mailApiFactory, //
			final EmailPersistence persistence //
	) {
		this.emailConfigurationSupplier = memoize(emailConfigurationSupplier);
		this.mailApiSupplier = memoize(new MailApiSupplier(emailConfigurationSupplier, mailApiFactory));
		this.persistence = persistence;
	}

	@Override
	public void send(final Email email) throws EmailServiceException {
		logger.info("sending email {}", email.getId());
		send(email, NO_ATTACHMENTS);
	}

	@Override
	public void send(final Email email, final Map<URL, String> attachments) throws EmailServiceException {
		logger.info("sending email {} with attachments {}", email.getId(), attachments);
		try {
			final NewMail newMail = mailApiSupplier.get().newMail() //
					.withFrom(from(email.getFromAddress())) //
					.withTo(addressesFrom(email.getToAddresses())) //
					.withCc(addressesFrom(email.getCcAddresses())) //
					.withBcc(addressesFrom(email.getBccAddresses())) //
					.withSubject(email.getSubject()) //
					.withContent(email.getContent()) //
					.withContentType(CONTENT_TYPE);
			for (final Entry<URL, String> attachment : attachments.entrySet()) {
				newMail.withAttachment(attachment.getKey(), attachment.getValue());
			}
			newMail.send();
		} catch (final MailException e) {
			throw EmailServiceException.send(e);
		}
	}

	private String from(final String fromAddress) {
		return defaultIfBlank(fromAddress, emailConfigurationSupplier.get().getEmailAddress());
	}

	private String[] addressesFrom(final String addresses) {
		if (addresses != null) {
			return addresses.split(EmailConstants.ADDRESSES_SEPARATOR);
		}
		return new String[0];
	}

	@Override
	public synchronized Iterable<Email> receive() throws EmailServiceException {
		logger.info("receiving emails");
		final CollectingEmailCallbackHandler callbackHandler = CollectingEmailCallbackHandler.newInstance() //
				.withPredicate(Predicates.<Email> alwaysTrue()) //
				.build();
		receive(callbackHandler);
		return unmodifiableIterable(callbackHandler.getEmails());
	}

	@Override
	public synchronized void receive(final EmailCallbackHandler callback) throws EmailServiceException {
		logger.info("receiving emails");
		/**
		 * Business rule: Consider the configuration of the IMAP Server as check
		 * to sync the e-mails. So don't try to reach always the server but only
		 * if configured
		 */
		if (emailConfigurationSupplier.get().isImapConfigured()) {
			try {
				receive0(callback);
			} catch (final MailException e) {
				logger.error("error receiving mails", e);
				throw EmailServiceException.receive(e);
			}
		} else {
			logger.warn("imap server not configured");
		}
	}

	private void receive0(final EmailCallbackHandler callback) {
		final Iterable<FetchedMail> fetchMails = mailApiSupplier.get() //
				.selectFolder(emailConfigurationSupplier.get().getInputFolder()) //
				.fetch();
		for (final FetchedMail fetchedMail : fetchMails) {
			final SelectMail mailMover = mailApiSupplier.get().selectMail(fetchedMail);
			boolean keepMail = false;
			try {
				final GetMail getMail = mailApiSupplier.get().selectMail(fetchedMail).get();
				final Email email = transform(getMail);
				mailMover.selectTargetFolder(emailConfigurationSupplier.get().getProcessedFolder());
				if (callback.apply(email)) {
					final Long id = persistence.save(email);
					final Email stored = persistence.getEmail(id);
					callback.accept(stored);
				}
			} catch (final Exception e) {
				logger.error("error getting mail", e);
				keepMail = emailConfigurationSupplier.get().keepUnknownMessages();
				mailMover.selectTargetFolder(emailConfigurationSupplier.get().getRejectedFolder());
			}

			try {
				if (!keepMail) {
					mailMover.move();
				}
			} catch (final MailException e) {
				logger.error("error moving mail", e);
			}
		}
	}

	private Email transform(final GetMail getMail) {
		final Email email = new Email();
		email.setFromAddress(getMail.getFrom());
		email.setToAddresses(StringUtils.join(getMail.getTos().iterator(), EmailConstants.ADDRESSES_SEPARATOR));
		email.setCcAddresses(StringUtils.join(getMail.getCcs().iterator(), EmailConstants.ADDRESSES_SEPARATOR));
		email.setSubject(getMail.getSubject());
		email.setContent(getMail.getContent());
		email.setStatus(getMessageStatusFromSender(getMail.getFrom()));
		final List<Attachment> attachments = Lists.newArrayList();
		for (final GetMail.Attachment attachment : getMail.getAttachments()) {
			attachments.add(Attachment.newInstance() //
					.withName(attachment.getName()) //
					.withDataHandler(attachment.getDataHandler()) //
					.build());
		}
		email.setAttachments(attachments);
		log(email);
		return email;
	}

	private EmailStatus getMessageStatusFromSender(final String from) {
		if (emailConfigurationSupplier.get().getEmailAddress().equalsIgnoreCase(from)) {
			// Probably sent from Shark with BCC here
			return EmailStatus.SENT;
		} else {
			return EmailStatus.RECEIVED; // TODO Set as NEW!
		}
	}

	private void log(final Email email) {
		logger.debug("Email");
		logger.debug("\tFrom: {}", email.getFromAddress());
		logger.debug("\tTO: {}", email.getToAddresses());
		logger.debug("\tCC: {}", email.getCcAddresses());
		logger.debug("\tSubject: {}", email.getSubject());
		logger.debug("\tBody:\n{}", email.getContent());
		logger.debug("\tAttachments:");
		for (final Attachment attachment : email.getAttachments()) {
			logger.debug("\t\t- {}", attachment.getName());
		}
	}

	@Override
	public Iterable<EmailTemplate> getEmailTemplates(final Email email) {
		logger.info("getting email templates for email with id '{}'", email.getId());
		final List<EmailTemplate> templates = Lists.newArrayList();
		if (isNotBlank(email.getNotifyWith())) {
			for (final EmailTemplate template : persistence.getEmailTemplates()) {
				if (template.getName().equals(email.getNotifyWith())) {
					templates.add(template);
				}
			}
		} else {
			logger.debug("notification not required");
		}
		return Collections.unmodifiableList(templates);
	}

	@Override
	public Long save(final Email email) {
		logger.info("saving email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		return persistence.save(email);
	}

	@Override
	public void delete(final Email email) {
		logger.info("deleting email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		persistence.delete(email);
	}

	@Override
	public Iterable<Email> getEmails(final Long processId) {
		logger.info("getting emails for process with id '{}'", processId);
		return persistence.getEmails(processId);
	}

	@Override
	public Iterable<Email> getOutgoingEmails(final Long processId) {
		logger.info("getting outgoing emails for process with id '{}'", processId);
		return persistence.getOutgoingEmails(processId);
	}

}

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

	private static class AllConfigurationWrapper implements Configuration.All {

		public static AllConfigurationWrapper of(final EmailAccount account) {
			return new AllConfigurationWrapper(account);
		}

		private final EmailAccount account;

		private AllConfigurationWrapper(final EmailAccount account) {
			this.account = account;
		}

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
			final boolean useSsl = Boolean.valueOf(account.isSmtpSsl());
			return useSsl ? PROTOCOL_SMTPS : PROTOCOL_SMTP;
		}

		@Override
		public String getOutputHost() {
			return account.getSmtpServer();
		}

		@Override
		public Integer getOutputPort() {
			return account.getSmtpPort();
		}

		@Override
		public boolean isStartTlsEnabled() {
			return false;
		}

		@Override
		public String getOutputUsername() {
			return account.getUsername();
		}

		@Override
		public String getOutputPassword() {
			return account.getPassword();
		}

		@Override
		public List<String> getOutputFromRecipients() {
			return Arrays.asList(account.getAddress());
		}

		@Override
		public String getInputProtocol() {
			final boolean useSsl = Boolean.valueOf(account.isImapSsl());
			return useSsl ? PROTOCOL_IMAPS : PROTOCOL_IMAP;
		}

		@Override
		public String getInputHost() {
			return account.getImapServer();
		}

		@Override
		public Integer getInputPort() {
			return account.getImapPort();
		}

		@Override
		public String getInputUsername() {
			return account.getUsername();
		}

		@Override
		public String getInputPassword() {
			return account.getPassword();
		}

	}

	private static class MailApiSupplier implements Supplier<MailApi> {

		private final Supplier<EmailAccount> emailAccountSupplier;
		private final MailApiFactory mailApiFactory;

		public MailApiSupplier(final Supplier<EmailAccount> emailAccountSupplier, final MailApiFactory mailApiFactory) {
			this.emailAccountSupplier = emailAccountSupplier;
			this.mailApiFactory = mailApiFactory;
		}

		@Override
		public MailApi get() {
			final EmailAccount account = emailAccountSupplier.get();
			logger.trace("configuring mail API with configuration:", account);
			return mailApiFactory.create(AllConfigurationWrapper.of(account));
		}

	}

	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

	private static final Map<URL, String> NO_ATTACHMENTS = Collections.emptyMap();

	private final Supplier<EmailAccount> emailAccountSupplier;
	private final Supplier<MailApi> mailApiSupplier;
	private final EmailPersistence persistence;

	DefaultEmailService( //
			final Supplier<EmailAccount> emailConfigurationSupplier, //
			final MailApiFactory mailApiFactory, //
			final EmailPersistence persistence //
	) {
		this.emailAccountSupplier = memoize(emailConfigurationSupplier);
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
		return defaultIfBlank(fromAddress, emailAccountSupplier.get().getAddress());
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
		if (emailAccountSupplier.get().isImapConfigured()) {
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
				.selectFolder(emailAccountSupplier.get().getInputFolder()) //
				.fetch();
		for (final FetchedMail fetchedMail : fetchMails) {
			final SelectMail mailMover = mailApiSupplier.get().selectMail(fetchedMail);
			boolean keepMail = false;
			try {
				final GetMail getMail = mailApiSupplier.get().selectMail(fetchedMail).get();
				final Email email = transform(getMail);
				mailMover.selectTargetFolder(emailAccountSupplier.get().getProcessedFolder());
				if (callback.apply(email)) {
					final Long id = persistence.save(email);
					final Email stored = persistence.getEmail(id);
					callback.accept(stored);
				}
			} catch (final Exception e) {
				logger.error("error getting mail", e);
				keepMail = !emailAccountSupplier.get().isRejectNotMatching();
				mailMover.selectTargetFolder(emailAccountSupplier.get().getRejectedFolder());
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
		if (emailAccountSupplier.get().getAddress().equalsIgnoreCase(from)) {
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

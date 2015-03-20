package org.cmdbuild.services.email;

import static com.google.common.collect.Iterables.unmodifiableIterable;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.data.store.email.EmailStatus.RECEIVED;
import static org.cmdbuild.system.SystemUtils.isMailDebugEnabled;

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
import org.cmdbuild.data.store.email.Attachment;
import org.cmdbuild.data.store.email.Email;
import org.cmdbuild.data.store.email.EmailConstants;
import org.slf4j.Logger;

import com.google.common.base.Predicate;
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
			return isMailDebugEnabled();
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

	private static final Predicate<Email> ALL_EMAILS = Predicates.alwaysTrue();

	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

	private static final Map<URL, String> NO_ATTACHMENTS = Collections.emptyMap();

	private final Supplier<EmailAccount> accountSupplier;
	private final Supplier<MailApi> apiSupplier;

	DefaultEmailService( //
			final Supplier<EmailAccount> emailAccountSupplier, //
			final MailApiFactory mailApiFactory) {
		this.accountSupplier = emailAccountSupplier;
		this.apiSupplier = new MailApiSupplier(emailAccountSupplier, mailApiFactory);
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
			final NewMail newMail = apiSupplier.get().newMail() //
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
			logger.error("error sending email", e);
			throw EmailServiceException.send(e);
		}
	}

	private String from(final String fromAddress) {
		return defaultIfBlank(fromAddress, accountSupplier.get().getAddress());
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
				.withPredicate(ALL_EMAILS) //
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
		if (accountSupplier.get().isImapConfigured()) {
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
		final Iterable<FetchedMail> fetchMails = apiSupplier.get() //
				.selectFolder(accountSupplier.get().getInputFolder()) //
				.fetch();
		for (final FetchedMail fetchedMail : fetchMails) {
			final SelectMail mailMover = apiSupplier.get().selectMail(fetchedMail);
			boolean keepMail = false;
			try {
				final GetMail getMail = apiSupplier.get().selectMail(fetchedMail).get();
				final Email email = transform(getMail);
				mailMover.selectTargetFolder(accountSupplier.get().getProcessedFolder());
				callback.handle(email);
			} catch (final Exception e) {
				logger.error("error getting mail", e);
				keepMail = !accountSupplier.get().isRejectNotMatching();
				mailMover.selectTargetFolder(accountSupplier.get().getRejectedFolder());
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
		email.setStatus(RECEIVED);
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

}

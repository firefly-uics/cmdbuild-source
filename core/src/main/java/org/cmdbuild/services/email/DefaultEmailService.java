package org.cmdbuild.services.email;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.common.mail.FetchedMail;
import org.cmdbuild.common.mail.GetMail;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.MailApi.Configuration;
import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.common.mail.MailException;
import org.cmdbuild.common.mail.SelectMail;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.model.email.Attachment;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.model.email.EmailConstants;
import org.cmdbuild.model.email.EmailTemplate;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailCallbackHandler.RuleAction;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class DefaultEmailService implements EmailService {

	private static RuleAction NULL_ACTION = new RuleAction() {

		@Override
		public void execute() {
			// nothing to do
		}

	};

	private static class CollectingRule implements Rule {

		public static CollectingRule of(final Collection<Email> emails) {
			return new CollectingRule(emails);
		}

		private final Collection<Email> emails;

		public CollectingRule(final Collection<Email> emails) {
			this.emails = emails;
		}

		@Override
		public boolean applies(final Email email) {
			return true;
		}

		@Override
		public Email adapt(final Email email) {
			return email;
		}

		@Override
		public RuleAction action(final Email email) {
			emails.add(email);
			return NULL_ACTION;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
					.toString();
		}

	}

	private final EmailConfiguration configuration;
	private final MailApi mailApi;
	private final EmailPersistence persistence;

	public DefaultEmailService( //
			final EmailConfiguration configuration, //
			final MailApiFactory factory, //
			final EmailPersistence persistence //
	) {
		this.configuration = configuration;
		factory.setConfiguration(transform(configuration));
		this.mailApi = factory.createMailApi();
		this.persistence = persistence;
	}

	private Configuration transform(final EmailConfiguration configuration) {
		logger.trace("configuring mail API with configuration:", configuration);
		return new MailApi.Configuration() {

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

	@Override
	public void send(final Email email) throws EmailServiceException {
		logger.info("sending email {}", email.getId());
		try {
			final MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
			mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
			mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
			mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
			mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
			mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
			CommandMap.setDefaultCommandMap(mc);

			mailApi.newMail() //
					.withFrom(from(email.getFromAddress())) //
					.withTo(addressesFrom(email.getToAddresses())) //
					.withCc(addressesFrom(email.getCcAddresses())) //
					.withBcc(addressesFrom(email.getBccAddresses())) //
					.withSubject(email.getSubject()) //
					.withContent(email.getContent()) //
					.withContentType("text/html; charset=UTF-8") //
					.send();
		} catch (final MailException e) {
			logger.error("error sending mails", e);
			throw EmailServiceException.send(e);
		}
	}

	private String from(final String fromAddress) {
		return defaultIfBlank(fromAddress, configuration.getEmailAddress());
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
		final List<Email> emails = Lists.newArrayList();
		final EmailCallbackHandler callbackHandler = DefaultEmailCallbackHandler.of(CollectingRule.of(emails));
		receive(callbackHandler);
		return Collections.unmodifiableList(emails);
	}

	@Override
	public synchronized void receive(final EmailCallbackHandler callback) throws EmailServiceException {
		logger.info("receiving emails");
		/**
		 * Business rule: Consider the configuration of the IMAP Server as check
		 * to sync the e-mails. So don't try to reach always the server but only
		 * if configured
		 */
		if (configuration.isImapConfigured()) {
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
		final Iterable<FetchedMail> fetchMails = mailApi.selectFolder(configuration.getInputFolder()).fetch();
		for (final FetchedMail fetchedMail : fetchMails) {
			final SelectMail mailMover = mailApi.selectMail(fetchedMail);
			boolean keepMail = false;
			try {
				final GetMail getMail = mailApi.selectMail(fetchedMail).get();
				Email email = transform(getMail);
				mailMover.selectTargetFolder(configuration.getProcessedFolder());
				for (final Rule rule : callback.getRules()) {
					if (rule.applies(email)) {
						email = rule.adapt(email);
						email = persistence.save(email);
						callback.notify(rule.action(email));
					}
				}
			} catch (final Exception e) {
				logger.error("error getting mail", e);
				keepMail = configuration.keepUnknownMessages();
				mailMover.selectTargetFolder(configuration.getRejectedFolder());
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
		if (configuration.getEmailAddress().equalsIgnoreCase(from)) {
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
	public void save(final Email email) {
		logger.info("saving email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		persistence.save(email);
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

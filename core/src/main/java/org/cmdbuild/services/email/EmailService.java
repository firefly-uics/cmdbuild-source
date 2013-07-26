package org.cmdbuild.services.email;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.common.mail.FetchedMail;
import org.cmdbuild.common.mail.GetMail;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.MailApi.Configuration;
import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.common.mail.MailException;
import org.cmdbuild.common.mail.SelectMail;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

/**
 * Service for coordinate e-mail operations and persistence.
 */
public class EmailService {

	private static class EmailServiceException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		private EmailServiceException() {
			// prevents instantiation
		}

		public static RuntimeException receive(final MailException cause) {
			logger.error("error receiving mails", cause);
			return WorkflowExceptionType.WF_EMAIL_CANNOT_RETRIEVE_MAIL.createException();
		}

		public static RuntimeException send(final MailException cause) {
			logger.error("error sending mails", cause);
			return WorkflowExceptionType.WF_EMAIL_NOT_SENT.createException();
		}

	}

	private static final Logger logger = Log.EMAIL;

	private static final String INBOX = "INBOX";
	private static final String IMPORTED = "Imported";
	private static final String REJECTED = "Rejected";

	private final EmailConfiguration configuration;
	private final MailApi mailApi;
	private final EmailPersistence persistence;

	public EmailService(final EmailConfiguration configuration, final MailApiFactory factory,
			final EmailPersistence persistence) {
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

	public void send(final Email email) {
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
					.withFrom(email.getFromAddress()) //
					.withTo(addressesFrom(email.getToAddresses())) //
					.withCc(addressesFrom(email.getCcAddresses())) //
					.withSubject(subjectFrom(email)) //
					.withContent(email.getContent()) //
					.withContentType("text/html; charset=UTF-8") //
					.send();
		} catch (final MailException e) {
			throw EmailServiceException.send(e);
		}
	}

	private String[] addressesFrom(final String addresses) {
		if (addresses != null) {
			return addresses.split(Email.ADDRESSES_SEPARATOR);
		}
		return new String[0];
	}

	private String subjectFrom(final Email email) {
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final Card activityCard = dataAccessLogic.fetchCard("Activity", email.getActivityId().longValue());
		final String emailSubject = String.format("[%s %d] %s", activityCard.getClassName(), email.getActivityId(),
				email.getSubject());
		return defaultIfBlank(emailSubject, EMPTY);
	}

	/**
	 * Retrieves mails from mailbox and stores them.
	 */
	public synchronized Iterable<Email> receive() {
		logger.info("receiving emails");
		final List<Email> emails = Lists.newArrayList();
		try {
			receive0(emails);
		} catch (final MailException e) {
			throw EmailServiceException.receive(e);
		}
		return Collections.unmodifiableList(emails);
	}

	private void receive0(final List<Email> emails) {
		/**
		 * Business rule: Consider the configuration of the IMAP Server as check
		 * to sync the e-mails. So don't try to reach always the server but only
		 * if configured
		 */
		if (!configuration.isImapConfigured()) {
			logger.warn("imap server not properly configured");
			return;
		}

		final Iterable<FetchedMail> fetchMails = mailApi.selectFolder(INBOX).fetch();
		for (final FetchedMail fetchedMail : fetchMails) {
			final SelectMail mailMover = mailApi.selectMail(fetchedMail);
			boolean keepMail = false;
			try {
				final GetMail getMail = mailApi.selectMail(fetchedMail).get();
				final Email email = transform(getMail);
				final Email createdEmail = persistence.create(email);
				emails.add(createdEmail);
				mailMover.selectTargetFolder(IMPORTED);
			} catch (final Exception e) {
				logger.error("error getting mail", e);
				keepMail = configuration.keepUnknownMessages();
				mailMover.selectTargetFolder(REJECTED);
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
		email.setToAddresses(StringUtils.join(getMail.getTos().iterator(), Email.ADDRESSES_SEPARATOR));
		email.setCcAddresses(StringUtils.join(getMail.getCcs().iterator(), Email.ADDRESSES_SEPARATOR));
		email.setSubject(extractSubject(getMail.getSubject()));
		email.setContent(getMail.getContent());
		email.setStatus(getMessageStatusFromSender(getMail.getFrom()));
		email.setActivityId(persistence.getActivityCardFrom(getMail.getSubject()).getId().intValue());
		log(email);
		return email;
	}

	private String extractSubject(final String subject) {
		// TODO handle this in some other way
		final int activitySectionEnd = subject.indexOf("]");
		Validate.isTrue(activitySectionEnd >= 0, "subject does not contains ']' character");
		return subject.substring(activitySectionEnd + 1).trim();
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
		logger.info("Email");
		logger.info("\tFrom: {}", email.getFromAddress());
		logger.info("\tTO: {}", email.getToAddresses());
		logger.info("\tCC: {}", email.getCcAddresses());
		logger.info("\tSubject: {}", email.getSubject());
		logger.info("\tBody:\n{}", email.getContent());
	}

}

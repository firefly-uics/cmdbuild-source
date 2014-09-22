package org.cmdbuild.services.email;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.common.mail.FetchedMail;
import org.cmdbuild.common.mail.GetMail;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.MailApi.Configuration;
import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.common.mail.MailException;
import org.cmdbuild.common.mail.NewMail;
import org.cmdbuild.common.mail.SelectMail;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Attachment;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.model.email.EmailConstants;
import org.cmdbuild.model.email.EmailTemplate;
import org.slf4j.Logger;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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

	private static final Pattern RECIPIENT_TEMPLATE_USER = Pattern.compile("\\[user\\]\\s*(\\w+)");
	private static final Pattern RECIPIENT_TEMPLATE_GROUP = Pattern.compile("\\[group\\]\\s*(\\w+)");
	private static final Pattern RECIPIENT_TEMPLATE_GROUP_USERS = Pattern.compile("\\[groupUsers\\]\\s*(\\w+)");

	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

	private static final Map<URL, String> NO_ATTACHMENTS = Collections.emptyMap();

	private final EmailConfiguration configuration;
	private final MailApi mailApi;
	private final EmailPersistence persistence;
	private final SubjectParser subjectParser;

	public EmailService( //
			final EmailConfiguration configuration, //
			final MailApiFactory factory, //
			final EmailPersistence persistence, //
			final SubjectParser subjectParser //
	) {
		this.configuration = configuration;
		factory.setConfiguration(transform(configuration));
		this.mailApi = factory.createMailApi();
		this.persistence = persistence;
		this.subjectParser = subjectParser;
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

	/**
	 * Sends the specified mail.
	 * 
	 * @param email
	 * 
	 * @throws EmailServiceException
	 *             if there is any problem.
	 */
	public void send(final Email email) throws EmailServiceException {
		logger.info("sending email {}", email.getId());
		send(email, NO_ATTACHMENTS);
	}

	/**
	 * Sends the specified {@link Email} with the specified attachment
	 * {@link URL}s.
	 * 
	 * @param email
	 * @param attachments
	 * 
	 * @throws EmailServiceException
	 *             if there is any problem.
	 */
	public void send(final Email email, final Map<URL, String> attachments) throws EmailServiceException {
		logger.info("sending email {} with attachments {}", email.getId(), attachments);
		try {
			final NewMail newMail = mailApi.newMail() //
					.withFrom(from(email.getFromAddress())) //
					.withTo(addressesFrom(email.getToAddresses())) //
					.withCc(addressesFrom(email.getCcAddresses())) //
					.withBcc(addressesFrom(email.getBccAddresses())) //
					.withSubject(subjectFrom(email)) //
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
		return defaultIfBlank(fromAddress, configuration.getEmailAddress());
	}

	private String[] addressesFrom(final String addresses) {
		if (addresses != null) {
			return addresses.split(EmailConstants.ADDRESSES_SEPARATOR);
		}
		return new String[0];
	}

	private String subjectFrom(final Email email) {
		// TODO move into another component
		final String emailSubject;
		if (email.isNoSubjectPrefix()) {
			emailSubject = email.getSubject();
		} else if (email.getActivityId() != null) {
			final CMCard card = persistence.getProcessCardFrom(email);
			if (StringUtils.isNotBlank(email.getNotifyWith())) {
				emailSubject = String.format("[%s %d %s] %s", //
						card.getType().getIdentifier().getLocalName(), //
						email.getActivityId(), //
						email.getNotifyWith(), //
						email.getSubject());
			} else {
				emailSubject = String.format("[%s %d] %s", //
						card.getType().getIdentifier().getLocalName(), //
						email.getActivityId(), //
						email.getSubject());
			}
		} else {
			emailSubject = email.getSubject();
		}
		return defaultIfBlank(emailSubject, EMPTY);
	}

	/**
	 * Retrieves mails from mailbox and stores them.
	 * 
	 * @throws EmailServiceException
	 *             if there is any problem.
	 */

	public synchronized Iterable<Email> receive() throws EmailServiceException {
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
				// created email does not have attachments
				createdEmail.setAttachments(email.getAttachments());
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
		email.setToAddresses(StringUtils.join(getMail.getTos().iterator(), EmailConstants.ADDRESSES_SEPARATOR));
		email.setCcAddresses(StringUtils.join(getMail.getCcs().iterator(), EmailConstants.ADDRESSES_SEPARATOR));
		email.setSubject(extractSubject(getMail.getSubject()));
		email.setContent(getMail.getContent());
		email.setStatus(getMessageStatusFromSender(getMail.getFrom()));
		email.setActivityId(persistence.getProcessCardFrom(getMail.getSubject()).getId().intValue());
		email.setNotifyWith(extractNotifyWith(getMail.getSubject()));
		final List<Attachment> attachments = Lists.newArrayList();
		for (final GetMail.Attachment attachment : getMail.getAttachments()) {
			attachments.add(Attachment.newInstance() //
					.withName(attachment.getName()) //
					.withUrl(attachment.getUrl()) //
					.build());
		}
		email.setAttachments(attachments);
		log(email);
		return email;
	}

	private String extractSubject(final String subject) {
		return subjectParser.parse(subject).getRealSubject();
	}

	private String extractNotifyWith(final String subject) {
		return subjectParser.parse(subject).getNotification();
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
			logger.debug("\t\t- {}, {}", attachment.getName(), attachment.getUrl());
		}
	}

	/**
	 * Gets all email templates associated with specified email.
	 * 
	 * @param email
	 * 
	 * @return all templates.
	 */
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

	/**
	 * Resolves all recipients according with the following rules:<br>
	 * <br>
	 * <ul>
	 * <li>"{@code [user] foo}": all the e-mail addresses of the user
	 * {@code foo}</li>
	 * <li>"{@code [group] foo}": all the e-mail addresses of the group
	 * {@code foo}</li>
	 * <li>"{@code [groupUsers] foo}": all the e-mail addresses of the users of
	 * the group {@code foo}</li>
	 * <li>the template as is otherwise</li>
	 * </ul>
	 * 
	 * 
	 * @param recipientTemplates
	 *            all templates that needs to be resolved.
	 * 
	 * @return the resolved templates.
	 */
	public Iterable<String> resolveRecipients(final Iterable<String> recipientTemplates) {
		logger.info("resolving recipients: {}", Iterables.toString(recipientTemplates));
		final Set<String> resolved = Sets.newHashSet();
		for (final String template : recipientTemplates) {
			resolved.addAll(resolve(template));
		}
		return resolved;
	}

	private Collection<? extends String> resolve(final String template) {
		logger.debug("resolving '{}'", template);
		final Set<String> resolved = Sets.newHashSet();
		do {
			if (isBlank(template)) {
				break;
			}

			Matcher matcher = RECIPIENT_TEMPLATE_USER.matcher(template);
			if (matcher.find()) {
				logger.debug("resolving '{}' as an user", template);
				final String user = matcher.group(1);
				Iterables.addAll(resolved, persistence.getEmailsForUser(user));
				break;
			}

			matcher = RECIPIENT_TEMPLATE_GROUP.matcher(template);
			if (matcher.find()) {
				logger.debug("resolving '{}' as a group", template);
				final String group = matcher.group(1);
				Iterables.addAll(resolved, persistence.getEmailsForGroup(group));
				break;
			}

			matcher = RECIPIENT_TEMPLATE_GROUP_USERS.matcher(template);
			if (matcher.find()) {
				logger.debug("resolving '{}' as all group's users", template);
				final String group = matcher.group(1);
				Iterables.addAll(resolved, persistence.getEmailsForGroupUsers(group));
				break;
			}

			logger.debug("resolving '{}' as is", template);
			resolved.add(template);
		} while (false);
		return resolved;
	}

	/**
	 * Saves the specified {@link Email} and returns the created or updated
	 * {@link Email#Id}.
	 * 
	 * @param email
	 * 
	 * @return the created or updated {@link Email#getId()}.
	 */
	public Long save(final Email email) {
		logger.info("saving email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		return persistence.save(email);
	}

	public void delete(final Email email) {
		logger.info("deleting email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		persistence.delete(email);
	}

	public Iterable<Email> getEmails(final Long processId) {
		logger.info("getting emails for process with id '{}'", processId);
		return persistence.getEmails(processId);
	}

	public Iterable<Email> getOutgoingEmails(final Long processId) {
		logger.info("getting outgoing emails for process with id '{}'", processId);
		return persistence.getOutgoingEmails(processId);
	}

}

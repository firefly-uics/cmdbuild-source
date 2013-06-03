package org.cmdbuild.services.email;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.data.converter.EmailConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.Email;
import org.cmdbuild.model.Email.EmailStatus;
import org.cmdbuild.model.data.Card;

public class EmailService {

	private final EmailConfiguration configuration;

	public EmailService(final EmailConfiguration configuration) {
		this.configuration = configuration;
	}

	public synchronized void syncEmail() throws IOException {
		final Session imapSession = getImapSession();

		try {
			final Store store = imapSession.getStore();
			store.connect();

			final Folder inbox = store.getFolder("INBOX");
			final Folder importedFolder = getOrCreateFolder(store, "Imported");
			final Folder rejectedFolder = getOrCreateFolder(store, "Rejected");

			inbox.open(Folder.READ_WRITE);
			final Message messages[] = inbox.getMessages();
			final Message[] singleMessageArray = new Message[1];
			for (int i = 0, n = messages.length; i < n; ++i) {
				final Message currentMessage = messages[i];
				singleMessageArray[0] = currentMessage;
				Folder destinationFolder = importedFolder;
				try {
					final Email email = getEmailFrom(currentMessage);
					logEmail(i, email);
					save(email);
				} catch (final Exception e) {
					Log.EMAIL.warn("Invalid email");
					destinationFolder = rejectedFolder;
				}
				inbox.copyMessages(singleMessageArray, destinationFolder);
				inbox.setFlags(singleMessageArray, new Flags(Flags.Flag.DELETED), true);
			}
			inbox.expunge();
			store.close();
		} catch (final MessagingException e) {
			Log.CMDBUILD.debug("Error connecting to the mailbox", e);
			throw WorkflowExceptionType.WF_EMAIL_CANNOT_RETRIEVE_MAIL.createException();
		}
	}

	private Email getEmailFrom(final Message message) throws MessagingException, IOException {
		final Email email = new Email();
		final String fromHeader = extractFrom(message);
		email.setFromAddress(fromHeader);
		final EmailStatus emailStatus = getMessageStatusFromSender(fromHeader);
		email.setStatus(emailStatus);
		email.setToAddresses(extractTO(message));
		email.setCcAddresses(extractCC(message));
		email.setSubject(extractSubject(message));
		email.setContent(extractBody(message));
		email.setActivityId(extractActivity(message).getId().intValue());
		return email;
	}

	private EmailStatus getMessageStatusFromSender(final String fromHeader) throws AddressException {
		final InternetAddress emailFromAddress = new InternetAddress(fromHeader);
		final InternetAddress wfFromAddress = new InternetAddress(configuration.getEmailAddress());
		if (emailFromAddress.getAddress().equalsIgnoreCase(wfFromAddress.getAddress())) {
			// Probably sent from Shark with BCC here
			return EmailStatus.SENT;
		} else {
			return EmailStatus.RECEIVED; // TODO Set as NEW!
		}
	}

	private String extractFrom(final Message message) throws MessagingException {
		final String[] fromHeaders = message.getHeader("From");
		if (fromHeaders != null && fromHeaders.length > 0) {
			return fromHeaders[0];
		} else {
			return "";
		}
	}

	private String extractTO(final Message message) throws MessagingException {
		final String[] toHeaders = message.getHeader("TO");
		if (toHeaders != null && toHeaders.length > 0) {
			return toHeaders[0];
		} else {
			return "";
		}
	}

	private String extractCC(final Message message) throws MessagingException {
		final String[] ccHeaders = message.getHeader("CC");
		if (ccHeaders != null && ccHeaders.length > 0) {
			return ccHeaders[0];
		} else {
			return "";
		}
	}

	private String extractSubject(final Message message) throws MessagingException {
		final String emailSubject = message.getSubject();
		if (emailSubject == null) {
			throw new IllegalArgumentException();
		}
		final int activitySectionEnd = emailSubject.indexOf("]");
		if (activitySectionEnd < 0) {
			throw new IllegalArgumentException();
		}
		return emailSubject.substring(activitySectionEnd + 1).trim();
	}

	private String extractBody(final Message message) throws MessagingException, IOException {
		final Object messageContent = message.getContent();
		if (messageContent == null) {
			throw new IllegalArgumentException();
		}
		if (messageContent instanceof Multipart) {
			final Multipart mp = (Multipart) messageContent;
			for (int i = 0, n = mp.getCount(); i < n; ++i) {
				final Part part = mp.getBodyPart(i);
				final String disposition = part.getDisposition();
				if (disposition == null) {
					return part.getContent().toString();
				}
			}
			return "";
		}
		return messageContent.toString();
	}

	private Card extractActivity(final Message message) throws MessagingException {
		final String emailSubject = message.getSubject();
		final Pattern activityExtractor = Pattern.compile("[^\\[]*\\[(\\S+)\\s+(\\d+)\\]");
		final Matcher activityParts = activityExtractor.matcher(emailSubject);
		if (!activityParts.lookingAt()) {
			throw new IllegalArgumentException();
		}
		final String activityClassName = activityParts.group(1);
		final Integer activityId = Integer.parseInt(activityParts.group(2));
		try {
			final Card activity = fetchActivityFrom(activityClassName, activityId);
			return activity;
		} catch (final NotFoundException e) {
		}
		throw new IllegalArgumentException();
	}

	private Card fetchActivityFrom(final String activityClassName, final Integer activityId) {
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		return dataAccessLogic.fetchCard(activityClassName, activityId.longValue());
	}

	private void logEmail(final int i, final Email email) {
		Log.EMAIL.info(String.format("Email %d", i));
		Log.EMAIL.info(String.format("  From: %s", email.getFromAddress()));
		Log.EMAIL.info(String.format("  TO: %s", email.getToAddresses()));
		Log.EMAIL.info(String.format("  CC: %s", email.getCcAddresses()));
		Log.EMAIL.info(String.format("  Subject: %s", email.getSubject()));
		Log.EMAIL.info(String.format("  Body:\n%s", email.getContent()));
	}

	private Session getImapSession() {
		if (configuration.isImapConfigured()) {
			final Properties imapProps = configuration.getImapProps();
			final Authenticator auth = new BasicAuthenticator(configuration);
			final Session session = Session.getDefaultInstance(imapProps, auth);
			return session;
		} else {
			throw WorkflowExceptionType.WF_EMAIL_NOT_CONFIGURED.createException();
		}
	}

	public Session getSmtpSession() {
		if (configuration.isSmtpConfigured()) {
			final Properties smtpProps = configuration.getSmtpProps();
			final Authenticator auth = new BasicAuthenticator(configuration);
			final Session session = Session.getDefaultInstance(smtpProps, auth);
			return session;
		} else {
			throw WorkflowExceptionType.WF_EMAIL_NOT_CONFIGURED.createException();
		}
	}

	private Folder getOrCreateFolder(final Store store, final String name) throws MessagingException {
		final Folder folder = store.getFolder(name);
		if (!folder.exists()) {
			folder.create(Folder.HOLDS_MESSAGES);
		}
		return folder;
	}

	private void save(final Email email) {
		final LookupStore lookupStore = applicationContext().getBean(LookupStore.class);
		final StorableConverter<Email> converter = new EmailConverter(lookupStore, null);
		final DataViewStore<Email> emailStore = new DataViewStore<Email>(
				TemporaryObjectsBeforeSpringDI.getSystemView(), converter);
		emailStore.create(email);
	}

	public void sendEmail(final Email email) {
		try {
			final Session smtpSession = getSmtpSession();
			Log.EMAIL.info(String.format("Sending email %d", email.getId()));
			final MimeMessage msg = new MimeMessage(smtpSession);
			msg.setFrom(new InternetAddress(email.getFromAddress()));
			if (email.getToAddresses() != null) {
				msg.setRecipients(RecipientType.TO, email.getToAddresses());
			}
			if (email.getCcAddresses() != null) {
				msg.setRecipients(RecipientType.CC, email.getCcAddresses());
			}
			final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
			final Card activityCard = dataAccessLogic.fetchCard("Activity", email.getActivityId().longValue());
			final String activityName = activityCard.getAttribute("Code", String.class);
			final String emailSubject = String.format("[%s %d] %s", activityName, email.getActivityId(),
					email.getSubject());
			if (emailSubject != null) {
				msg.setSubject(emailSubject);
			}
			final Multipart mp = new MimeMultipart("alternative");
			final BodyPart bp = new MimeBodyPart();
			bp.setContent(email.getContent(), "text/html; charset=UTF-8");
			mp.addBodyPart(bp);
			msg.setContent(mp);
			Transport.send(msg);
		} catch (final Exception e) {
			Log.EMAIL.error("Can't send email: " + e.getMessage(), e);
			throw WorkflowExceptionType.WF_EMAIL_NOT_SENT.createException();
		}
	}

}

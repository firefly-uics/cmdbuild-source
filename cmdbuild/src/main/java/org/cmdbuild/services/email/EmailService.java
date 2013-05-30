package org.cmdbuild.services.email;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.SSLSocketFactory;

import org.cmdbuild.config.EmailProperties;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.wrappers.EmailCard;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;

public class EmailService {
	
	private static final String SSL_FACTORY = SSLSocketFactory.class.getName();
	private static final Authenticator NO_AUTHENTICATION = null;
	
	private static Authenticator authenticator() {
		return authenticationRequired() ? new BasicAuthenticator() : NO_AUTHENTICATION;
	}

	private static boolean authenticationRequired() {
		final EmailProperties emailProperties = EmailProperties.getInstance();
		return isNotBlank(emailProperties.getEmailUsername());
	}

	synchronized public static void syncEmail() throws IOException {
		Session imapSession = getImapSession();

		try {
			Store store = imapSession.getStore();
			store.connect();

			Folder inbox = store.getFolder("INBOX");
			Folder importedFolder = getOrCreateFolder(store, "Imported");
			Folder rejectedFolder = getOrCreateFolder(store, "Rejected");

			inbox.open(Folder.READ_WRITE);
			Message messages[] = inbox.getMessages();
			Message[] singleMessageArray = new Message[1];
			for (int i = 0, n = messages.length; i < n; ++i) {
				Message currentMessage = messages[i];
				singleMessageArray[0] = currentMessage;
				Folder destinationFolder = importedFolder;
				try {
					EmailCard email = new EmailCard(currentMessage);
					logEmail(i, email);
					email.save();
				} catch (Exception e) {
					Log.EMAIL.warn("Invalid email");
					final boolean keepUnknownMessages = EmailProperties.getInstance().keepUnknownMessages();
					destinationFolder = keepUnknownMessages ? null : rejectedFolder;
				}
				if (destinationFolder != null) {
					inbox.copyMessages(singleMessageArray, destinationFolder);
					inbox.setFlags(singleMessageArray, new Flags(Flags.Flag.DELETED), true);
				}
			}
			inbox.expunge();
			store.close();
		} catch (MessagingException e) {
			Log.OTHER.debug("Error connecting to the mailbox", e);
			throw WorkflowExceptionType.WF_EMAIL_CANNOT_RETRIEVE_MAIL.createException();
		}
	}

	private static void logEmail(int i, EmailCard email) {
		Log.EMAIL.info(String.format("Email %d", i));
		Log.EMAIL.info(String.format("  From: %s", email.getFrom()));
		Log.EMAIL.info(String.format("  TO: %s", email.getTO()));
		Log.EMAIL.info(String.format("  CC: %s", email.getCC()));
		Log.EMAIL.info(String.format("  Subject: %s", email.getSubject()));
		Log.EMAIL.info(String.format("  Body:\n%s", email.getBody()));
	}

	private static Session getImapSession() {
		EmailProperties emailconf = EmailProperties.getInstance();
		if (emailconf.isImapConfigured()) {
			Properties imapProps = getImapProps();
			Authenticator auth = authenticator();
			Session session = Session.getDefaultInstance(imapProps, auth);
			return session;
		} else {
			throw WorkflowExceptionType.WF_EMAIL_NOT_CONFIGURED
					.createException();
		}
	}
	
	private static Properties getImapProps() {
		final EmailProperties emailProperties = EmailProperties.getInstance();
		final Properties imapProps = System.getProperties();
		if (emailProperties.imapNeedsSsl()) {
			// imapProps.put("mail.imap.host", getImapServer());
			// imapProps.put("mail.imap.ssl.enable", true);
			// imapProps.put("mail.store.protocol", "imap");
			imapProps.put("mail.imaps.host", emailProperties.getImapServer());
			imapProps.put("mail.store.protocol", "imaps");
			imapProps.put("mail.imap.socketFactory.class", SSL_FACTORY);
		} else {
			imapProps.put("mail.imap.host", emailProperties.getImapServer());
			imapProps.put("mail.store.protocol", "imap");
		}
		final Integer imapPort = emailProperties.getImapPort();
		if (imapPort != null) {
			imapProps.put("mail.imap.port", imapPort.toString());
			imapProps.put("mail.imap.socketFactory.port", imapPort.toString());
		}
		return imapProps;
	}

	private static Session getSmtpSession() {
		EmailProperties emailconf = EmailProperties.getInstance();
		if (emailconf.isSmtpConfigured()) {
			Properties smtpProps = getSmtpProps();
			Authenticator auth = authenticator();
			Session session = Session.getDefaultInstance(smtpProps, auth);
			return session;
		} else {
			throw WorkflowExceptionType.WF_EMAIL_NOT_CONFIGURED
					.createException();
		}
	}

	private static Properties getSmtpProps() {
		final EmailProperties emailProperties = EmailProperties.getInstance();
		final Properties smtpProps = System.getProperties();
		smtpProps.put("mail.transport.protocol", "smtp");
		smtpProps.put("mail.host", emailProperties.getSmtpServer());
		smtpProps.put("mail.smtp.host", emailProperties.getSmtpServer());
		final Integer smtpPort = emailProperties.getSmtpPort();
		if (smtpPort != null) {
			smtpProps.put("mail.smtp.port", smtpPort.toString());
			smtpProps.put("mail.smtp.socketFactory.port", smtpPort.toString());
		}
		if (emailProperties.smtpNeedsSsl()) {
			smtpProps.put("mail.smtp.socketFactory.class", SSL_FACTORY);
			smtpProps.put("mail.smtp.socketFactory.fallback", "false");
			smtpProps.setProperty("mail.smtp.quitwait", "false");
		}
		smtpProps.put("mail.smtp.auth", authenticationRequired() ? "true" : "false");
		return smtpProps;
	}

	private static Folder getOrCreateFolder(Store store, String name)
			throws MessagingException {
		Folder folder = store.getFolder(name);
		if (!folder.exists())
			folder.create(Folder.HOLDS_MESSAGES);
		return folder;
	}

	public static void createOrUpdateProcessEmail(ICard processCard,
			Map<String, String> values) {
		EmailCard email = getOrCreateEmail(processCard, values);
		updateEmailValues(values, email);
		email.save();
	}

	private static EmailCard getOrCreateEmail(ICard processCard,
			Map<String, String> values) {
		int emailCardId = getEmailIdFromValues(values);
		if (emailCardId > 0) {
			return EmailCard.get(processCard, emailCardId);
		} else {
			return EmailCard.create(processCard);
		}
	}

	public static void sendEmail(EmailCard email) {
		try {
			Session smtpSession = EmailService.getSmtpSession();
			Log.EMAIL.info(String.format("Sending email %d", email.getId()));
			MimeMessage msg = new MimeMessage(smtpSession);
			msg.setFrom(new InternetAddress(email.getFrom()));
			if(email.getTO()!=null)
				msg.setRecipients(RecipientType.TO, email.getTO());
			if(email.getCC()!=null)
				msg.setRecipients(RecipientType.CC, email.getCC());
			String emailSubject = String.format("[%s %d] %s", email
					.getActivityName(), email.getActivityId(), email
					.getSubject());
			if(emailSubject!=null)
				msg.setSubject(emailSubject);
			Multipart mp = new MimeMultipart("alternative");
			BodyPart bp = new MimeBodyPart();
			bp.setContent(email.getBody(), "text/html; charset=UTF-8");
			mp.addBodyPart(bp);
			msg.setContent(mp);
			Transport.send(msg);
		} catch (Exception e) {
			Log.EMAIL.error("Can't send email: " + e.getMessage());
			Log.EMAIL.debug(e);
			throw WorkflowExceptionType.WF_EMAIL_NOT_SENT.createException();
		}
	}

	private static int getEmailIdFromValues(Map<String, String> values) {
		try {
			return Integer.parseInt(values.get(ICard.CardAttributes.Id
					.toString()));
		} catch (Exception e) {
			return 0;
		}
	}

	private static void updateEmailValues(Map<String, String> values,
			EmailCard email) {
		email.setTO(values.get(EmailCard.TOAttr));
		email.setSubject(values.get(EmailCard.SubjectAttr));
		email.setBody(values.get(EmailCard.BodyAttr));
		email.setCC(values.get(EmailCard.CCAttr));
		email.setEmailStatus(values.get(EmailCard.EmailStatusAttr + "_value"));
	}

	public static boolean isConfigured() {
		EmailProperties emailconf = EmailProperties.getInstance();
		return emailconf.isImapConfigured() && emailconf.isSmtpConfigured();
	}
}
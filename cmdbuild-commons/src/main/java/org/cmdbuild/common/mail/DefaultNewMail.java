package org.cmdbuild.common.mail;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.cmdbuild.common.mail.JavaxMailConstants.CONTENT_TYPE_TEXT_PLAIN;
import static org.cmdbuild.common.mail.JavaxMailConstants.FALSE;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_DEBUG;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMPT_SOCKET_FACTORY_CLASS;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMPT_SOCKET_FACTORY_FALLBACK;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTPS_AUTH;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTPS_HOST;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTPS_PORT;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTP_AUTH;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTP_HOST;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTP_PORT;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTP_STARTTLS_ENABLE;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_TRANSPORT_PROTOCOL;
import static org.cmdbuild.common.mail.JavaxMailConstants.SMTPS;
import static org.cmdbuild.common.mail.JavaxMailConstants.SSL_FACTORY;
import static org.cmdbuild.common.mail.JavaxMailConstants.TRUE;

import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.cmdbuild.common.mail.MailApi.OutputConfiguration;

class DefaultNewMail implements NewMail {

	private static final PasswordAuthenticator NO_AUTENTICATION = null;

	private final OutputConfiguration configuration;

	private final List<String> froms;
	private final Map<RecipientType, Set<String>> recipients;
	private String subject;
	private String body;
	private String contentType;
	private final Set<URL> attachments;

	private Message message;

	public DefaultNewMail(final OutputConfiguration configuration) {
		this.configuration = configuration;

		this.froms = new ArrayList<String>();

		this.recipients = new HashMap<RecipientType, Set<String>>();
		recipients.put(RecipientType.TO, new HashSet<String>());
		recipients.put(RecipientType.CC, new HashSet<String>());
		recipients.put(RecipientType.BCC, new HashSet<String>());

		contentType = CONTENT_TYPE_TEXT_PLAIN;

		attachments = new HashSet<URL>();
	}

	@Override
	public NewMail withFrom(final String from) {
		froms.add(from);
		return this;
	}

	@Override
	public NewMail withTo(final String to) {
		recipients.get(RecipientType.TO).add(to);
		return this;
	}

	@Override
	public NewMail withCc(final String cc) {
		recipients.get(RecipientType.CC).add(cc);
		return this;
	}

	@Override
	public NewMail withBcc(final String bcc) {
		recipients.get(RecipientType.BCC).add(bcc);
		return this;
	}

	@Override
	public NewMail withSubject(final String subject) {
		this.subject = subject;
		return this;
	}

	@Override
	public NewMail withContent(final String body) {
		this.body = body;
		return this;
	}

	@Override
	public NewMail withContentType(final String contentType) {
		this.contentType = contentType;
		return this;
	}

	@Override
	public NewMail withAttachment(final URL url) {
		attachments.add(url);
		return this;
	}

	@Override
	public void send() {
		try {
			final Session session = createSession();
			message = messageFrom(session);
			setFrom();
			addRecipients();
			setSubject();
			setSentDate();
			setBody();
			send(session);
		} catch (final MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Session createSession() {
		final Properties properties = createConfigurationProperties();
		final Authenticator authenticator = getAutenticator();
		return Session.getInstance(properties, authenticator);
	}

	private MimeMessage messageFrom(final Session session) {
		return new MimeMessage(session);
	}

	private Properties createConfigurationProperties() {
		final Properties properties = new Properties();
		properties.setProperty(MAIL_DEBUG, Boolean.toString(configuration.isDebug()));
		properties.setProperty(MAIL_TRANSPORT_PROTOCOL, configuration.getOutputProtocol());
		properties.setProperty(MAIL_SMTP_STARTTLS_ENABLE, configuration.isStartTlsEnabled() ? TRUE : FALSE);
		final String auth = authenticationRequired() ? TRUE : FALSE;
		if (sslRequired()) {
			properties.setProperty(MAIL_SMTPS_HOST, configuration.getOutputHost());
			if (configuration.getOutputPort() != null) {
				properties.setProperty(MAIL_SMTPS_PORT, configuration.getOutputPort().toString());
			}
			properties.setProperty(MAIL_SMTPS_AUTH, auth);
			properties.setProperty(MAIL_SMPT_SOCKET_FACTORY_CLASS, SSL_FACTORY);
			properties.setProperty(MAIL_SMPT_SOCKET_FACTORY_FALLBACK, FALSE);
		} else {
			properties.setProperty(MAIL_SMTP_HOST, configuration.getOutputHost());
			if (configuration.getOutputPort() != null) {
				properties.setProperty(MAIL_SMTP_PORT, configuration.getOutputPort().toString());
			}
			properties.setProperty(MAIL_SMTP_AUTH, auth);
		}
		return properties;
	}

	private boolean sslRequired() {
		return SMTPS.equals(configuration.getOutputProtocol());
	}

	private boolean authenticationRequired() {
		return isNotBlank(configuration.getOutputUsername());
	}

	private Authenticator getAutenticator() {
		return authenticationRequired() ? PasswordAuthenticator.from(configuration) : NO_AUTENTICATION;
	}

	private void setFrom() throws MessagingException {
		final List<Address> addresses = new ArrayList<Address>();
		final List<String> fromsSource = froms.isEmpty() ? configuration.getOutputFromRecipients() : froms;
		for (final String address : fromsSource) {
			addresses.add(new InternetAddress(address));
		}
		message.addFrom(addresses.toArray(new Address[addresses.size()]));
	}

	private void addRecipients() throws MessagingException {
		for (final RecipientType type : asList(RecipientType.TO, RecipientType.CC, RecipientType.BCC)) {
			for (final String recipient : recipients.get(type)) {
				final Address address = new InternetAddress(recipient);
				message.addRecipient(type, address);
			}
		}
	}

	private void setSubject() throws MessagingException {
		message.setSubject(subject);
	}

	private void setSentDate() throws MessagingException {
		message.setSentDate(GregorianCalendar.getInstance().getTime());
	}

	private void setBody() throws MessagingException {
		final Part textPart;
		if (hasAttachments()) {
			textPart = new MimeBodyPart();
			final Multipart multipart = new MimeMultipart();
			multipart.addBodyPart((BodyPart) textPart);
			addAttachmentBodyParts(multipart);
			message.setContent(multipart);
		} else {
			textPart = message;
		}

		if (isBlank(contentType) || CONTENT_TYPE_TEXT_PLAIN.equals(contentType)) {
			textPart.setText(body);
		} else {
			textPart.setContent(body, contentType);
		}
	}

	private boolean hasAttachments() {
		return !attachments.isEmpty();
	}

	private void addAttachmentBodyParts(final Multipart multipart) throws MessagingException {
		for (final URL attachment : attachments) {
			final BodyPart bodyPart = getBodyPartFor(attachment);
			multipart.addBodyPart(bodyPart);
		}
	}

	private BodyPart getBodyPartFor(final URL file) throws MessagingException {
		final BodyPart bodyPart = new MimeBodyPart();
		final DataSource source = new URLDataSource(file);
		bodyPart.setDataHandler(new DataHandler(source));
		bodyPart.setFileName(file.getPath());
		return bodyPart;
	}

	private void send(final Session session) throws MessagingException {
		Transport transport = null;
		try {
			transport = connect(session);
			transport.sendMessage(message, message.getAllRecipients());
		} catch (final MessagingException e) {
			throw e;
		} finally {
			closeIfOpened(transport);
		}
	}

	private Transport connect(final Session session) throws MessagingException {
		final Transport transport = session.getTransport();
		transport.connect();
		return transport;
	}

	private void closeIfOpened(final Transport transport) {
		if (transport != null && transport.isConnected()) {
			try {
				transport.close();
			} catch (final MessagingException e) {
			}
		}
	}

}

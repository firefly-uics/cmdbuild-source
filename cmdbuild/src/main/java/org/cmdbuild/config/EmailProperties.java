package org.cmdbuild.config;

import java.security.Security;
import java.util.Properties;

import org.cmdbuild.services.Settings;

@SuppressWarnings("restriction")
public class EmailProperties extends DefaultProperties implements EmailConfiguration {

	private static final long serialVersionUID = 8184420208391927123L;

	private static final String MODULE_NAME = "email";

	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	private static final String EMAIL_ADDRESS = "email.address";
	private static final String SMTP_SERVER = "email.smtp.server";
	private static final String SMTP_PORT = "email.smtp.port";
	private static final String SMTP_SSL = "email.smtp.ssl";
	private static final String IMAP_SERVER = "email.imap.server";
	private static final String IMAP_PORT = "email.imap.port";
	private static final String IMAP_SSL = "email.imap.ssl";
	private static final String EMAIL_USERNAME = "email.username";
	private static final String EMAIL_PASSWORD = "email.password";

	public EmailProperties() {
		super();
		setProperty(EMAIL_ADDRESS, "");
		setProperty(SMTP_SERVER, "");
		setProperty(SMTP_PORT, ""); // check later
		setProperty(SMTP_SSL, "false");
		setProperty(IMAP_SERVER, "");
		setProperty(IMAP_PORT, ""); // check later
		setProperty(IMAP_SSL, "false");
		setProperty(EMAIL_USERNAME, "");
		setProperty(EMAIL_PASSWORD, "");
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	}

	public static EmailProperties getInstance() {
		return (EmailProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public String getImapServer() {
		return getProperty(IMAP_SERVER);
	}

	@Override
	public Integer getImapPort() {
		try {
			return Integer.valueOf(getProperty(IMAP_PORT));
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean imapNeedsSsl() {
		return Boolean.valueOf(getProperty(IMAP_SSL));
	}

	@Override
	public String getSmtpServer() {
		return getProperty(SMTP_SERVER);
	}

	@Override
	public Integer getSmtpPort() {
		try {
			return Integer.valueOf(getProperty(SMTP_PORT));
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean smtpNeedsSsl() {
		return Boolean.valueOf(getProperty(SMTP_SSL));
	}

	@Override
	public String getEmailAddress() {
		return getProperty(EMAIL_ADDRESS);
	}

	@Override
	public String getEmailUsername() {
		return getProperty(EMAIL_USERNAME);
	}

	@Override
	public String getEmailPassword() {
		return getProperty(EMAIL_PASSWORD);
	}

	@Override
	public boolean isImapConfigured() {
		return !("".equals(getImapServer()) || "".equals(getEmailUsername()) || "".equals(getEmailPassword()));
	}

	@Override
	public boolean isSmtpConfigured() {
		return !("".equals(getSmtpServer()) || "".equals(getEmailAddress()));
	}

	@Override
	public Properties getSmtpProps() {
		final Properties smtpProps = System.getProperties();
		smtpProps.put("mail.transport.protocol", "smtp");
		smtpProps.put("mail.host", getSmtpServer());
		smtpProps.put("mail.smtp.host", getSmtpServer());
		addSmtpPortIfPresent(smtpProps);
		if (smtpNeedsSsl()) {
			smtpProps.put("mail.smtp.socketFactory.class", SSL_FACTORY);
			smtpProps.put("mail.smtp.socketFactory.fallback", "false");
			smtpProps.setProperty("mail.smtp.quitwait", "false");
		}
		smtpProps.put("mail.smtp.auth", "true");
		return smtpProps;
	}

	private void addSmtpPortIfPresent(final Properties imapProps) {
		final Integer smtpPort = getSmtpPort();
		if (smtpPort != null) {
			imapProps.put("mail.smtp.port", smtpPort.toString());
			imapProps.put("mail.smtp.socketFactory.port", smtpPort.toString());
		}
	}

	@Override
	public Properties getImapProps() {
		final Properties imapProps = System.getProperties();
		if (imapNeedsSsl()) {
			// imapProps.put("mail.imap.host", getImapServer());
			// imapProps.put("mail.imap.ssl.enable", true);
			// imapProps.put("mail.store.protocol", "imap");
			imapProps.put("mail.imaps.host", getImapServer());
			imapProps.put("mail.store.protocol", "imaps");
			imapProps.put("mail.imap.socketFactory.class", SSL_FACTORY);
		} else {
			imapProps.put("mail.imap.host", getImapServer());
			imapProps.put("mail.store.protocol", "imap");
		}
		addImapPortIfPresent(imapProps);
		return imapProps;
	}

	private void addImapPortIfPresent(final Properties imapProps) {
		final Integer imapPort = getImapPort();
		if (imapPort != null) {
			imapProps.put("mail.imap.port", imapPort.toString());
			imapProps.put("mail.imap.socketFactory.port", imapPort.toString());
		}
	}
}

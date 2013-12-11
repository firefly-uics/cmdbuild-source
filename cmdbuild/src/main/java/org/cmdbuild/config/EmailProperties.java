package org.cmdbuild.config;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.security.Security;

import org.cmdbuild.services.Settings;

@SuppressWarnings("restriction")
public class EmailProperties extends DefaultProperties implements EmailConfiguration {

	private static final long serialVersionUID = 8184420208391927123L;

	private static final String MODULE_NAME = "email";

	private static final String EMAIL_ADDRESS = "email.address";
	private static final String SMTP_SERVER = "email.smtp.server";
	private static final String SMTP_PORT = "email.smtp.port";
	private static final String SMTP_SSL = "email.smtp.ssl";
	private static final String IMAP_SERVER = "email.imap.server";
	private static final String IMAP_PORT = "email.imap.port";
	private static final String IMAP_SSL = "email.imap.ssl";
	private static final String EMAIL_USERNAME = "email.username";
	private static final String EMAIL_PASSWORD = "email.password";
	private static final String EMAIL_MESSAGES_UNKNOWN_KEEP = "email.messages.unknown.keep";
	private static final String EMAIL_SERVICE_DELAY = "email.service.delay";

	public EmailProperties() {
		super();
		setProperty(EMAIL_ADDRESS, EMPTY);
		setProperty(SMTP_SERVER, EMPTY);
		setProperty(SMTP_PORT, EMPTY); // check later
		setProperty(SMTP_SSL, "false");
		setProperty(IMAP_SERVER, EMPTY);
		setProperty(IMAP_PORT, EMPTY); // check later
		setProperty(IMAP_SSL, "false");
		setProperty(EMAIL_USERNAME, EMPTY);
		setProperty(EMAIL_PASSWORD, EMPTY);
		setProperty(EMAIL_MESSAGES_UNKNOWN_KEEP, "false");
		setProperty(EMAIL_SERVICE_DELAY, EMPTY);
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
		return isNotBlank(getImapServer()) && isNotBlank(getEmailUsername()) && isNotBlank(getEmailPassword());
	}

	@Override
	public boolean isSmtpConfigured() {
		return isNotBlank(getSmtpServer()) && isNotBlank(getEmailAddress());
	}

	@Override
	public boolean keepUnknownMessages() {
		return Boolean.valueOf(getProperty(EMAIL_MESSAGES_UNKNOWN_KEEP));
	}

	/**
	 * Minutes to wait to
	 * check the in-box and
	 * read the incoming emails
	 */
	@Override
	public Integer emailServiceDelay() {
		try {
			return Integer.valueOf(getProperty(EMAIL_SERVICE_DELAY));
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	@Override
	public void accept(final PropertiesVisitor visitor) {
		visitor.visit(this);
	}
}

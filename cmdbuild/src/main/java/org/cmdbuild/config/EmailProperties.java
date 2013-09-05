package org.cmdbuild.config;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.security.Security;

import org.cmdbuild.logic.setup.EmailModule;
import org.cmdbuild.services.Settings;

@SuppressWarnings("restriction")
public class EmailProperties extends DefaultProperties implements EmailConfiguration {

	private static final long serialVersionUID = 8184420208391927123L;

	private static final String MODULE_NAME = "email";

	private static final String EMAIL_MESSAGES_UNKNOWN_KEEP = "email.messages.unknown.keep";
	private static final String EMAIL_SERVICE_DELAY = "email.service.delay";

	public EmailProperties() {
		super();
		setProperty(EmailModule.EMAIL_ADDRESS, EMPTY);
		setProperty(EmailModule.SMTP_SERVER, EMPTY);
		setProperty(EmailModule.SMTP_PORT, EMPTY); // check later
		setProperty(EmailModule.SMTP_SSL, "false");
		setProperty(EmailModule.IMAP_SERVER, EMPTY);
		setProperty(EmailModule.IMAP_PORT, EMPTY); // check later
		setProperty(EmailModule.IMAP_SSL, "false");
		setProperty(EmailModule.EMAIL_USERNAME, EMPTY);
		setProperty(EmailModule.EMAIL_PASSWORD, EMPTY);
		setProperty(EMAIL_MESSAGES_UNKNOWN_KEEP, "false");
		setProperty(EMAIL_SERVICE_DELAY, EMPTY);
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	}

	public static EmailProperties getInstance() {
		return (EmailProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public String getImapServer() {
		return getProperty(EmailModule.IMAP_SERVER);
	}

	@Override
	public Integer getImapPort() {
		try {
			return Integer.valueOf(getProperty(EmailModule.IMAP_PORT));
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean imapNeedsSsl() {
		return Boolean.valueOf(getProperty(EmailModule.IMAP_SSL));
	}

	@Override
	public String getSmtpServer() {
		return getProperty(EmailModule.SMTP_SERVER);
	}

	@Override
	public Integer getSmtpPort() {
		try {
			return Integer.valueOf(getProperty(EmailModule.SMTP_PORT));
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean smtpNeedsSsl() {
		return Boolean.valueOf(getProperty(EmailModule.SMTP_SSL));
	}

	@Override
	public String getEmailAddress() {
		return getProperty(EmailModule.EMAIL_ADDRESS);
	}

	@Override
	public String getEmailUsername() {
		return getProperty(EmailModule.EMAIL_USERNAME);
	}

	@Override
	public String getEmailPassword() {
		return getProperty(EmailModule.EMAIL_PASSWORD);
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
	 * Minutes to wait to check the in-box and read the incoming emails
	 */
	@Override
	public Integer emailServiceDelay() {
		try {
			return Integer.valueOf(getProperty(EMAIL_SERVICE_DELAY));
		} catch (final NumberFormatException e) {
			return null;
		}
	}

}

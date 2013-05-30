package org.cmdbuild.config;

import java.security.Security;

import org.cmdbuild.services.Settings;

@SuppressWarnings("restriction")
public class EmailProperties extends DefaultProperties {

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
		setProperty(EMAIL_MESSAGES_UNKNOWN_KEEP, "false");
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	}

	public static EmailProperties getInstance() {
		return (EmailProperties)Settings.getInstance().getModule(MODULE_NAME);
	}

    public String getImapServer() {
        return getProperty(IMAP_SERVER);
    }

    public Integer getImapPort() {
    	try {
    		return Integer.valueOf(getProperty(IMAP_PORT));
    	} catch (NumberFormatException e) {
    		return null;
    	}
    }

    public boolean imapNeedsSsl() {
        return Boolean.valueOf(getProperty(IMAP_SSL));
    }

    public String getSmtpServer() {
        return getProperty(SMTP_SERVER);
    }

    public Integer getSmtpPort() {
    	try {
    		return Integer.valueOf(getProperty(SMTP_PORT));
    	} catch (NumberFormatException e) {
    		return null;
    	}
    }

    public boolean smtpNeedsSsl() {
        return Boolean.valueOf(getProperty(SMTP_SSL));
    }

    public String getEmailAddress() {
        return getProperty(EMAIL_ADDRESS);
    }

    public String getEmailUsername() {
        return getProperty(EMAIL_USERNAME);
    }

    public String getEmailPassword() {
        return getProperty(EMAIL_PASSWORD);
    }

    public boolean isImapConfigured() {
        return !("".equals(getImapServer()) ||
        		"".equals(getEmailUsername()) ||
                "".equals(getEmailPassword()));
    }

    public boolean isSmtpConfigured() {
        return !("".equals(getSmtpServer()) ||
        		"".equals(getEmailAddress()));
    }

	public boolean keepUnknownMessages() {
		return Boolean.valueOf(getProperty(EMAIL_MESSAGES_UNKNOWN_KEEP));
	}

}

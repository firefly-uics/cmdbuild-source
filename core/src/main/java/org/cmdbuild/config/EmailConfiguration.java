package org.cmdbuild.config;

import java.util.Properties;

public interface EmailConfiguration {

	String getImapServer();

	Integer getImapPort();

	boolean imapNeedsSsl();

	String getSmtpServer();

	Integer getSmtpPort();

	boolean smtpNeedsSsl();

	String getEmailAddress();

	String getEmailUsername();

	String getEmailPassword();

	boolean isImapConfigured();

	boolean isSmtpConfigured();

	Properties getSmtpProps();

	Properties getImapProps();

}

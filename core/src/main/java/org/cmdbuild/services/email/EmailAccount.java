package org.cmdbuild.services.email;

public interface EmailAccount {

	String getName();

	boolean isDefault();

	String getUsername();

	String getPassword();

	String getAddress();

	String getSmtpServer();

	Integer getSmtpPort();

	boolean isSmtpSsl();

	boolean isSmtpConfigured();

	String getImapServer();

	Integer getImapPort();

	boolean isImapSsl();

	boolean isImapConfigured();

	String getInputFolder();

	String getProcessedFolder();

	String getRejectedFolder();

	boolean isRejectNotMatching();

}
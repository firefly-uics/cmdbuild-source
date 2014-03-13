package org.cmdbuild.data.store.email;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;

public class EmailAccount implements Storable {

	private final Long id;

	private boolean isDefault;
	private String name;
	private String address;
	private String username;
	private String password;
	private String smtpServer;
	private Integer smtpPort;
	private boolean smtpSsl;
	private String imapServer;
	private Integer imapPort;
	private boolean imapSsl;
	private String inputFolder;
	private String processedFolder;
	private String rejectedFolder;
	private boolean rejectNotMatching;

	public EmailAccount() {
		this(null);
	}

	public EmailAccount(final Long id) {
		this.id = id;
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(final boolean isDefault) {
		this.isDefault = isDefault;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(final String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public Integer getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(final Integer smtpPort) {
		this.smtpPort = smtpPort;
	}

	public boolean isSmtpSsl() {
		return smtpSsl;
	}

	public void setSmtpSsl(final boolean smtpSsl) {
		this.smtpSsl = smtpSsl;
	}

	public String getImapServer() {
		return imapServer;
	}

	public void setImapServer(final String imapServer) {
		this.imapServer = imapServer;
	}

	public Integer getImapPort() {
		return imapPort;
	}

	public void setImapPort(final Integer imapPort) {
		this.imapPort = imapPort;
	}

	public boolean isImapSsl() {
		return imapSsl;
	}

	public void setImapSsl(final boolean imapSsl) {
		this.imapSsl = imapSsl;
	}

	public String getInputFolder() {
		return inputFolder;
	}

	public void setInputFolder(final String inputFolder) {
		this.inputFolder = inputFolder;
	}

	public String getProcessedFolder() {
		return processedFolder;
	}

	public void setProcessedFolder(final String processedFolder) {
		this.processedFolder = processedFolder;
	}

	public String getRejectedFolder() {
		return rejectedFolder;
	}

	public void setRejectedFolder(final String rejectedFolder) {
		this.rejectedFolder = rejectedFolder;
	}

	public boolean isRejectNotMatching() {
		return rejectNotMatching;
	}

	public void setRejectNotMatching(final boolean rejectNotMatching) {
		this.rejectNotMatching = rejectNotMatching;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}

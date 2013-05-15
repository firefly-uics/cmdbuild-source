package org.cmdbuild.model;

public class AbstractEmail {

	private String toAddresses;
	private String ccAddresses;
	private String subject;
	private String content;

	public String getToAddresses() {
		return toAddresses;
	}

	public void setToAddresses(final String toAddresses) {
		this.toAddresses = toAddresses;
	}

	public String getCcAddresses() {
		return ccAddresses;
	}

	public void setCcAddresses(final String ccAddresses) {
		this.ccAddresses = ccAddresses;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}
}

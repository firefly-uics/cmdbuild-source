package org.cmdbuild.model.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageEmail extends Widget {

	public static class EmailTemplate {
		private String toAddresses;
		private String ccAddresses;
		private String subject;
		private String content;
		private String condition;

		public String getToAddresses() {
			return toAddresses;
		}
		public void setToAddresses(String toAddresses) {
			this.toAddresses = toAddresses;
		}
		public String getCcAddresses() {
			return ccAddresses;
		}
		public void setCcAddresses(String ccAddresses) {
			this.ccAddresses = ccAddresses;
		}
		public String getSubject() {
			return subject;
		}
		public void setSubject(String subject) {
			this.subject = subject;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public String getCondition() {
			return condition;
		}
		public void setCondition(String condition) {
			this.condition = condition;
		}
	}

	private boolean readOnly;

	private Collection<EmailTemplate> emailTemplates;
	private Map<String, String> templates;

	public ManageEmail() {
		super();
		emailTemplates = new ArrayList<EmailTemplate>();
		templates = new HashMap<String, String>();
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public List<EmailTemplate> getEmailTemplates() {
		return new ArrayList<EmailTemplate>(emailTemplates);
	}

	public void setEmailTemplates(Collection<EmailTemplate> emailTemplates) {
		if (emailTemplates == null) {
			emailTemplates = new ArrayList<EmailTemplate>();
		}

		this.emailTemplates = emailTemplates;
	}

	public Map<String, String> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<String, String> templates) {
		this.templates = templates;
	}
}
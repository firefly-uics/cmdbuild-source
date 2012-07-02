package org.cmdbuild.model.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ManageEmail extends Widget {

	public static class Template {
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

	private Collection<Template> templates;

	public ManageEmail() {
		super();
		this.templates = new ArrayList<Template>();
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public List<Template> getTemplates() {
		return new ArrayList<Template>(templates);
	}

	public void setTemplates(Collection<Template> templates) {
		if (templates == null) {
			templates = new ArrayList<Template>();
		}

		this.templates = templates;
	}

	public void addTemplate(Template template) {
		this.templates.add(template);
	}
}
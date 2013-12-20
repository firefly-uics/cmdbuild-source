package org.cmdbuild.model.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.model.AbstractEmail;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.workflow.CMActivityInstance;

public class ManageEmail extends Widget {

	private static final String UPDATED_SUBMISSION_PARAM = "Updated";
	private static final String DELETED_SUBMISSION_PARAM = "Deleted";

	private static class Submission {
		public List<Email> updated = new ArrayList<Email>();
		public List<Long> deleted = new ArrayList<Long>();
	}

	public static class EmailTemplate extends AbstractEmail {
		private String condition;

		public String getCondition() {
			return condition;
		}

		public void setCondition(final String condition) {
			this.condition = condition;
		}
	}

	private boolean readOnly;

	private final EmailLogic emailLogic;

	private Collection<EmailTemplate> emailTemplates;
	private Map<String, String> templates;

	public ManageEmail(final EmailLogic emailLogic) {
		super();
		this.emailLogic = emailLogic;
		this.emailTemplates = new ArrayList<EmailTemplate>();
		this.templates = new HashMap<String, String>();
	}

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(final boolean readOnly) {
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

	public void setTemplates(final Map<String, String> templates) {
		this.templates = templates;
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output)
			throws Exception {
		if (readOnly) {
			return;
		}
		final Submission submission = decodeInput(input);
		deleteEmails(activityInstance, submission.deleted);
		updateEmails(activityInstance, submission.updated);
	}

	private Submission decodeInput(final Object input) {
		@SuppressWarnings("unchecked")
		final Map<String, List<?>> inputMap = (Map<String, List<?>>) input;
		final Submission emails = new Submission();
		fillEmails(emails.updated, inputMap.get(UPDATED_SUBMISSION_PARAM));
		fillIds(emails.deleted, inputMap.get(DELETED_SUBMISSION_PARAM));
		return emails;
	}

	private void fillEmails(final List<Email> emailList, final List<?> emailObjectList) {
		@SuppressWarnings("unchecked")
		final List<Map<String, Object>> emailMapList = (List<Map<String, Object>>) emailObjectList;
		for (final Map<String, Object> emailMap : emailMapList) {
			emailList.add(newEmailInstance(emailMap));
		}
	}

	private void fillIds(final List<Long> emailIds, final List<?> idsObjectList) {
		@SuppressWarnings("unchecked")
		final List<Number> idsList = (List<Number>) idsObjectList;
		for (final Number id : idsList) {
			emailIds.add(id.longValue());
		}
	}

	private Email newEmailInstance(final Map<String, Object> emailMap) {
		final Email email;
		if (emailMap.containsKey("id")) {
			final long id = ((Number) emailMap.get("id")).longValue();
			email = new Email(id);
		} else {
			email = new Email();
			email.setStatus(EmailStatus.DRAFT);
		}
		email.setFromAddress((String) emailMap.get("fromAddress"));
		email.setToAddresses((String) emailMap.get("toAddresses"));
		email.setCcAddresses((String) emailMap.get("ccAddresses"));
		email.setSubject((String) emailMap.get("subject"));
		email.setContent((String) emailMap.get("content"));
		email.setNotifyWith((String) emailMap.get("notifyWith"));
		return email;
	}

	private void deleteEmails(final CMActivityInstance activityInstance, final List<Long> deletedEmails) {
		final Long processCardId = activityInstance.getProcessInstance().getCardId();
		for (final Long emailId : deletedEmails) {
			emailLogic.deleteEmail(processCardId, emailId);
		}
	}

	private void updateEmails(final CMActivityInstance activityInstance, final List<Email> updatedEmails) {
		final Long processCardId = activityInstance.getProcessInstance().getCardId();
		for (final Email email : updatedEmails) {
			emailLogic.saveEmail(processCardId, email);
		}
	}

	@Override
	public void advance(final CMActivityInstance activityInstance) {
		final Long processCardId = activityInstance.getProcessInstance().getCardId();
		emailLogic.sendOutgoingAndDraftEmails(processCardId);
	}

}
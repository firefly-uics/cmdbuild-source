package org.cmdbuild.model.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.logic.EmailLogic;
import org.cmdbuild.logic.EmailLogic.AbstractEmail;
import org.cmdbuild.logic.EmailLogic.Email;
import org.cmdbuild.workflow.CMActivityInstance;

public class ManageEmail extends Widget {

	private static final String UPDATED_SUBMISSION_PARAM = "Updated";
	private static final String DELETED_SUBMISSION_PARAM = "Deleted";

	private static class Submission {
		public List<Email> updated = new ArrayList<Email>();
		public List<Email> deleted = new ArrayList<Email>();
	}

	public static class EmailTemplate extends AbstractEmail {
		private String condition;

		public String getCondition() {
			return condition;
		}
		public void setCondition(String condition) {
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

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output) throws Exception {
		if (readOnly) {
			return;
		}
		final Submission submission = decodeInput(input);
		deleteEmails(activityInstance, submission.deleted);
		updateEmails(activityInstance, submission.updated);
	}

	private Submission decodeInput(final Object input) {
		@SuppressWarnings("unchecked") final Map<String, List<Map<String, Object>>> inputMap = (Map<String, List<Map<String, Object>>>) input;
		final Submission emails = new Submission();
		fillEmails(emails.updated, inputMap.get(UPDATED_SUBMISSION_PARAM));
		fillEmails(emails.deleted, inputMap.get(DELETED_SUBMISSION_PARAM));
		return emails;
	}

	private void fillEmails(final List<Email> emailList, final List<Map<String, Object>> emailMapList) {
		for (final Map<String, Object> emailMap : emailMapList) {
			emailList.add(newEmailInstance(emailMap));
		}
	}

	private Email newEmailInstance(final Map<String, Object> emailMap) {
		final Email email;
		if (emailMap.containsKey("id")) {
			long id = ((Number) emailMap.get("id")).longValue();
			email = new Email(id);
		} else {
			email = new Email();
		}
		email.setFromAddress((String) emailMap.get("fromAddress"));
		email.setToAddresses((String) emailMap.get("toAddresses"));
		email.setCcAddresses((String) emailMap.get("ccAddresses"));
		email.setSubject((String) emailMap.get("subject"));
		email.setContent((String) emailMap.get("content"));
		return email;
	}

	private void deleteEmails(final CMActivityInstance activityInstance, final List<Email> deletedEmails) {
		final Long processCardId = activityInstance.getProcessInstance().getCardId();
		for (final Email email : deletedEmails) {
			emailLogic.deleteEmail(processCardId, email);
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
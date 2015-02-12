package org.cmdbuild.model.widget;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailLogic.EmailSubmission;
import org.cmdbuild.model.AbstractEmail;
import org.cmdbuild.workflow.CMActivityInstance;

import com.google.common.collect.Lists;

public class ManageEmail extends Widget {

	private static final String UPDATED_SUBMISSION_PARAM = "Updated";
	private static final String DELETED_SUBMISSION_PARAM = "Deleted";

	private static final String ID_ATTRIBUTE = "id";
	private static final String FROM_ADDRESS_ATTRIBUTE = "fromAddress";
	private static final String TO_ADDRESSES_ATTRIBUTE = "toAddresses";
	private static final String CC_ADDRESSES_ATTRIBUTE = "ccAddresses";
	private static final String BCC_ADDRESSES_ATTRIBUTE = "bccAddresses";
	private static final String SUBJECT_ATTRIBUTE = "subject";
	private static final String CONTENT_ATTRIBUTE = "content";
	private static final String NOTIFY_WITH_ATTRIBUTE = "notifyWith";
	private final static String NO_SUBJECT_PREFIX = "noSubjectPrefix";
	private static final String ACCOUNT_ATTRIBUTE = "account";
	private static final String TEMPORARY_ID = "temporaryId";

	private static class Submission {

		public final Iterable<EmailSubmission> updated;
		public final Iterable<Long> deleted;

		public Submission( //
				final Iterable<EmailSubmission> updated, //
				final Iterable<Long> deleted //
		) {
			this.updated = updated;
			this.deleted = deleted;
		}

	}

	public static class EmailTemplate extends AbstractEmail {

		private static final Map<String, String> NO_VARIABLES = Collections.emptyMap();

		private String key;
		private String condition;
		private Map<String, String> variables;
		private boolean noSubjectPrefix;
		private String account;

		public String getKey() {
			return key;
		}

		public void setKey(final String name) {
			this.key = name;
		}

		public String getCondition() {
			return condition;
		}

		public void setCondition(final String condition) {
			this.condition = condition;
		}

		public Map<String, String> getVariables() {
			return defaultIfNull(variables, NO_VARIABLES);
		}

		public void setVariables(final Map<String, String> variables) {
			this.variables = variables;
		}

		public boolean isNoSubjectPrefix() {
			return noSubjectPrefix;
		}

		public void setNoSubjectPrefix(final boolean noSubjectPrefix) {
			this.noSubjectPrefix = noSubjectPrefix;
		}

		public String getAccount() {
			return account;
		}

		public void setAccount(final String account) {
			this.account = account;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private boolean readOnly;

	private final EmailLogic emailLogic;

	private Collection<EmailTemplate> emailTemplates;
	private boolean noSubjectPrefix;

	public ManageEmail(final EmailLogic emailLogic) {
		super();
		this.emailLogic = emailLogic;
		this.emailTemplates = Lists.newArrayList();
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
		return Lists.newArrayList(emailTemplates);
	}

	public void setEmailTemplates(Collection<EmailTemplate> emailTemplates) {
		if (emailTemplates == null) {
			emailTemplates = Collections.emptyList();
		}

		this.emailTemplates = emailTemplates;
	}

	public boolean isNoSubjectPrefix() {
		return noSubjectPrefix;
	}

	public void setNoSubjectPrefix(final boolean noSubjectPrefix) {
		this.noSubjectPrefix = noSubjectPrefix;
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output)
			throws Exception {
		final Submission submission = decodeInput(input);
		deleteEmails(activityInstance, submission.deleted);
		updateEmails(activityInstance, submission.updated);
	}

	private Submission decodeInput(final Object input) {
		@SuppressWarnings("unchecked")
		final Map<String, List<?>> inputMap = (Map<String, List<?>>) input;
		return new Submission( //
				decodeUpdatedEmails(inputMap.get(UPDATED_SUBMISSION_PARAM)), //
				decodeDeletedEmailIds(inputMap.get(DELETED_SUBMISSION_PARAM)) //
		);
	}

	private Iterable<EmailSubmission> decodeUpdatedEmails(final List<?> emailObjectList) {
		final List<EmailSubmission> emailSubmissions = Lists.newArrayList();
		@SuppressWarnings("unchecked")
		final List<Map<String, Object>> emailMapList = (List<Map<String, Object>>) emailObjectList;
		for (final Map<String, Object> emailMap : emailMapList) {
			emailSubmissions.add(decodeEmailSubmission(emailMap));
		}
		return emailSubmissions;
	}

	private EmailSubmission decodeEmailSubmission(final Map<String, Object> emailMap) {
		final EmailSubmission email;
		if (emailMap.containsKey(ID_ATTRIBUTE)) {
			final long id = ((Number) emailMap.get(ID_ATTRIBUTE)).longValue();
			email = new EmailSubmission(id);
		} else {
			email = new EmailSubmission();
		}
		email.setFromAddress((String) emailMap.get(FROM_ADDRESS_ATTRIBUTE));
		email.setToAddresses((String) emailMap.get(TO_ADDRESSES_ATTRIBUTE));
		email.setCcAddresses((String) emailMap.get(CC_ADDRESSES_ATTRIBUTE));
		email.setBccAddresses((String) emailMap.get(BCC_ADDRESSES_ATTRIBUTE));
		email.setSubject((String) emailMap.get(SUBJECT_ATTRIBUTE));
		email.setContent((String) emailMap.get(CONTENT_ATTRIBUTE));
		email.setNotifyWith((String) emailMap.get(NOTIFY_WITH_ATTRIBUTE));
		email.setNoSubjectPrefix((Boolean) emailMap.get(NO_SUBJECT_PREFIX));
		email.setAccount((String) emailMap.get(ACCOUNT_ATTRIBUTE));
		email.setTemporaryId((String) emailMap.get(TEMPORARY_ID));
		return email;
	}

	private Iterable<Long> decodeDeletedEmailIds(final List<?> idsObjectList) {
		final List<Long> emailIds = Lists.newArrayList();
		@SuppressWarnings("unchecked")
		final List<Number> idsList = (List<Number>) idsObjectList;
		for (final Number id : idsList) {
			emailIds.add(id.longValue());
		}
		return emailIds;
	}

	private void deleteEmails(final CMActivityInstance activityInstance, final Iterable<Long> deletedEmails) {
		final Long processCardId = activityInstance.getProcessInstance().getCardId();
		emailLogic.deleteEmails(processCardId, deletedEmails);
	}

	private void updateEmails(final CMActivityInstance activityInstance, final Iterable<EmailSubmission> updatedEmails) {
		final Long processCardId = activityInstance.getProcessInstance().getCardId();
		emailLogic.saveEmails(processCardId, updatedEmails);
	}

	@Override
	public void advance(final CMActivityInstance activityInstance) {
		final Long processCardId = activityInstance.getProcessInstance().getCardId();
		emailLogic.sendOutgoingAndDraftEmails(processCardId);
	}

}
package org.cmdbuild.model.widget;

import static com.google.common.base.Predicates.or;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.draft;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.outgoing;
import static org.cmdbuild.logic.email.Predicates.statusIs;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.logic.email.EmailAttachmentsLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailLogic.Email;
import org.cmdbuild.logic.email.EmailLogic.ForwardingEmail;
import org.cmdbuild.logic.email.EmailLogic.Status;
import org.cmdbuild.model.AbstractEmail;
import org.cmdbuild.workflow.CMActivityInstance;

import static com.google.common.collect.Maps.*;

public class ManageEmail extends Widget {

	public static class EmailTemplate extends AbstractEmail {

		private static final Map<String, String> NO_VARIABLES = Collections.emptyMap();

		private String key;
		private String condition;
		private Map<String, String> variables;
		private boolean noSubjectPrefix;
		private String account;
		private boolean keepSynchronization = true;
		private boolean promptSynchronization;

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

		public boolean isKeepSynchronization() {
			return keepSynchronization;
		}

		public void setKeepSynchronization(final boolean keepSynchronization) {
			this.keepSynchronization = keepSynchronization;
		}

		public boolean isPromptSynchronization() {
			return promptSynchronization;
		}

		public void setPromptSynchronization(final boolean promptSynchronization) {
			this.promptSynchronization = promptSynchronization;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private boolean readOnly;

	private final EmailLogic emailLogic;
	private final EmailAttachmentsLogic emailAttachmentsLogic;

	private Collection<EmailTemplate> templates;
	private boolean noSubjectPrefix;

	public ManageEmail(final EmailLogic emailLogic, final EmailAttachmentsLogic emailAttachmentsLogic) {
		super();
		this.emailLogic = emailLogic;
		this.emailAttachmentsLogic = emailAttachmentsLogic;
		this.templates = newArrayList();
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

	public List<EmailTemplate> getTemplates() {
		return newArrayList(templates);
	}

	public void setTemplates(Collection<EmailTemplate> templates) {
		if (templates == null) {
			templates = Collections.emptyList();
		}

		this.templates = templates;
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
		final Map<String, Object> inputMap = (Map<String, Object>) input;
		final Long instanceId = activityInstance.getProcessInstance().getCardId();
		final Long submittedInstanceId = Number.class.cast(inputMap.get(DEFAULT_SUBMISSION_PARAM)).longValue();
		final Iterable<Email> emails = from(emailLogic.readAll(submittedInstanceId)).filter(statusIs(draft()));
		final Collection<Email> toBeDeleted = newArrayList();
		final Map<Email, Email> toBeCreatedWithOriginal = newHashMap();
		for (final Email email : emails) {
			if (email.isTemporary()) {
				toBeDeleted.add(email);
				toBeCreatedWithOriginal.put(new ForwardingEmail() {

					@Override
					protected Email delegate() {
						return email;
					}

					@Override
					public boolean isTemporary() {
						return false;
					}

					@Override
					public Long getActivityId() {
						return instanceId;
					}

				}, email);
			}
		}
		for (final Email email : toBeDeleted) {
			emailLogic.delete(email);
			for (final EmailAttachmentsLogic.Attachment attachment : emailAttachmentsLogic.readAll(email)) {
				emailAttachmentsLogic.delete(email, attachment);
			}
		}
		for (final Entry<Email, Email> entry : toBeCreatedWithOriginal.entrySet()) {
			final Email toBeCreated = entry.getKey();
			final Email original = entry.getValue();
			emailLogic.create(toBeCreated);
			for (final EmailAttachmentsLogic.Attachment attachment : emailAttachmentsLogic.readAll(original)) {
				emailAttachmentsLogic.copy(toBeCreated, attachment);
				emailAttachmentsLogic.delete(original, attachment);
			}
		}
	}

	@Override
	public void advance(final CMActivityInstance activityInstance) {
		final Long instanceId = activityInstance.getProcessInstance().getCardId();
		final Iterable<Email> emails = from(emailLogic.readAll(instanceId)) //
				.filter(or(statusIs(draft()), statusIs(outgoing())));
		for (final Email email : emails) {
			if (email.isTemporary()) {
				logger.warn("temporary e-mail should not be found on advancement");
				emailLogic.delete(email);
			} else {
				emailLogic.update(new ForwardingEmail() {

					@Override
					protected Email delegate() {
						return email;
					}

					@Override
					public Status getStatus() {
						return outgoing();
					}

				});
			}
		}
	}

}
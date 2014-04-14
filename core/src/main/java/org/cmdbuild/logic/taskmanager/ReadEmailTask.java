package org.cmdbuild.logic.taskmanager;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.Maps;

public class ReadEmailTask implements ScheduledTask {

	public static class Builder implements org.cmdbuild.common.Builder<ReadEmailTask> {

		private static final Map<String, String> EMPTY_ATTRIBUTES = Collections.emptyMap();

		private Long id;
		private String description;
		private Boolean active;
		private String cronExpression;
		private String emailAccount;
		private String regexFromFilter;
		private String regexSubjectFilter;
		private Boolean notificationRuleActive;
		private Boolean attachmentsRuleActive;
		private String attachmentsRuleCategory;
		private Boolean workflowRuleActive;
		private String workflowClassName;
		private final Map<String, String> workflowAttributes = Maps.newHashMap();
		private Boolean workflowAdvanceable;
		private Boolean workflowAttachments;
		private String workflowAttachmentsCategory;

		private Builder() {
			// use factory method
		}

		@Override
		public ReadEmailTask build() {
			validate();
			return new ReadEmailTask(this);
		}

		private void validate() {
			active = (active == null) ? false : active;

			notificationRuleActive = (notificationRuleActive == null) ? false : notificationRuleActive;

			attachmentsRuleActive = (attachmentsRuleActive == null) ? false : attachmentsRuleActive;
			if (attachmentsRuleActive) {
				Validate.notNull(attachmentsRuleCategory, "missing attachments category");
			}
			workflowRuleActive = (workflowRuleActive == null) ? false : workflowRuleActive;
			workflowAdvanceable = (workflowAdvanceable == null) ? false : workflowAdvanceable;
			workflowAttachments = (workflowAttachments == null) ? false : workflowAttachments;
			if (workflowRuleActive) {
				Validate.notNull(workflowClassName, "missing workflow's class name");
				if (workflowAttachments) {
					Validate.notNull(workflowAttachmentsCategory, "missing workflow's attachments category");
				}
			}
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withActiveStatus(final boolean active) {
			this.active = active;
			return this;
		}

		public Builder withCronExpression(final String cronExpression) {
			this.cronExpression = cronExpression;
			return this;
		}

		public Builder withEmailAccount(final String emailAccount) {
			this.emailAccount = emailAccount;
			return this;
		}

		public Builder withRegexFromFilter(final String regexFromFilter) {
			this.regexFromFilter = regexFromFilter;
			return this;
		}

		public Builder withRegexSubjectFilter(final String regexSubjectFilter) {
			this.regexSubjectFilter = regexSubjectFilter;
			return this;
		}

		public Builder withNotificationStatus(final Boolean notificationRuleActive) {
			this.notificationRuleActive = notificationRuleActive;
			return this;
		}

		public Builder withAttachmentsActive(final Boolean attachmentsRuleActive) {
			this.attachmentsRuleActive = attachmentsRuleActive;
			return this;
		}

		public Builder withAttachmentsCategory(final String category) {
			this.attachmentsRuleCategory = category;
			return this;
		}

		public Builder withWorkflowActive(final Boolean workflowRuleActive) {
			this.workflowRuleActive = workflowRuleActive;
			return this;
		}

		public Builder withWorkflowClassName(final String workflowClassName) {
			this.workflowClassName = workflowClassName;
			return this;
		}

		public Builder withWorkflowAttributes(final Map<String, String> workflowAttributes) {
			this.workflowAttributes.putAll(defaultIfNull(workflowAttributes, EMPTY_ATTRIBUTES));
			return this;
		}

		public Builder withWorkflowAdvanceableStatus(final Boolean workflowAdvanceable) {
			this.workflowAdvanceable = workflowAdvanceable;
			return this;
		}

		public Builder withWorkflowAttachmentsStatus(final Boolean workflowAttachments) {
			this.workflowAttachments = workflowAttachments;
			return this;
		}

		public Builder withWorkflowAttachmentsCategory(final String workflowAttachmentsCategory) {
			this.workflowAttachmentsCategory = workflowAttachmentsCategory;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String description;
	private final boolean active;
	private final String cronExpression;
	private final String emailAccount;
	private final String regexFromFilter;
	private final String regexSubjectFilter;
	private final boolean notificationActive;
	private final boolean attachmentsActive;
	private final String attachmentsCategory;
	private final boolean workflowActive;
	private final String workflowClassName;
	private final Map<String, String> workflowAttributes;
	private final boolean workflowAdvanceable;
	private final boolean workflowAttachments;
	private final String workflowAttachmentsCategory;

	private ReadEmailTask(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.active = builder.active;
		this.cronExpression = builder.cronExpression;
		this.emailAccount = builder.emailAccount;
		this.regexFromFilter = builder.regexFromFilter;
		this.regexSubjectFilter = builder.regexSubjectFilter;
		this.notificationActive = builder.notificationRuleActive;
		this.attachmentsActive = builder.attachmentsRuleActive;
		this.attachmentsCategory = builder.attachmentsRuleCategory;
		this.workflowActive = builder.workflowRuleActive;
		this.workflowClassName = builder.workflowClassName;
		this.workflowAttributes = builder.workflowAttributes;
		this.workflowAdvanceable = builder.workflowAdvanceable;
		this.workflowAttachments = builder.workflowAttachments;
		this.workflowAttachmentsCategory = builder.workflowAttachmentsCategory;
	}

	@Override
	public void accept(final TaskVistor visitor) {
		visitor.visit(this);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public String getCronExpression() {
		return cronExpression;
	}

	public String getEmailAccount() {
		return emailAccount;
	}

	public boolean isNotificationActive() {
		return notificationActive;
	}

	public String getRegexFromFilter() {
		return regexFromFilter;
	}

	public String getRegexSubjectFilter() {
		return regexSubjectFilter;
	}

	public boolean isAttachmentsActive() {
		return attachmentsActive;
	}

	public String getAttachmentsCategory() {
		return attachmentsCategory;
	}

	public boolean isWorkflowActive() {
		return workflowActive;
	}

	public String getWorkflowClassName() {
		return workflowClassName;
	}

	public Map<String, String> getWorkflowAttributes() {
		return workflowAttributes;
	}

	public boolean isWorkflowAdvanceable() {
		return workflowAdvanceable;
	}

	public boolean isWorkflowAttachments() {
		return workflowAttachments;
	}

	public String getWorkflowAttachmentsCategory() {
		return workflowAttachmentsCategory;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}

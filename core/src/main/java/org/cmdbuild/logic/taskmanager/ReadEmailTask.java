package org.cmdbuild.logic.taskmanager;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collections;
import java.util.Map;

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
		private Boolean workflowRuleActive;
		private String workflowClassName;
		private final Map<String, String> workflowAttributes = Maps.newHashMap();
		private Boolean workflowAdvanceable;
		private Boolean workflowAttachments;

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
			workflowRuleActive = (workflowRuleActive == null) ? false : workflowRuleActive;
			workflowAdvanceable = (workflowAdvanceable == null) ? false : workflowAdvanceable;
			workflowAttachments = (workflowAttachments == null) ? false : workflowAttachments;
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

		public Builder withAttachmentsRuleActive(final Boolean attachmentsRuleActive) {
			this.attachmentsRuleActive = attachmentsRuleActive;
			return this;
		}

		public Builder withWorkflowRuleActive(final Boolean workflowRuleActive) {
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
	private final boolean notificationRuleActive;
	private final boolean attachmentsRuleActive;
	private final boolean workflowRuleActive;
	private final String workflowClassName;
	private final Map<String, String> workflowAttributes;
	private final boolean workflowAdvanceable;
	private final boolean workflowAttachments;

	private ReadEmailTask(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.active = builder.active;
		this.cronExpression = builder.cronExpression;
		this.emailAccount = builder.emailAccount;
		this.regexFromFilter = builder.regexFromFilter;
		this.regexSubjectFilter = builder.regexSubjectFilter;
		this.notificationRuleActive = builder.notificationRuleActive;
		this.attachmentsRuleActive = builder.attachmentsRuleActive;
		this.workflowRuleActive = builder.workflowRuleActive;
		this.workflowClassName = builder.workflowClassName;
		this.workflowAttributes = builder.workflowAttributes;
		this.workflowAdvanceable = builder.workflowAdvanceable;
		this.workflowAttachments = builder.workflowAttachments;
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

	public boolean isNotificationRuleActive() {
		return notificationRuleActive;
	}

	public String getRegexFromFilter() {
		return regexFromFilter;
	}

	public String getRegexSubjectFilter() {
		return regexSubjectFilter;
	}

	public boolean isAttachmentsRuleActive() {
		return attachmentsRuleActive;
	}

	public boolean isWorkflowRuleActive() {
		return workflowRuleActive;
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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}

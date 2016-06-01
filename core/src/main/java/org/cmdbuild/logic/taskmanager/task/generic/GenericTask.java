package org.cmdbuild.logic.taskmanager.task.generic;

import static java.lang.Boolean.FALSE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.logic.taskmanager.AbstractScheduledTask;
import org.cmdbuild.logic.taskmanager.TaskVisitor;
import org.joda.time.DateTime;

public class GenericTask extends AbstractScheduledTask {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<GenericTask> {

		private Long id;
		private String description;
		private Boolean active;
		private String cronExpression;
		private DateTime lastExecution;
		private Boolean emailActive;
		private String emailTemplate;
		private String emailAccount;

		private Builder() {
			// use factory method
		}

		@Override
		public GenericTask build() {
			validate();
			return new GenericTask(this);
		}

		private void validate() {
			active = defaultIfNull(active, FALSE);
			emailActive = defaultIfNull(emailActive, FALSE);
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withActiveStatus(final Boolean active) {
			this.active = active;
			return this;
		}

		public Builder withCronExpression(final String cronExpression) {
			this.cronExpression = cronExpression;
			return this;
		}

		public Builder withLastExecution(final DateTime lastExecution) {
			this.lastExecution = lastExecution;
			return this;
		}

		public Builder withEmailActive(final Boolean emailActive) {
			this.emailActive = emailActive;
			return this;
		}

		public Builder withEmailTemplate(final String emailTemplate) {
			this.emailTemplate = emailTemplate;
			return this;
		}

		public Builder withEmailAccount(final String emailAccount) {
			this.emailAccount = emailAccount;
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
	private final DateTime lastExecution;
	private final boolean emailActive;
	private final String emailTemplate;
	private final String emailAccount;

	private GenericTask(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.active = builder.active;
		this.cronExpression = builder.cronExpression;
		this.lastExecution = builder.lastExecution;
		this.emailActive = builder.emailActive;
		this.emailTemplate = builder.emailTemplate;
		this.emailAccount = builder.emailAccount;
	}

	@Override
	public void accept(final TaskVisitor visitor) {
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

	@Override
	public boolean isExecutable() {
		return true;
	}

	@Override
	public DateTime getLastExecution() {
		return lastExecution;
	}

	public boolean isEmailActive() {
		return emailActive;
	}

	public String getEmailTemplate() {
		return emailTemplate;
	}

	public String getEmailAccount() {
		return emailAccount;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof GenericTask)) {
			return false;
		}
		final GenericTask other = GenericTask.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.id, other.id) //
				.append(this.description, other.description) //
				.append(this.active, other.active) //
				.append(this.cronExpression, other.cronExpression) //
				.append(this.lastExecution, other.lastExecution) //
				.append(this.emailActive, other.emailActive) //
				.append(this.emailTemplate, other.emailTemplate) //
				.append(this.emailAccount, other.emailAccount) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(id) //
				.append(description) //
				.append(active) //
				.append(cronExpression) //
				.append(lastExecution) //
				.append(emailActive) //
				.append(emailTemplate) //
				.append(emailAccount) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}

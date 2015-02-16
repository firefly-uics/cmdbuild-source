package org.cmdbuild.servlets.json.email;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACCOUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVITY_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.BCC;
import static org.cmdbuild.servlets.json.CommunicationConstants.BODY;
import static org.cmdbuild.servlets.json.CommunicationConstants.CC;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FROM;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.KEEP_SYNCHRONIZATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFY_WITH;
import static org.cmdbuild.servlets.json.CommunicationConstants.NO_SUBJECT_PREFIX;
import static org.cmdbuild.servlets.json.CommunicationConstants.PROMPT_SYNCHRONIZATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.STATUS;
import static org.cmdbuild.servlets.json.CommunicationConstants.SUBJECT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPLATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPORARY;
import static org.cmdbuild.servlets.json.CommunicationConstants.TO;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.email.EmailStatus;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.AbstractJsonResponseSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class Email extends JSONBaseWithSpringContext {

	private static class JsonEmail extends AbstractJsonResponseSerializer implements EmailLogic.Email {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<JsonEmail> {

			private Long id;
			private String fromAddress;
			private String toAddresses;
			private String ccAddresses;
			private String bccAddresses;
			private String subject;
			private String content;
			private String notifyWith;
			private DateTime date;
			private EmailStatus status;
			private Long activityId;
			private boolean noSubjectPrefix;
			private String account;
			private boolean temporary;
			private String template;
			private boolean keepSynchronization;
			private boolean promptSynchronization;

			private Builder() {
				// use factory method
			}

			@Override
			public JsonEmail build() {
				return new JsonEmail(this);
			}

			public Builder with(final EmailLogic.Email email) {
				return this //
						.withId(email.getId()) //
						.withFromAddress(email.getFromAddress()) //
						.withToAddresses(email.getToAddresses()) //
						.withCcAddresses(email.getCcAddresses()) //
						.withBccAddresses(email.getCcAddresses()) //
						.withSubject(email.getSubject()) //
						.withContent(email.getContent()) //
						.withNotifyWith(email.getNotifyWith()) //
						.withStatus(email.getStatus()) //
						.withActivityId(email.getActivityId()) //
						.withNoSubjectPrefix(email.isNoSubjectPrefix()) //
						.withAccount(email.getAccount()) //
						.withTemporary(email.isTemporary()) //
						.withTemplate(email.getTemplate()) //
						.withKeepSynchronization(email.isKeepSynchronization()) //
						.withPromptSynchronization(email.isPromptSynchronization());
			}

			public Builder withId(final Long id) {
				this.id = id;
				return this;
			}

			public Builder withFromAddress(final String fromAddress) {
				this.fromAddress = fromAddress;
				return this;
			}

			public Builder withToAddresses(final String toAddresses) {
				this.toAddresses = toAddresses;
				return this;
			}

			public Builder withCcAddresses(final String ccAddresses) {
				this.ccAddresses = ccAddresses;
				return this;
			}

			public Builder withBccAddresses(final String bccAddresses) {
				this.bccAddresses = bccAddresses;
				return this;
			}

			public Builder withSubject(final String subject) {
				this.subject = subject;
				return this;
			}

			public Builder withContent(final String content) {
				this.content = content;
				return this;
			}

			public Builder withNotifyWith(final String notifyWith) {
				this.notifyWith = notifyWith;
				return this;
			}

			public Builder withStatus(final EmailStatus status) {
				this.status = status;
				return this;
			}

			public Builder withActivityId(final Long activityId) {
				this.activityId = activityId;
				return this;
			}

			public Builder withNoSubjectPrefix(final boolean noSubjectPrefix) {
				this.noSubjectPrefix = noSubjectPrefix;
				return this;
			}

			public Builder withAccount(final String account) {
				this.account = account;
				return this;
			}

			public Builder withTemporary(final boolean temporary) {
				this.temporary = temporary;
				return this;
			}

			public Builder withTemplate(final String template) {
				this.template = template;
				return this;
			}

			public Builder withKeepSynchronization(final boolean keepSynchronization) {
				this.keepSynchronization = keepSynchronization;
				return this;
			}

			public Builder withPromptSynchronization(final boolean promptSynchronization) {
				this.promptSynchronization = promptSynchronization;
				return this;
			}

			@Override
			public String toString() {
				return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final Long id;
		private final String fromAddress;
		private final String toAddresses;
		private final String ccAddresses;
		private final String bccAddresses;
		private final String subject;
		private final String content;
		private final String notifyWith;
		private final DateTime date;
		private final EmailStatus status;
		private final Long activityId;
		private final boolean noSubjectPrefix;
		private final String account;
		private final boolean temporary;
		private final String template;
		private final boolean keepSynchronization;
		private final boolean promptSynchronization;

		private JsonEmail(final Builder builder) {
			this.id = builder.id;
			this.fromAddress = builder.fromAddress;
			this.toAddresses = builder.toAddresses;
			this.ccAddresses = builder.ccAddresses;
			this.bccAddresses = builder.bccAddresses;
			this.subject = builder.subject;
			this.content = builder.content;
			this.notifyWith = builder.notifyWith;
			this.date = builder.date;
			this.status = builder.status;
			this.activityId = builder.activityId;
			this.noSubjectPrefix = builder.noSubjectPrefix;
			this.account = builder.account;
			this.temporary = builder.temporary;
			this.template = builder.template;
			this.keepSynchronization = builder.keepSynchronization;
			this.promptSynchronization = builder.promptSynchronization;
		}

		@Override
		@JsonProperty(ID)
		public Long getId() {
			return id;
		}

		@Override
		@JsonProperty(FROM)
		public String getFromAddress() {
			return fromAddress;
		}

		@Override
		@JsonProperty(TO)
		public String getToAddresses() {
			return toAddresses;
		}

		@Override
		@JsonProperty(CC)
		public String getCcAddresses() {
			return ccAddresses;
		}

		@Override
		@JsonProperty(BCC)
		public String getBccAddresses() {
			return bccAddresses;
		}

		@Override
		@JsonProperty(SUBJECT)
		public String getSubject() {
			return subject;
		}

		@Override
		@JsonProperty(BODY)
		public String getContent() {
			return content;
		}

		@Override
		@JsonProperty(DATE)
		public DateTime getDate() {
			return date;
		}

		@Override
		@JsonProperty(STATUS)
		public EmailStatus getStatus() {
			return status;
		}

		@Override
		@JsonProperty(ACTIVITY_ID)
		public Long getActivityId() {
			return activityId;
		}

		@Override
		@JsonProperty(NOTIFY_WITH)
		public String getNotifyWith() {
			return notifyWith;
		}

		@Override
		@JsonProperty(NO_SUBJECT_PREFIX)
		public boolean isNoSubjectPrefix() {
			return noSubjectPrefix;
		}

		@Override
		@JsonProperty(ACCOUNT)
		public String getAccount() {
			return account;
		}

		@Override
		@JsonProperty(TEMPORARY)
		public boolean isTemporary() {
			return temporary;
		}

		@Override
		@JsonProperty(TEMPLATE)
		public String getTemplate() {
			return template;
		}

		@Override
		@JsonProperty(KEEP_SYNCHRONIZATION)
		public boolean isKeepSynchronization() {
			return keepSynchronization;
		}

		@Override
		@JsonProperty(PROMPT_SYNCHRONIZATION)
		public boolean isPromptSynchronization() {
			return promptSynchronization;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}

			if (!(obj instanceof Email)) {
				return false;
			}

			final EmailLogic.Email other = EmailLogic.Email.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getId(), other.getId()) //
					.append(this.getFromAddress(), other.getFromAddress()) //
					.append(this.getToAddresses(), other.getToAddresses()) //
					.append(this.getCcAddresses(), other.getCcAddresses()) //
					.append(this.getBccAddresses(), other.getBccAddresses()) //
					.append(this.getSubject(), other.getSubject()) //
					.append(this.getContent(), other.getContent()) //
					.append(this.getDate(), other.getDate()) //
					.append(this.getStatus(), other.getStatus()) //
					.append(this.getActivityId(), other.getActivityId()) //
					.append(this.getNotifyWith(), other.getNotifyWith()) //
					.append(this.isNoSubjectPrefix(), other.isNoSubjectPrefix()) //
					.append(this.getAccount(), other.getAccount()) //
					.append(this.isTemporary(), other.isTemporary()) //
					.append(this.getTemplate(), other.getTemplate()) //
					.append(this.isKeepSynchronization(), other.isKeepSynchronization()) //
					.append(this.isPromptSynchronization(), other.isPromptSynchronization()) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(id) //
					.append(fromAddress) //
					.append(toAddresses) //
					.append(ccAddresses) //
					.append(bccAddresses) //
					.append(subject) //
					.append(content) //
					.append(date) //
					.append(status) //
					.append(activityId) //
					.append(notifyWith) //
					.append(noSubjectPrefix) //
					.append(account) //
					.append(temporary) //
					.append(template) //
					.append(keepSynchronization) //
					.append(promptSynchronization) //
					.toHashCode();
		}

		@Override
		public final String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE).toString();
		}

	}

	private static final Function<EmailLogic.Email, JsonEmail> TO_JSON_EMAIL = new Function<EmailLogic.Email, JsonEmail>() {

		@Override
		public JsonEmail apply(final EmailLogic.Email input) {
			return JsonEmail.newInstance() //
					.with(input) //
					.build();
		}

	};

	@JSONExported
	public JsonResponse create( //
			@Parameter(value = FROM, required = false) final String from, //
			@Parameter(TO) final String to, //
			@Parameter(value = CC, required = false) final String cc, //
			@Parameter(value = BCC, required = false) final String bcc, //
			@Parameter(SUBJECT) final String subject, //
			@Parameter(BODY) final String body, //
			@Parameter(value = NOTIFY_WITH, required = false) final String notifyWith, //
			@Parameter(value = ACTIVITY_ID, required = false) final Long activityId, //
			@Parameter(value = NO_SUBJECT_PREFIX, required = false) final boolean noSubjectPrefix, //
			@Parameter(value = ACCOUNT, required = false) final String account, //
			@Parameter(value = TEMPORARY, required = false) final boolean temporary, //
			@Parameter(value = TEMPLATE, required = false) final String template, //
			@Parameter(value = KEEP_SYNCHRONIZATION, required = false) final boolean keepSynchronization, //
			@Parameter(value = PROMPT_SYNCHRONIZATION, required = false) final boolean promptSynchronization //
	) {
		final Long id = emailLogic().create(JsonEmail.newInstance() //
				.withFromAddress(from) //
				.withToAddresses(to) //
				.withCcAddresses(cc) //
				.withBccAddresses(bcc) //
				.withSubject(subject) //
				.withContent(body) //
				.withNotifyWith(notifyWith) //
				.withStatus(EmailStatus.DRAFT) //
				.withActivityId(activityId) //
				.withNoSubjectPrefix(noSubjectPrefix) //
				.withAccount(account) //
				.withTemporary(temporary) //
				.withTemplate(template) //
				.withKeepSynchronization(keepSynchronization) //
				.withPromptSynchronization(promptSynchronization) //
				.build());
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse readAll( //
			@Parameter(ACTIVITY_ID) final Long activityId //
	) {
		final Iterable<EmailLogic.Email> emails = emailLogic().readAll(activityId);
		return JsonResponse.success(Iterators.transform(emails.iterator(), TO_JSON_EMAIL));
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(ID) final Long id, //
			@Parameter(TEMPORARY) final boolean temporary //
	) {
		final EmailLogic.Email read = emailLogic().read(JsonEmail.newInstance() //
				.withId(id) //
				.withTemporary(temporary) //
				.build());
		return JsonResponse.success(TO_JSON_EMAIL.apply(read));
	}

	@JSONExported
	public JsonResponse update( //
			@Parameter(ID) final Long id, //
			@Parameter(value = FROM, required = false) final String from, //
			@Parameter(TO) final String to, //
			@Parameter(value = CC, required = false) final String cc, //
			@Parameter(value = BCC, required = false) final String bcc, //
			@Parameter(SUBJECT) final String subject, //
			@Parameter(BODY) final String body, //
			@Parameter(value = NOTIFY_WITH, required = false) final String notifyWith, //
			@Parameter(value = ACTIVITY_ID, required = false) final Long activityId, //
			@Parameter(value = NO_SUBJECT_PREFIX, required = false) final boolean noSubjectPrefix, //
			@Parameter(value = ACCOUNT, required = false) final String account, //
			@Parameter(value = TEMPORARY, required = false) final boolean temporary, //
			@Parameter(value = TEMPLATE, required = false) final String template, //
			@Parameter(value = KEEP_SYNCHRONIZATION, required = false) final boolean keepSynchronization, //
			@Parameter(value = PROMPT_SYNCHRONIZATION, required = false) final boolean promptSynchronization //
	) {
		emailLogic().update(JsonEmail.newInstance() //
				.withId(id) //
				.withFromAddress(from) //
				.withToAddresses(to) //
				.withCcAddresses(cc) //
				.withBccAddresses(bcc) //
				.withSubject(subject) //
				.withContent(body) //
				.withNotifyWith(notifyWith) //
				.withStatus(EmailStatus.DRAFT) //
				.withActivityId(activityId) //
				.withNoSubjectPrefix(noSubjectPrefix) //
				.withAccount(account) //
				.withTemporary(temporary) //
				.withTemplate(template) //
				.withKeepSynchronization(keepSynchronization) //
				.withPromptSynchronization(promptSynchronization) //
				.build());
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse delete( //
			@Parameter(ID) final Long id, //
			@Parameter(TEMPORARY) final boolean temporary //
	) {
		emailLogic().delete(JsonEmail.newInstance() //
				.withId(id) //
				.withTemporary(temporary) //
				.build());
		return JsonResponse.success(id);
	}

}

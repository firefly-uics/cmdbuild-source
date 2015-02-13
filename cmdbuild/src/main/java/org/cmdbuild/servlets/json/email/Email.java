package org.cmdbuild.servlets.json.email;

import static org.cmdbuild.servlets.json.CommunicationConstants.ACCOUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVITY_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.BCC;
import static org.cmdbuild.servlets.json.CommunicationConstants.BODY;
import static org.cmdbuild.servlets.json.CommunicationConstants.CC;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FROM;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFY_WITH;
import static org.cmdbuild.servlets.json.CommunicationConstants.NO_SUBJECT_PREFIX;
import static org.cmdbuild.servlets.json.CommunicationConstants.STATUS;
import static org.cmdbuild.servlets.json.CommunicationConstants.SUBJECT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPLATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPORARY;
import static org.cmdbuild.servlets.json.CommunicationConstants.TO;

import org.cmdbuild.data.store.email.EmailStatus;
import org.cmdbuild.logic.email.DefaultEmailLogic.EmailImpl;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.AbstractJsonResponseSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class Email extends JSONBaseWithSpringContext {

	public static class JsonEmail extends AbstractJsonResponseSerializer {

		private final EmailLogic.Email delegate;

		public JsonEmail(final EmailLogic.Email delegate) {
			this.delegate = delegate;
		}

		@JsonProperty(ID)
		public Long getId() {
			return delegate.getId();
		}

		@JsonProperty(FROM)
		public String getFromAddress() {
			return delegate.getFromAddress();
		}

		@JsonProperty(TO)
		public String getToAddresses() {
			return delegate.getToAddresses();
		}

		@JsonProperty(CC)
		public String getCcAddresses() {
			return delegate.getCcAddresses();
		}

		@JsonProperty(BCC)
		public String getBccAddresses() {
			return delegate.getBccAddresses();
		}

		@JsonProperty(SUBJECT)
		public String getSubject() {
			return delegate.getSubject();
		}

		@JsonProperty(BODY)
		public String getContent() {
			return delegate.getContent();
		}

		@JsonProperty(DATE)
		public String getDate() {
			return formatDateTime(delegate.getDate());
		}

		@JsonProperty(STATUS)
		public String getStatus() {
			return delegate.getStatus().getLookupName();
		}

		@JsonProperty(ACTIVITY_ID)
		public Long getActivityId() {
			return delegate.getActivityId();
		}

		@JsonProperty(NOTIFY_WITH)
		public String getNotifyWith() {
			return delegate.getNotifyWith();
		}

		@JsonProperty(NO_SUBJECT_PREFIX)
		public boolean isNoSubjectPrefix() {
			return delegate.isNoSubjectPrefix();
		}

		@JsonProperty(ACCOUNT)
		public String getAccount() {
			return delegate.getAccount();
		}

		@JsonProperty(TEMPORARY)
		public boolean isTemporary() {
			return delegate.isTemporary();
		}

		@JsonProperty(TEMPLATE)
		public String getTemplate() {
			return delegate.getTemplate();
		}

	}

	private static final Function<EmailLogic.Email, JsonEmail> TO_JSON_EMAIL = new Function<EmailLogic.Email, JsonEmail>() {

		@Override
		public JsonEmail apply(final EmailLogic.Email input) {
			return new JsonEmail(input);
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
			@Parameter(value = TEMPLATE, required = false) final String template //
	) {
		final Long id = emailLogic().create(EmailImpl.newInstance() //
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
		final EmailLogic.Email read = emailLogic().read(EmailImpl.newInstance() //
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
			@Parameter(value = TEMPLATE, required = false) final String template //
	) {
		emailLogic().update(EmailImpl.newInstance() //
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
				.build());
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse delete( //
			@Parameter(ID) final Long id, //
			@Parameter(TEMPORARY) final boolean temporary //
	) {
		emailLogic().delete(EmailImpl.newInstance() //
				.withId(id) //
				.withTemporary(temporary) //
				.build());
		return JsonResponse.success(id);
	}

}

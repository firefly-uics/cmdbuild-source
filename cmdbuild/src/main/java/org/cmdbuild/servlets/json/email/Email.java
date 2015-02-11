package org.cmdbuild.servlets.json.email;

import static org.cmdbuild.servlets.json.CommunicationConstants.ACCOUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVITY_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.BCC;
import static org.cmdbuild.servlets.json.CommunicationConstants.BODY;
import static org.cmdbuild.servlets.json.CommunicationConstants.CC;
import static org.cmdbuild.servlets.json.CommunicationConstants.FROM;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFY_WITH;
import static org.cmdbuild.servlets.json.CommunicationConstants.NO_SUBJECT_PREFIX;
import static org.cmdbuild.servlets.json.CommunicationConstants.PROCESS_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.SUBJECT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPORARY;
import static org.cmdbuild.servlets.json.CommunicationConstants.TO;

import org.cmdbuild.data.store.email.EmailStatus;
import org.cmdbuild.logic.email.DefaultEmailLogic.EmailImpl;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonEmail;
import org.cmdbuild.servlets.utils.Parameter;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class Email extends JSONBaseWithSpringContext {

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
			@Parameter(value = TEMPORARY, required = false) final boolean temporary //
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
				.build());
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse readAll( //
			@Parameter(ACTIVITY_ID) final Long activityId //
	) {
		final Iterable<EmailLogic.Email> emails = emailLogic().getEmails(activityId);
		return JsonResponse.success(Iterators.transform(emails.iterator(), TO_JSON_EMAIL));
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(ID) final Long id, //
			@Parameter(TEMPORARY) final boolean temporary //
	) {
		final EmailLogic.Email read = emailLogic().read(EmailImpl.newInstance() //
				.withId(id) //
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
			@Parameter(value = TEMPORARY, required = false) final boolean temporary //
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
				.build());
		return JsonResponse.success(id);
	}

}

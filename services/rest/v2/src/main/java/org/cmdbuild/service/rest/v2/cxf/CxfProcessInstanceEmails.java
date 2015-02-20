package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.data.store.email.EmailConstants.ADDRESSES_SEPARATOR;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.draft;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.outgoing;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.received;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.sent;
import static org.cmdbuild.service.rest.v2.model.Models.newEmail;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import java.util.Collection;
import java.util.NoSuchElementException;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.logic.email.EmailImpl;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.v2.ProcessInstanceEmails;
import org.cmdbuild.service.rest.v2.cxf.serialization.DefaultConverter;
import org.cmdbuild.service.rest.v2.model.Email;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CxfProcessInstanceEmails implements ProcessInstanceEmails {

	private static final Function<EmailLogic.Email, Long> LOGIC_TO_LONG = new Function<EmailLogic.Email, Long>() {

		@Override
		public Long apply(final org.cmdbuild.logic.email.EmailLogic.Email input) {
			return input.getId();
		}

	};

	private static final Function<EmailLogic.Email, Email> LOGIC_TO_REST = new Function<EmailLogic.Email, Email>() {

		private final Collection<String> NO_ADDRESSES = emptyList();
		private final DateAttributeType DATE_ATTRIBUTE_TYPE = new DateAttributeType();

		@Override
		public Email apply(final org.cmdbuild.logic.email.EmailLogic.Email input) {
			return newEmail() //
					.withId(input.getId()) //
					.withFrom(input.getFromAddress()) //
					.withTo(splitAddresses(input.getToAddresses())) //
					.withCc(splitAddresses(input.getCcAddresses())) //
					.withBcc(splitAddresses(input.getBccAddresses())) //
					.withSubject(input.getSubject()) //
					.withBody(input.getContent()) //
					.withDate(dateAsString(input.getDate())) //
					.withStatus(statuses.inverse().get(input.getStatus())) //
					.withNotifyWith(input.getNotifyWith()) //
					.withNoSubjectPrefix(input.isNoSubjectPrefix()) //
					.withAccount(input.getAccount()) //
					.withTemporary(input.isTemporary()) //
					.withTemplate(input.getTemplate()) //
					.withKeepSynchronization(input.isKeepSynchronization()) //
					.withPromptSynchronization(input.isPromptSynchronization()) //
					.build();
		}

		private Collection<String> splitAddresses(final String addresses) {
			return isBlank(addresses) ? NO_ADDRESSES : Splitter.on(ADDRESSES_SEPARATOR) //
					.trimResults() //
					.splitToList(defaultString(addresses));
		}

		private String dateAsString(final DateTime input) {
			return (input == null) ? null : DefaultConverter.newInstance() //
					.build() //
					.toClient() //
					.convert(DATE_ATTRIBUTE_TYPE, input.toDate()) //
					.toString();

		}

	};

	private static final Function<Email, EmailLogic.Email> REST_TO_LOGIC = new Function<Email, EmailLogic.Email>() {

		@Override
		public EmailLogic.Email apply(final Email input) {
			return EmailImpl.newInstance() //
					.withId(input.getId()) //
					.withFromAddress(input.getFrom()) //
					.withToAddresses(joinAddresses(input.getTo())) //
					.withCcAddresses(joinAddresses(input.getCc())) //
					.withBccAddresses(joinAddresses(input.getBcc())) //
					.withSubject(input.getSubject()) //
					.withContent(input.getBody()) //
					.withStatus(statuses.get(input.getStatus())) //
					.withNotifyWith(input.getNotifyWith()) //
					.withNoSubjectPrefix(input.isNoSubjectPrefix()) //
					.withAccount(input.getAccount()) //
					.withTemporary(input.isTemporary()) //
					.withTemplate(input.getTemplate()) //
					.withKeepSynchronization(input.isKeepSynchronization()) //
					.withPromptSynchronization(input.isPromptSynchronization()) //
					.build();
		}

		private String joinAddresses(final Iterable<String> addresses) {
			return (addresses == null) ? null : Joiner.on(ADDRESSES_SEPARATOR) //
					.join(addresses);
		}

	};

	private static final BiMap<String, EmailLogic.Status> statuses = HashBiMap.create();

	static {
		statuses.put("received", received());
		statuses.put("draft", draft());
		statuses.put("outgoing", outgoing());
		statuses.put("sent", sent());
	}

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;
	private final EmailLogic emailLogic;

	public CxfProcessInstanceEmails(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic,
			final EmailLogic emailLogic) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
		this.emailLogic = emailLogic;
	}

	@Override
	public ResponseMultiple<String> statuses() {
		return newResponseMultiple(String.class) //
				.withElements(statuses.keySet()) //
				.withMetadata(newMetadata() //
						.withTotal(statuses.size()) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<Long> create(final String processId, final Long processInstanceId, final Email email) {
		checkPreconditions(processId, processInstanceId);
		final Long id = emailLogic.create(new EmailLogic.ForwardingEmail() {

			@Override
			protected org.cmdbuild.logic.email.EmailLogic.Email delegate() {
				return REST_TO_LOGIC.apply(email);
			}

			@Override
			public Long getActivityId() {
				return processInstanceId;
			}

		});
		return newResponseSingle(Long.class) //
				.withElement(id) //
				.build();
	}

	@Override
	public ResponseMultiple<Long> readAll(final String processId, final Long processInstanceId, final Integer limit,
			final Integer offset) {
		checkPreconditions(processId, processInstanceId);
		final Iterable<EmailLogic.Email> elements = emailLogic.readAll(processInstanceId);
		return newResponseMultiple(Long.class) //
				.withElements(from(elements) //
						.skip((offset == null) ? 0 : offset) //
						.limit((limit == null) ? MAX_VALUE : limit) //
						.transform(LOGIC_TO_LONG) //
				) //
				.withMetadata(newMetadata() //
						.withTotal(size(elements)) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<Email> read(final String processId, final Long processInstanceId, final Long emailId) {
		checkPreconditions(processId, processInstanceId);
		final EmailLogic.Email element = emailLogic.read(EmailImpl.newInstance() //
				.withId(emailId) //
				.build());
		return newResponseSingle(Email.class) //
				.withElement(LOGIC_TO_REST.apply(element)) //
				.build();
	}

	@Override
	public void update(final String processId, final Long processInstanceId, final Long emailId, final Email email) {
		checkPreconditions(processId, processInstanceId);
		emailLogic.update(new EmailLogic.ForwardingEmail() {

			@Override
			protected org.cmdbuild.logic.email.EmailLogic.Email delegate() {
				return REST_TO_LOGIC.apply(email);
			}

			@Override
			public Long getId() {
				return emailId;
			}

			@Override
			public Long getActivityId() {
				return processInstanceId;
			}

		});
	}

	@Override
	public void delete(final String processId, final Long processInstanceId, final Long emailId) {
		checkPreconditions(processId, processInstanceId);
		emailLogic.delete(EmailImpl.newInstance() //
				.withId(emailId) //
				.build());
	}

	private void checkPreconditions(final String classId, final Long cardId) {
		final CMClass targetClass = workflowLogic.findProcessClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		try {
			workflowLogic.getProcessInstance(classId, cardId);
		} catch (final NoSuchElementException e) {
			errorHandler.cardNotFound(cardId);
		}
	}

}

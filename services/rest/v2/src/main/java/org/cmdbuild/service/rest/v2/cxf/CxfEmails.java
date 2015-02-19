package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.data.store.email.EmailConstants.ADDRESSES_SEPARATOR;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.draft;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.outgoing;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.received;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.sent;
import static org.cmdbuild.service.rest.v2.constants.Serialization.REFERENCE;
import static org.cmdbuild.service.rest.v2.model.Models.newEmail;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import java.util.Collection;
import java.util.Date;

import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.logic.data.access.filter.json.JsonParser;
import org.cmdbuild.logic.data.access.filter.model.Attribute;
import org.cmdbuild.logic.data.access.filter.model.Element;
import org.cmdbuild.logic.data.access.filter.model.ElementVisitor;
import org.cmdbuild.logic.data.access.filter.model.EqualTo;
import org.cmdbuild.logic.data.access.filter.model.Filter;
import org.cmdbuild.logic.data.access.filter.model.ForwardingElementVisitor;
import org.cmdbuild.logic.data.access.filter.model.ForwardingPredicateVisitor;
import org.cmdbuild.logic.data.access.filter.model.Parser;
import org.cmdbuild.logic.data.access.filter.model.Predicate;
import org.cmdbuild.logic.data.access.filter.model.PredicateVisitor;
import org.cmdbuild.logic.email.EmailImpl;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.service.rest.v2.Emails;
import org.cmdbuild.service.rest.v2.cxf.serialization.DefaultConverter;
import org.cmdbuild.service.rest.v2.model.Email;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CxfEmails implements Emails {

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
					.withDate(dateAsString(input.getDate().toDate())) //
					.withStatus(statuses.inverse().get(input.getStatus())) //
					.withReference(input.getActivityId()) //
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

		private String dateAsString(final Date input) {
			return DefaultConverter.newInstance() //
					.build() //
					.toClient() //
					.convert(DATE_ATTRIBUTE_TYPE, input) //
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
					.withActivityId(input.getReference()) //
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

	private final EmailLogic emailLogic;

	public CxfEmails(final EmailLogic emailLogic) {
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
	public ResponseSingle<Long> create(final Email email) {
		final Long id = emailLogic.create(REST_TO_LOGIC.apply(email));
		return newResponseSingle(Long.class) //
				.withElement(id) //
				.build();
	}

	@Override
	public ResponseMultiple<Long> readAll(final String filter, final Integer limit, final Integer offset) {
		final Long processId;
		if (isNotBlank(filter)) {
			final Parser parser = new JsonParser(filter);
			final Filter filterModel = parser.parse();
			final Optional<Element> attribute = filterModel.attribute();
			if (attribute.isPresent()) {
				processId = new ForwardingElementVisitor() {

					private final ElementVisitor UNSUPPORTED = UnsupportedProxyFactory.of(ElementVisitor.class) //
							.create();

					private Long output;

					@Override
					protected ElementVisitor delegate() {
						return UNSUPPORTED;
					}

					public Long processId() {
						attribute.get().accept(this);
						return output;
					}

					@Override
					public void visit(final Attribute element) {
						output = new ForwardingPredicateVisitor() {

							private final PredicateVisitor UNSUPPORTED = UnsupportedProxyFactory.of(
									PredicateVisitor.class) //
									.create();
							private final String name = element.getName();
							private final Predicate predicate = element.getPredicate();

							private Long output;

							@Override
							protected PredicateVisitor delegate() {
								return UNSUPPORTED;
							}

							public Long processId() {
								predicate.accept(this);
								return output;
							}

							@Override
							public void visit(final EqualTo predicate) {
								if (REFERENCE.equals(name)) {
									output = Number.class.cast(predicate.getValue()).longValue();
								} else {
									super.visit(predicate);
								}
							}

						}.processId();
					}

				}.processId();
			} else {
				processId = null;
			}
		} else {
			processId = null;
		}
		final Iterable<EmailLogic.Email> elements = emailLogic.readAll(processId);
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
	public ResponseSingle<Email> read(final Long id) {
		final EmailLogic.Email element = emailLogic.read(EmailImpl.newInstance() //
				.withId(id).build());
		return newResponseSingle(Email.class) //
				.withElement(LOGIC_TO_REST.apply(element)) //
				.build();
	}

	@Override
	public void update(final Long id, final Email email) {
		emailLogic.update(new EmailLogic.ForwardingEmail() {

			@Override
			protected org.cmdbuild.logic.email.EmailLogic.Email delegate() {
				return REST_TO_LOGIC.apply(email);
			}

			@Override
			public Long getId() {
				return id;
			}

		});
	}

	@Override
	public void delete(final Long id) {
		emailLogic.delete(EmailImpl.newInstance() //
				.withId(id) //
				.build());
	}

}

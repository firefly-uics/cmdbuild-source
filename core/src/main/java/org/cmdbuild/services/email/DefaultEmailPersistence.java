package org.cmdbuild.services.email;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.data.converter.EmailConverter.EMAIL_CLASS_NAME;
import static org.cmdbuild.data.converter.EmailConverter.EMAIL_STATUS_ATTRIBUTE;
import static org.cmdbuild.data.converter.EmailConverter.PROCESS_ID_ATTRIBUTE;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.EmailConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.data.store.email.EmailTemplateStorableConverter;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.model.email.EmailTemplate;
import org.cmdbuild.services.email.SubjectParser.ParsedSubject;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class DefaultEmailPersistence implements EmailPersistence {

	private static final Logger logger = Log.PERSISTENCE;

	private final CMDataView dataView;
	private final LookupStore lookupStore;
	private final SubjectParser subjectParser;

	public DefaultEmailPersistence(final CMDataView dataView, final LookupStore lookupStore,
			final SubjectParser subjectParser) {
		this.dataView = dataView;
		this.lookupStore = lookupStore;
		this.subjectParser = subjectParser;
	}

	@Override
	public CMCard getProcessCardFrom(final String subject) throws IllegalArgumentException {
		logger.info("getting process card for subject '{}'", subject);
		final ParsedSubject parsed = subjectParser.parse(subject);
		if (!parsed.hasExpectedFormat()) {
			logger.warn("invalid subject format '{}'", subject);
			throw new IllegalArgumentException("invalid subject format");
		}
		final String activityClassName = parsed.getActivityClassName();
		final Integer activityId = parsed.getActivityId();
		return getCard(activityClassName, activityId);
	}

	@Override
	public CMCard getProcessCardFrom(final Email email) throws IllegalArgumentException {
		logger.info("getting process card for email with id '{}' and process id '{}'", email.getId(),
				email.getActivityId());
		// TODO externalize strings
		final String activityClassName = "Activity";
		final Integer activityId = email.getActivityId();
		return getCard(activityClassName, activityId);
	}

	private CMCard getCard(final String classname, final Integer id) {
		try {
			logger.debug("looking for card from class '{}' and with id '{}'", classname, id.longValue());
			final CMClass activityClass = dataView.findClass(classname);
			final CMQueryRow row = dataView
					.select(anyAttribute(activityClass))
					//
					.from(activityClass)
					//
					.where(condition(attribute(activityClass, activityClass.getKeyAttributeName()), eq(id.longValue()))) //
					.run() //
					.getOnlyRow();
			return row.getCard(activityClass);
		} catch (final NotFoundException e) {
			logger.error("activity card not found for classname '{}' and id '{}'", classname, id.longValue());
		}
		throw new IllegalArgumentException();
	}

	@Override
	public Iterable<EmailTemplate> getEmailTemplates() {
		logger.info("getting all email templates");
		final EmailTemplateStorableConverter converter = new EmailTemplateStorableConverter();
		final Store<EmailTemplate> store = new DataViewStore<EmailTemplate>(dataView, converter);
		return store.list();
	}

	@Override
	public Iterable<String> getEmailsForUser(final String user) {
		logger.info("getting all email addresses for user '{}'", user);
		// TODO externalize strings
		final CMClass userClass = dataView.findClass("User");
		Validate.notNull(userClass, "user class not visible");
		final CMCard card = dataView.select(anyAttribute(userClass)) //
				.from(userClass) //
				.where(condition(attribute(userClass, "Username"), eq(user))) //
				.run() //
				.getOnlyRow() //
				.getCard(userClass);
		final String email = card.get("Email", String.class);
		return StringUtils.isNotBlank(email) ? Arrays.asList(email) : Collections.<String> emptyList();
	}

	@Override
	public Iterable<String> getEmailsForGroup(final String group) {
		logger.info("getting all email addresses for group '{}'", group);
		// TODO externalize strings
		final CMClass roleClass = dataView.findClass("Role");
		Validate.notNull(roleClass, "role class not visible");
		final CMCard card = dataView.select(anyAttribute(roleClass)) //
				.from(roleClass) //
				.where(condition(attribute(roleClass, "Code"), eq(group))) //
				.run() //
				.getOnlyRow() //
				.getCard(roleClass);
		final String email = card.get("Email", String.class);
		return StringUtils.isNotBlank(email) ? Arrays.asList(email) : Collections.<String> emptyList();
	}

	@Override
	public Iterable<String> getEmailsForGroupUsers(final String group) {
		logger.info("getting all email addresses for users of group '{}'", group);
		// TODO externalize strings
		final List<String> emails = Lists.newArrayList();
		final CMClass userClass = dataView.findClass("User");
		Validate.notNull(userClass, "user class not visible");
		final CMClass roleClass = dataView.findClass("Role");
		Validate.notNull(roleClass, "role class not visible");
		final CMDomain userRoleDomain = dataView.findDomain("UserRole");
		Validate.notNull(userRoleDomain, "user-role domain not visible");
		final Iterable<CMQueryRow> rows = dataView.select(anyAttribute(roleClass), attribute(userClass, "Email")) //
				.from(roleClass) //
				.join(userClass, over(userRoleDomain)) //
				.where(condition(attribute(roleClass, "Code"), eq(group))) //
				.run();
		for (final CMQueryRow row : rows) {
			final CMCard card = row.getCard(userClass);
			final String email = card.get("Email", String.class);
			if (StringUtils.isNotBlank(email)) {
				emails.add(email);
			}
		}
		return emails;
	}

	@Override
	public Iterable<Email> getOutgoingEmails(final Long processId) {
		logger.info("getting all outgoing emails for process with id '{}'", processId);
		final List<Email> emails = Lists.newArrayList();
		final CMClass emailClass = dataView.findClass(EMAIL_CLASS_NAME);
		final CMQueryResult result = dataView.select(anyAttribute(emailClass)) //
				.from(emailClass) //
				.where(and( //
						condition(attribute(emailClass, PROCESS_ID_ATTRIBUTE), eq(processId)), //
						condition(attribute(emailClass, EMAIL_STATUS_ATTRIBUTE), //
								in(lookupId(EmailStatus.DRAFT), lookupId(EmailStatus.OUTGOING)))) //
				) //
				.run();
		final StorableConverter<Email> converter = emailConverter(processId);
		for (final CMQueryRow row : result) {
			final CMCard card = row.getCard(emailClass);
			final Email email = converter.convert(card);
			emails.add(email);
		}
		return emails;
	}

	private Integer lookupId(final EmailStatus emailStatus) {
		final LookupStore lookupStore = applicationContext().getBean(LookupStore.class);
		final Iterable<Lookup> elements = lookupStore.listForType(LookupType.newInstance() //
				.withName(EmailStatus.LOOKUP_TYPE) //
				.build());
		for (final Lookup lookup : elements) {
			if (emailStatus.getLookupName().equals(lookup.description)) {
				return lookup.getId().intValue();
			}
		}
		logger.error("lookup not found for type '{}' and description '{}'", EmailStatus.LOOKUP_TYPE, emailStatus);
		throw new NoSuchElementException();
	}

	@Override
	public Email create(final Email email) {
		logger.info("saving email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		final Store<Email> emailStore = emailStore(email.getActivityId());
		final Storable storable = emailStore.create(email);
		final Email storedEmail = emailStore.read(storable);
		return storedEmail;
	}

	@Override
	public Long save(final Email email) {
		logger.info("saving email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		final Integer processCardId = email.getActivityId();
		final Store<Email> emailStore = emailStore(processCardId);
		email.setActivityId(processCardId.intValue());
		final Long id;
		if (email.getId() == null) {
			logger.debug("creating new email");
			email.setStatus(EmailStatus.DRAFT);
			final Storable storable = emailStore.create(email);
			id = Long.valueOf(storable.getIdentifier());
		} else {
			logger.debug("updating existing email");
			emailStore.update(email);
			id = email.getId();
		}
		return id;
	}

	@Override
	public void delete(final Email email) {
		logger.info("deleting email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		emailStore(email.getActivityId()).delete(email);
	}

	@Override
	public Iterable<Email> getEmails(final Long processId) {
		logger.info("getting all emails for process' id '{}'", processId);
		return emailStore(processId).list();
	}

	private Store<Email> emailStore(final Integer processId) {
		logger.trace("getting email store for process' id '{}'", processId);
		return emailStore(processId.longValue());
	}

	private Store<Email> emailStore(final Long processId) {
		logger.trace("getting email store for process' id '{}'", processId);
		final StorableConverter<Email> converter = emailConverter(processId);
		return new DataViewStore<Email>(dataView, converter);
	}

	private EmailConverter emailConverter(final Long processId) {
		logger.trace("getting email converter for process' id '{}'", processId);
		return new EmailConverter(lookupStore, processId.intValue());
	}

}

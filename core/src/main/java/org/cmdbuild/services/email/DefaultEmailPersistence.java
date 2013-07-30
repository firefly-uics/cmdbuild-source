package org.cmdbuild.services.email;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.EmailConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.data.store.email.EmailTemplateStorableConverter;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Email;
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
	public Email create(final Email email) {
		logger.info("creating new e-mail card");
		final StorableConverter<Email> converter = new EmailConverter(lookupStore, email.getActivityId());
		final DataViewStore<Email> emailStore = new DataViewStore<Email>(dataView, converter);
		final Storable storable = emailStore.create(email);
		final Email storedEmail = emailStore.read(storable);
		return storedEmail;
	}

	@Override
	public CMCard getProcessCardFrom(final String subject) throws IllegalArgumentException {
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
		// TODO externalize strings
		final String activityClassName = "Activity";
		final Integer activityId = email.getActivityId();
		return getCard(activityClassName, activityId);
	}

	private CMCard getCard(final String classname, final Integer id) {
		try {
			logger.debug("looking for card from class '{}' and with id '{}'", classname, id.longValue());
			final CMClass activityClass = dataView.findClass(classname);
			final CMQueryRow row = dataView.select(anyAttribute(activityClass)) //
					.from(activityClass) //
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
		final EmailTemplateStorableConverter converter = new EmailTemplateStorableConverter();
		final Store<EmailTemplate> store = new DataViewStore<EmailTemplate>(dataView, converter);
		return store.list();
	}

	@Override
	public Iterable<String> getEmailsForUser(final String user) {
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

}

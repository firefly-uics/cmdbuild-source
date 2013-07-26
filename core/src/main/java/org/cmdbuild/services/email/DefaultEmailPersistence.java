package org.cmdbuild.services.email;

import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.EmailConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.services.email.SubjectParser.ParsedSubject;
import org.slf4j.Logger;

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
	public CMCard getActivityCardFrom(final String subject) {
		final ParsedSubject parsed = subjectParser.parse(subject);
		if (!parsed.hasExpectedFormat()) {
			logger.warn("invalid subject format '{}'", subject);
			throw new IllegalArgumentException("invalid subject format");
		}
		final String activityClassName = parsed.getActivityClassName();
		final Integer activityId = parsed.getActivityId();

		try {
			logger.debug("looking for card from class '{}' and with id '{}'", activityClassName, activityId.longValue());
			final CMClass activityClass = dataView.findClass(activityClassName);
			final CMQueryRow row = dataView.select(anyAttribute(activityClass)) //
					.from(activityClass) //
					.where(condition(attribute(activityClass, ID_ATTRIBUTE), eq(activityId.longValue()))) //
					.run() //
					.getOnlyRow();
			return row.getCard(activityClass);
		} catch (final NotFoundException e) {
			logger.error("activity card not found for classname '{}' and id '{}'", activityClassName,
					activityId.longValue());
		}
		throw new IllegalArgumentException();
	}
}

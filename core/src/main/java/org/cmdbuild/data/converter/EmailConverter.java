package org.cmdbuild.data.converter;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Map;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.Email;
import org.cmdbuild.model.Email.EmailStatus;
import org.cmdbuild.model.data.Card;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class EmailConverter implements StorableConverter<Email> {

	private static final String EMAIL_CLASS_NAME = "Email";
	private static final String LOOKUP_CLASS_NAME = "LookUp";
	private static final String PROCESS_ID_ATTRIBUTE = "Activity";
	private static final String EMAIL_STATUS_ATTRIBUTE = "EmailStatus";
	private static final String DATE_ATTRIBUTE = SystemAttributes.BeginDate.getDBName();
	private static final String FROM_ADDRESS_ATTRIBUTE = "FromAddress";
	private static final String TO_ADDRESSES_ATTRIBUTE = "ToAddresses";
	private static final String CC_ADDRESSES_ATTRIBUTE = "CcAddresses";
	private static final String SUBJECT_ATTRIBUTE = "Subject";
	private static final String CONTENT_ATTRIBUTE = "Content";
	private static final String IDENTIFIER_ATTRIBUTE = "Id";
	private final Integer processId;

	public EmailConverter(final Integer processId) {
		this.processId = processId;
	}

	@Override
	public String getClassName() {
		return EMAIL_CLASS_NAME;
	}

	@Override
	public String getGroupAttributeName() {
		return PROCESS_ID_ATTRIBUTE;
	}

	@Override
	public Object getGroupAttributeValue() {
		return processId;
	}

	@Override
	public String getIdentifierAttributeName() {
		return IDENTIFIER_ATTRIBUTE;
	}

	@Override
	public Storable storableOf(final CMCard card) {
		return new Storable() {

			@Override
			public String getIdentifier() {
				return card.get(getIdentifierAttributeName(), String.class);
			}

		};
	}

	@Override
	public Email convert(final CMCard card) {
		final Email email = new Email(card.getId());
		email.setFromAddress((card.get(FROM_ADDRESS_ATTRIBUTE) != null) ? (String) card.get(FROM_ADDRESS_ATTRIBUTE)
				: null);
		email.setCcAddresses((card.get(CC_ADDRESSES_ATTRIBUTE) != null) ? (String) card.get(CC_ADDRESSES_ATTRIBUTE)
				: null);
		email.setToAddresses((card.get(TO_ADDRESSES_ATTRIBUTE) != null) ? (String) card.get(TO_ADDRESSES_ATTRIBUTE)
				: null);
		email.setSubject((card.get(SUBJECT_ATTRIBUTE) != null) ? (String) card.get(SUBJECT_ATTRIBUTE) : null);
		email.setContent((card.get(CONTENT_ATTRIBUTE) != null) ? (String) card.get(CONTENT_ATTRIBUTE) : null);
		email.setDate((card.get(DATE_ATTRIBUTE) != null) ? (DateTime) card.get(DATE_ATTRIBUTE) : null);
		final Integer emailStatusLookupId = (Integer) card.get(EMAIL_STATUS_ATTRIBUTE);
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final Card lookupCard = dataAccessLogic.fetchCard(LOOKUP_CLASS_NAME, emailStatusLookupId.longValue());
		email.setStatus(getEmailStatusFromLookup(lookupCard));
		email.setActivityId((card.get(PROCESS_ID_ATTRIBUTE) != null) ? (Integer) card.get(PROCESS_ID_ATTRIBUTE) : null);
		return email;
	}

	private EmailStatus getEmailStatusFromLookup(final Card lookupCard) {
		final String lookupEmailStatus = lookupCard.getAttribute("Description", String.class);
		return EmailStatus.fromName(lookupEmailStatus);
	}

	@Override
	public Map<String, Object> getValues(final Email storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(CC_ADDRESSES_ATTRIBUTE, storable.getCcAddresses());
		values.put(TO_ADDRESSES_ATTRIBUTE, storable.getToAddresses());
		values.put(FROM_ADDRESS_ATTRIBUTE, storable.getFromAddress());
		values.put(SUBJECT_ATTRIBUTE, storable.getSubject());
		values.put(CONTENT_ATTRIBUTE, storable.getContent());
		values.put(DATE_ATTRIBUTE, storable.getDate());
		values.put(PROCESS_ID_ATTRIBUTE, storable.getActivityId());
		final EmailStatus emailStatus = storable.getStatus();
		final Integer emailLookupId = getEmailLookupIdFrom(emailStatus);
		values.put(EMAIL_STATUS_ATTRIBUTE, emailLookupId);
		values.put(IDENTIFIER_ATTRIBUTE, storable.getId());
		return values;
	}

	private Integer getEmailLookupIdFrom(final EmailStatus emailStatus) {
		final CMDataView view = TemporaryObjectsBeforeSpringDI.getSystemView();
		final CMClass lookupClass = view.findClass(LOOKUP_CLASS_NAME);
		final CMQueryRow row = view.select(anyAttribute(lookupClass)) //
				.from(lookupClass) //
				.where(condition(attribute(lookupClass, "Description"), eq(emailStatus.getLookupName()))) //
				.run().getOnlyRow();
		final CMCard lookupCard = row.getCard(lookupClass);
		return lookupCard.getId().intValue();
	}

	@Override
	public String getUser(final Email storable) {
		return SYSTEM_USER;
	}

}

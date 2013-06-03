package org.cmdbuild.data.converter;

import java.util.Map;
import java.util.NoSuchElementException;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.model.Email;
import org.cmdbuild.model.Email.EmailStatus;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class EmailConverter implements StorableConverter<Email> {

	private static final String EMAIL_CLASS_NAME = "Email";

	private static final String PROCESS_ID_ATTRIBUTE = "Activity";
	private static final String EMAIL_STATUS_ATTRIBUTE = "EmailStatus";
	private static final String DATE_ATTRIBUTE = SystemAttributes.BeginDate.getDBName();
	private static final String FROM_ADDRESS_ATTRIBUTE = "FromAddress";
	private static final String TO_ADDRESSES_ATTRIBUTE = "ToAddresses";
	private static final String CC_ADDRESSES_ATTRIBUTE = "CcAddresses";
	private static final String SUBJECT_ATTRIBUTE = "Subject";
	private static final String CONTENT_ATTRIBUTE = "Content";
	private static final String IDENTIFIER_ATTRIBUTE = "Id";

	private final LookupStore lookupStore;
	private final Integer processId;

	public EmailConverter(final LookupStore lookupStore, final Integer processId) {
		this.lookupStore = lookupStore;
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
		final Lookup lookup = lookupStore.read(Lookup.newInstance() //
				.withId(emailStatusLookupId.longValue()) //
				.build());
		email.setStatus(EmailStatus.fromName(lookup.description));
		email.setActivityId((card.get(PROCESS_ID_ATTRIBUTE) != null) ? (Integer) card.get(PROCESS_ID_ATTRIBUTE) : null);
		return email;
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
		values.put(EMAIL_STATUS_ATTRIBUTE, getEmailLookupIdFrom(storable.getStatus()));
		values.put(IDENTIFIER_ATTRIBUTE, storable.getId());
		return values;
	}

	private Long getEmailLookupIdFrom(final EmailStatus emailStatus) {
		final EmailStatus safeEmailStatus = (emailStatus == null) ? EmailStatus.NEW : emailStatus;
		for (final Lookup lookup : lookupStore.listForType(LookupType.newInstance() //
				.withName(EmailStatus.LOOKUP_TYPE) //
				.build())) {
			if (lookup.description.equals(safeEmailStatus.getLookupName())) {
				return lookup.getId();
			}
		}
		throw new NoSuchElementException();
	}

	@Override
	public String getUser(final Email storable) {
		return SYSTEM_USER;
	}

}

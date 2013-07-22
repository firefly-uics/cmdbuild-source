package org.cmdbuild.data.converter;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;

import java.util.Map;
import java.util.NoSuchElementException;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;

import com.google.common.collect.Maps;

public class EmailConverter implements StorableConverter<Email> {

	public static final String EMAIL_CLASS_NAME = "Email";

	public static final String IDENTIFIER_ATTRIBUTE = ID_ATTRIBUTE;

	public static final String PROCESS_ID_ATTRIBUTE = "Activity";
	public static final String EMAIL_STATUS_ATTRIBUTE = "EmailStatus";
	public static final String FROM_ADDRESS_ATTRIBUTE = "FromAddress";
	public static final String TO_ADDRESSES_ATTRIBUTE = "ToAddresses";
	public static final String CC_ADDRESSES_ATTRIBUTE = "CcAddresses";
	public static final String SUBJECT_ATTRIBUTE = "Subject";
	public static final String CONTENT_ATTRIBUTE = "Content";

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
		email.setFromAddress(defaultIfBlank(card.get(FROM_ADDRESS_ATTRIBUTE, String.class), null));
		email.setCcAddresses(defaultIfBlank(card.get(CC_ADDRESSES_ATTRIBUTE, String.class), null));
		email.setToAddresses(defaultIfBlank(card.get(TO_ADDRESSES_ATTRIBUTE, String.class), null));
		email.setSubject(defaultIfBlank(card.get(SUBJECT_ATTRIBUTE, String.class), null));
		email.setContent(defaultIfBlank(card.get(CONTENT_ATTRIBUTE, String.class), null));
		email.setDate((card.getBeginDate()));

		final Long emailStatusLookupId = card.get(EMAIL_STATUS_ATTRIBUTE, Long.class);
		final Lookup lookup = lookupStore.read(Lookup.newInstance() //
				.withId(emailStatusLookupId) //
				.build());
		email.setStatus(EmailStatus.fromName(lookup.description));
		email.setActivityId((card.get(PROCESS_ID_ATTRIBUTE) != null) ? card.get(PROCESS_ID_ATTRIBUTE, Long.class)
				.intValue() : null);
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
		values.put(PROCESS_ID_ATTRIBUTE, storable.getActivityId());
		if (storable.getStatus() != null) {
			values.put(EMAIL_STATUS_ATTRIBUTE, getEmailLookupIdFrom(storable.getStatus()));
		}
		values.put(IDENTIFIER_ATTRIBUTE, storable.getId());
		return values;
	}

	private Long getEmailLookupIdFrom(final EmailStatus emailStatus) {
		for (final Lookup lookup : lookupStore.listForType(LookupType.newInstance() //
				.withName(EmailStatus.LOOKUP_TYPE) //
				.build())) {
			if (lookup.description.equals(emailStatus.getLookupName())) {
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

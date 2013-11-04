package org.cmdbuild.data.converter;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import java.util.Map;
import java.util.NoSuchElementException;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;

import com.google.common.collect.Maps;

public class EmailConverter extends BaseStorableConverter<Email> {

	public static final String EMAIL_CLASS_NAME = "Email";

	public static final String PROCESS_ID_ATTRIBUTE = "Activity";
	public static final String EMAIL_STATUS_ATTRIBUTE = "EmailStatus";
	public static final String FROM_ADDRESS_ATTRIBUTE = "FromAddress";
	public static final String TO_ADDRESSES_ATTRIBUTE = "ToAddresses";
	public static final String CC_ADDRESSES_ATTRIBUTE = "CcAddresses";
	public static final String SUBJECT_ATTRIBUTE = "Subject";
	public static final String CONTENT_ATTRIBUTE = "Content";
	public static final String NOTIFY_WITH = "NotifyWith";

	private final LookupStore lookupStore;

	public EmailConverter(final LookupStore lookupStore) {
		this.lookupStore = lookupStore;
	}

	@Override
	public String getClassName() {
		return EMAIL_CLASS_NAME;
	}

	@Override
	public Email convert(final CMCard card) {
		final Email email = new Email(card.getId());
		email.setFromAddress(defaultIfBlank(card.get(FROM_ADDRESS_ATTRIBUTE, String.class), null));
		email.setCcAddresses(defaultIfBlank(card.get(CC_ADDRESSES_ATTRIBUTE, String.class), null));
		email.setToAddresses(defaultIfBlank(card.get(TO_ADDRESSES_ATTRIBUTE, String.class), null));
		email.setSubject(defaultIfBlank(card.get(SUBJECT_ATTRIBUTE, String.class), null));
		email.setContent(defaultIfBlank(card.get(CONTENT_ATTRIBUTE, String.class), null));
		email.setNotifyWith(defaultIfBlank(card.get(NOTIFY_WITH, String.class), null));
		email.setDate((card.getBeginDate()));

		final Long emailStatusLookupId = card.get(EMAIL_STATUS_ATTRIBUTE, IdAndDescription.class).getId();
		final Lookup lookup = lookupStore.read(Lookup.newInstance() //
				.withId(emailStatusLookupId) //
				.build());
		email.setStatus(EmailStatus.fromName(lookup.description));
		email.setActivityId((card.get(PROCESS_ID_ATTRIBUTE) != null) ? card.get(PROCESS_ID_ATTRIBUTE,
				IdAndDescription.class).getId() : null);
		return email;
	}

	@Override
	public Map<String, Object> getValues(final Email email) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(CC_ADDRESSES_ATTRIBUTE, email.getCcAddresses());
		values.put(TO_ADDRESSES_ATTRIBUTE, email.getToAddresses());
		values.put(FROM_ADDRESS_ATTRIBUTE, email.getFromAddress());
		values.put(SUBJECT_ATTRIBUTE, email.getSubject());
		values.put(CONTENT_ATTRIBUTE, email.getContent());
		values.put(PROCESS_ID_ATTRIBUTE, email.getActivityId());
		values.put(NOTIFY_WITH, email.getNotifyWith());
		if (email.getStatus() != null) {
			values.put(EMAIL_STATUS_ATTRIBUTE, getEmailLookupIdFrom(email.getStatus()));
		}
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

}

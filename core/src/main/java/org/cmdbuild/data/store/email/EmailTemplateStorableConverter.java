package org.cmdbuild.data.store.email;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.dao.BaseStorableConverter;

public class EmailTemplateStorableConverter extends BaseStorableConverter<EmailTemplate> {

	public static final String TABLE_NAME = "_EmailTemplate";

	public static final String NAME = Constants.CODE_ATTRIBUTE;
	public static final String DESCRIPTION = Constants.DESCRIPTION_ATTRIBUTE;
	public static final String FROM = "From";
	public static final String TO = "To";
	public static final String CC = "CC";
	public static final String BCC = "BCC";
	public static final String SUBJECT = "Subject";
	public static final String BODY = "Body";

	@Override
	public String getClassName() {
		return TABLE_NAME;
	}

	@Override
	public String getIdentifierAttributeName() {
		return NAME;
	}

	@Override
	public EmailTemplate convert(final CMCard card) {
		return EmailTemplate.newInstance() //
				.withId(card.getId()) //
				.withName(readStringAttribute(card, NAME)) //
				.withDescription(readStringAttribute(card, DESCRIPTION)) //
				.withFrom(readStringAttribute(card, FROM)) //
				.withTo(readStringAttribute(card, TO)) //
				.withCc(readStringAttribute(card, CC)) //
				.withBcc(readStringAttribute(card, BCC)) //
				.withSubject(readStringAttribute(card, SUBJECT)) //
				.withBody(readStringAttribute(card, BODY)) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final EmailTemplate emailTemplate) {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(NAME, emailTemplate.getName());
		values.put(DESCRIPTION, emailTemplate.getDescription());
		values.put(FROM, emailTemplate.getFrom());
		values.put(TO, emailTemplate.getTo());
		values.put(CC, emailTemplate.getCc());
		values.put(BCC, emailTemplate.getBcc());
		values.put(SUBJECT, emailTemplate.getSubject());
		values.put(BODY, emailTemplate.getBody());
		return values;
	}

}

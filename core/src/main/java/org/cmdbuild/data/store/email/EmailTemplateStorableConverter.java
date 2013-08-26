package org.cmdbuild.data.store.email;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.email.EmailTemplate;

public class EmailTemplateStorableConverter extends BaseStorableConverter<EmailTemplate> {

	private final String TABLE_NAME = "_EmailTemplate";

	private final String OWNER_ENTRY_TYPE = "Owner";
	private final String FROM = "From";
	private final String TO = "To";
	private final String CC = "CC";
	private final String BCC = "BCC";
	private final String SUBJECT = "Subject";
	private final String BODY = "Body";
	private final String NAME = "Code";
	private final String DESCRIPTION = "Description";

	@Override
	public String getClassName() {
		return TABLE_NAME;
	}

	@Override
	public EmailTemplate convert(final CMCard card) {
		final EmailTemplate emailTemplate = new EmailTemplate();
		emailTemplate.setOwnerId(readLongAttribute(card, OWNER_ENTRY_TYPE));
		emailTemplate.setFrom(readStringAttribute(card, FROM));
		emailTemplate.setTo(readStringAttribute(card, TO));
		emailTemplate.setCC(readStringAttribute(card, CC));
		emailTemplate.setBCC(readStringAttribute(card, BCC));
		emailTemplate.setSubject(readStringAttribute(card, SUBJECT));
		emailTemplate.setBody(readStringAttribute(card, BODY));
		emailTemplate.setName(readStringAttribute(card, NAME));
		emailTemplate.setDescription(readStringAttribute(card, DESCRIPTION));

		return emailTemplate;
	}

	@Override
	public Map<String, Object> getValues(final EmailTemplate emailTemplate) {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(OWNER_ENTRY_TYPE, emailTemplate.getOwnerClassId());
		values.put(FROM, emailTemplate.getFrom());
		values.put(TO, emailTemplate.getTo());
		values.put(CC, emailTemplate.getCC());
		values.put(BCC, emailTemplate.getBCC());
		values.put(SUBJECT, emailTemplate.getSubject());
		values.put(BODY, emailTemplate.getBody());
		values.put(NAME, emailTemplate.getName());
		values.put(DESCRIPTION, emailTemplate.getDescription());

		return values;
	}

	@Override
	public String getIdentifierAttributeName() {
		return NAME;
	}
}

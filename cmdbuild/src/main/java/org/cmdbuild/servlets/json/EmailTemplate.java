package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.ComunicationConstants.BCC;
import static org.cmdbuild.servlets.json.ComunicationConstants.BODY;
import static org.cmdbuild.servlets.json.ComunicationConstants.CC;
import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.SUBJECT;
import static org.cmdbuild.servlets.json.ComunicationConstants.TEMPLATES;
import static org.cmdbuild.servlets.json.ComunicationConstants.TEMPLATE_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.TO;

import org.cmdbuild.servlets.json.serializers.EmailTemplateSeializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EmailTemplate extends JSONBaseWithSpringContext {

	@JSONExported
	public JSONObject readTemplates ( //
			@Parameter(CLASS_NAME) final String ownerClassName //
		) throws JSONException {

		final JSONArray templates = EmailTemplateSeializer.toClient( //
				emailTemplateLogic().readForEntryTypeName(ownerClassName) //
			);

		return new JSONObject() {{
			put(TEMPLATES, templates);
		}};

	}

	@JSONExported
	public void createTemplate( //
			@Parameter(TEMPLATE_NAME) final String templateName, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(TO) final String to, //
			@Parameter(CC) final String cc, //
			@Parameter(BCC) final String bcc, //
			@Parameter(SUBJECT) final String subject, //
			@Parameter(BODY) final String body, //
			@Parameter(value = CLASS_ID, required = false) final Long ownerClassId //
		) throws JSONException {

		emailTemplateLogic().create( //
				emailTemplate(templateName, description, to, cc, bcc, subject, body, ownerClassId) //
			);

	}

	@JSONExported
	public void updateTemplate( //
			@Parameter(TEMPLATE_NAME) final String templateName, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(TO) final String to, //
			@Parameter(CC) final String cc, //
			@Parameter(BCC) final String bcc, //
			@Parameter(SUBJECT) final String subject, //
			@Parameter(BODY) final String body, //
			@Parameter(value = CLASS_ID, required = false) final Long ownerClassId //
		) throws JSONException {

		emailTemplateLogic().update( //
				emailTemplate(templateName, description, to, cc, bcc, subject, body, ownerClassId) //
			);
	}

	@JSONExported
	public void deleteTemplate( //
			@Parameter(TEMPLATE_NAME) final String templateName //
		) {

		emailTemplateLogic().delete(templateName);
	}

	/**
	 * @param templateName
	 * @param description
	 * @param to
	 * @param cc
	 * @param bcc
	 * @param subject
	 * @param body
	 */
	private org.cmdbuild.model.email.EmailTemplate emailTemplate( //
			final String templateName, //
			final String description, //
			final String to, //
			final String cc, //
			final String bcc, //
			final String subject, //
			final String body, //
			final Long ownerClassId //
		) {

		final org.cmdbuild.model.email.EmailTemplate emailTemplate = new org.cmdbuild.model.email.EmailTemplate();

		emailTemplate.setName(templateName);
		emailTemplate.setDescription(description);
		emailTemplate.setTo(to);
		emailTemplate.setCC(cc);
		emailTemplate.setBCC(bcc);
		emailTemplate.setSubject(subject);
		emailTemplate.setBody(body);
		final Long zero = new Long(0);
		if (zero.equals(ownerClassId)) {
			emailTemplate.setOwnerId(null);
		} else {
			emailTemplate.setOwnerId(ownerClassId);
		}


		return emailTemplate;
	}

}

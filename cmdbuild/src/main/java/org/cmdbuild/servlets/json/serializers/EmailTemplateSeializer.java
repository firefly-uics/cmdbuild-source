package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.servlets.json.ComunicationConstants.*;

import java.util.List;

import org.cmdbuild.model.email.EmailTemplate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EmailTemplateSeializer {

	public static JSONObject toClient(final EmailTemplate emailTemplate) throws JSONException {
		final JSONObject out = new JSONObject();
		out.put(TEMPLATE_NAME, emailTemplate.getName());
		out.put(FROM, emailTemplate.getFrom());
		out.put(TO, emailTemplate.getTo());
		out.put(CC, emailTemplate.getCC());
		out.put(BCC, emailTemplate.getBCC());
		out.put(SUBJECT, emailTemplate.getSubject());
		out.put(BODY, emailTemplate.getBody());
		out.put(CLASS_ID, emailTemplate.getOwnerClassId());
		out.put(DESCRIPTION, emailTemplate.getDescription());

		return out;
	}

	public static JSONArray toClient(final List<EmailTemplate> emailTemplates) throws JSONException {
		final JSONArray out = new JSONArray();
		for (final EmailTemplate emailTemplate: emailTemplates) {
			out.put(toClient(emailTemplate));
		}

		return out;
	}

}

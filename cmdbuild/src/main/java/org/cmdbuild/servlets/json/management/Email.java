package org.cmdbuild.servlets.json.management;

import java.util.UUID;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonEmail;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class Email extends JSONBaseWithSpringContext {

	@JSONExported
	public JsonResponse getEmailList(@Parameter("ProcessId") final Long processCardId) {
		final Iterable<org.cmdbuild.model.email.Email> emails = emailLogic().getEmails(processCardId);
		return JsonResponse.success(Iterators.transform(emails.iterator(),
				new Function<org.cmdbuild.model.email.Email, JsonEmail>() {
					@Override
					public JsonEmail apply(final org.cmdbuild.model.email.Email input) {
						return new JsonEmail(input);
					}
				}));
	}

	@JSONExported
	public JSONObject uploadAttachmentFromExistingEmail(
			@Parameter("emailId") final Long emailId,
			@Parameter("file") final FileItem file) throws JSONException {

		final JSONObject out = new JSONObject();
		if (file == null) {
			throw new RuntimeException("@@ Give me a file");
		} else {
			out.put("success", true);
			out.put("fileName", file.getName());
		}

		return out;
	}

	@JSONExported
	public JSONObject uploadAttachmentFromNewEmail(
			@Parameter(value = "uuid", required = false) final String uuid,
			@Parameter("file") final FileItem file) throws JSONException {

		final JSONObject out = new JSONObject();
		if (file == null) {
			throw new RuntimeException("@@ Give me a file");
		}

		out.put("success", true);
		out.put("fileName", file.getName());

		if (uuid == null) {
			out.put("uuid", UUID.randomUUID());
		} else {
			out.put("uuid", uuid);
		}

		return out;
	}

	@JSONExported
	public JSONObject deleteAttachmentFromExistingEmail(
			@Parameter("emailId") final Long emailId,
			@Parameter("fileName") final String fileName) throws JSONException {

		final JSONObject out = new JSONObject();
		out.put("success", true);

		return out;
	}

	@JSONExported
	public JSONObject deleteAttachmentFromNewEmail(
			@Parameter("uuid") final String uuid,
			@Parameter("fileName") final String fileName) throws JSONException {

		final JSONObject out = new JSONObject();
		out.put("success", true);

		return out;
	}
};

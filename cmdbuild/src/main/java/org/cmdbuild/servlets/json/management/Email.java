package org.cmdbuild.servlets.json.management;

import static org.cmdbuild.servlets.json.ComunicationConstants.EMAIL_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILE;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILE_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.PROCESS_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.SUCCESS;
import static org.cmdbuild.servlets.json.ComunicationConstants.TEMPORARY_ID;

import java.io.IOException;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonEmail;
import org.cmdbuild.servlets.utils.FileItemDataSource;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class Email extends JSONBaseWithSpringContext {

	private static final Function<org.cmdbuild.model.email.Email, JsonEmail> TO_JSON_EMAIL = new Function<org.cmdbuild.model.email.Email, JsonEmail>() {

		@Override
		public JsonEmail apply(final org.cmdbuild.model.email.Email input) {
			return new JsonEmail(input);
		}

	};

	@JSONExported
	public JsonResponse getEmailList( //
			@Parameter(PROCESS_ID) final Long processCardId //
	) {
		final Iterable<org.cmdbuild.model.email.Email> emails = emailLogic().getEmails(processCardId);
		return JsonResponse.success(Iterators.transform(emails.iterator(), TO_JSON_EMAIL));
	}

	@JSONExported
	public JSONObject uploadAttachmentFromExistingEmail( //
			@Parameter(EMAIL_ID) final Long emailId, //
			@Parameter(FILE) final FileItem file //
	) throws JSONException, IOException {
		final DataHandler dataHandler = new DataHandler(FileItemDataSource.of(file));
		emailLogic().uploadAttachment(emailId.toString(), false, dataHandler);

		final JSONObject out = new JSONObject();
		out.put(SUCCESS, true);
		out.put(FILE_NAME, file.getName());
		return out;
	}

	@JSONExported
	public JSONObject uploadAttachmentFromNewEmail( //
			@Parameter(value = TEMPORARY_ID, required = false) final String temporaryId, //
			@Parameter(FILE) final FileItem file //
	) throws JSONException, IOException {
		final DataHandler dataHandler = new DataHandler(FileItemDataSource.of(file));
		final String returnedIdentifier = emailLogic().uploadAttachment(temporaryId, true, dataHandler);

		final JSONObject out = new JSONObject();
		out.put(SUCCESS, true);
		out.put(FILE_NAME, file.getName());
		out.put(TEMPORARY_ID, returnedIdentifier);
		return out;
	}

	@JSONExported
	public JSONObject deleteAttachmentFromExistingEmail( //
			@Parameter(EMAIL_ID) final Long emailId, //
			@Parameter(FILE_NAME) final String fileName //
	) throws JSONException {
		emailLogic().deleteAttachment(emailId.toString(), false, fileName);

		final JSONObject out = new JSONObject();
		out.put(SUCCESS, true);
		return out;
	}

	@JSONExported
	public JSONObject deleteAttachmentFromNewEmail( //
			@Parameter(TEMPORARY_ID) final String temporaryId, //
			@Parameter(FILE_NAME) final String fileName //
	) throws JSONException {
		emailLogic().deleteAttachment(temporaryId, true, fileName);

		final JSONObject out = new JSONObject();
		out.put(SUCCESS, true);
		return out;
	}
};

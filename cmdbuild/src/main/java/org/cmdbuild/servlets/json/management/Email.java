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

import static org.cmdbuild.logic.email.EmailLogic.DeleteableAttachment.*;
import static org.cmdbuild.logic.email.EmailLogic.UploadableAttachment.*;

import org.cmdbuild.logic.email.EmailLogic.EmailWithAttachmentNames;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonEmail;
import org.cmdbuild.servlets.utils.FileItemDataSource;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class Email extends JSONBaseWithSpringContext {

	private static final Function<EmailWithAttachmentNames, JsonEmail> TO_JSON_EMAIL = new Function<EmailWithAttachmentNames, JsonEmail>() {

		@Override
		public JsonEmail apply(final EmailWithAttachmentNames input) {
			return new JsonEmail(input);
		}

	};

	@JSONExported
	public JsonResponse getEmailList( //
			@Parameter(PROCESS_ID) final Long processCardId //
	) {
		final Iterable<EmailWithAttachmentNames> emails = emailLogic().getEmails(processCardId);
		return JsonResponse.success(Iterators.transform(emails.iterator(), TO_JSON_EMAIL));
	}

	@JSONExported
	public JSONObject uploadAttachmentFromExistingEmail( //
			@Parameter(EMAIL_ID) final Long emailId, //
			@Parameter(FILE) final FileItem file //
	) throws JSONException, IOException {
		emailLogic().upload(uploadableAttachment() //
				.withIdentifier(emailId.toString()) //
				.withDataHandler(new DataHandler(FileItemDataSource.of(file))));

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
		final String returnedIdentifier = emailLogic().upload(uploadableAttachment() //
				.withIdentifier(temporaryId) //
				.withDataHandler(new DataHandler(FileItemDataSource.of(file))) //
				.withTemporaryStatus(true));

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
		emailLogic().delete(deleteableAttachment() //
				.withIdentifier(emailId.toString()) //
				.withFileName(fileName));

		final JSONObject out = new JSONObject();
		out.put(SUCCESS, true);
		return out;
	}

	@JSONExported
	public JSONObject deleteAttachmentFromNewEmail( //
			@Parameter(TEMPORARY_ID) final String temporaryId, //
			@Parameter(FILE_NAME) final String fileName //
	) throws JSONException {
		emailLogic().delete(deleteableAttachment() //
				.withIdentifier(temporaryId) //
				.withFileName(fileName) //
				.withTemporaryStatus(true));

		final JSONObject out = new JSONObject();
		out.put(SUCCESS, true);
		return out;
	}
};

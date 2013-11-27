package org.cmdbuild.servlets.json.management;

import static org.cmdbuild.logic.email.EmailLogic.DeleteableAttachment.deleteableAttachment;
import static org.cmdbuild.logic.email.EmailLogic.UploadableAttachment.uploadableAttachment;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTACHMENTS;
import static org.cmdbuild.servlets.json.ComunicationConstants.EMAIL_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILE;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILE_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.PROCESS_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.SUCCESS;
import static org.cmdbuild.servlets.json.ComunicationConstants.TEMPORARY_ID;

import java.io.IOException;
import java.util.UUID;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.logic.email.EmailLogic.EmailWithAttachmentNames;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonEmail;
import org.cmdbuild.servlets.utils.FileItemDataSource;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
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

	// TODO: implement the logic
	@JSONExported
	public JSONObject copyAttachmentsFromCardForNewEmail(
			@Parameter(value = TEMPORARY_ID, required = false) final String uuid,
			@Parameter(ATTACHMENTS) final String jsonAttachments //
	) throws JSONException {

		final JSONArray attachments = new JSONArray(jsonAttachments);
		final JSONObject out = new JSONObject();

		extractFileNames(attachments, out);

		if (uuid == null) {
			out.put(TEMPORARY_ID, UUID.randomUUID());
		} else {
			out.put(TEMPORARY_ID, uuid);
		}

		out.put(SUCCESS, true);
		return out;
	}

	// TODO: implement the logic
	@JSONExported
	public JSONObject copyAttachmentsFromCardForExistingEmail(
			@Parameter(value = EMAIL_ID, required = false) final Long emailId,
			@Parameter(ATTACHMENTS) final String jsonAttachments //
	) throws JSONException {

		final JSONArray attachments = new JSONArray(jsonAttachments);
		final JSONObject out = new JSONObject();

		extractFileNames(attachments, out);

		out.put(SUCCESS, true);
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

	/**
	 * @param attachments
	 *            an array of object like that {className: "...", cardId: "...",
	 *            fileName: "..."}
	 * @param out
	 * @throws JSONException
	 */
	private void extractFileNames(final JSONArray attachments, final JSONObject out) throws JSONException {
		final JSONArray fileNames = new JSONArray();
		for (int i = 0; i < attachments.length(); i++) {
			final JSONObject attachmentConf = attachments.getJSONObject(i);
			fileNames.put(attachmentConf.get(FILE_NAME));
		}
		out.put(ATTACHMENTS, fileNames);
	}

};

package org.cmdbuild.servlets.json.email;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.logic.email.EmailAttachmentsLogic.Copy.newCopy;
import static org.cmdbuild.logic.email.EmailAttachmentsLogic.Delete.newDelete;
import static org.cmdbuild.logic.email.EmailAttachmentsLogic.Upload.newUpload;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTACHMENTS;
import static org.cmdbuild.servlets.json.CommunicationConstants.EMAIL_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILE_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPORARY;

import java.io.IOException;
import java.util.Set;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.logic.email.EmailAttachmentsLogic;
import org.cmdbuild.logic.email.EmailAttachmentsLogic.CopiableAttachment;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.utils.FileItemDataSource;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;

public class Attachment extends JSONBaseWithSpringContext {

	private static class JsonAttachment {

		public String className;
		public Long cardId;
		public String fileName;

	}

	private static final Function<JsonAttachment, CopiableAttachment> TO_COPIABLE_ATTACHMENTS = new Function<JsonAttachment, EmailAttachmentsLogic.CopiableAttachment>() {

		@Override
		public CopiableAttachment apply(final JsonAttachment input) {
			// TODO Auto-generated method stub
			return CopiableAttachment.newCopy() //
					.withClassName(input.className) //
					.withCardId(input.cardId) //
					.withFileName(input.fileName) //
					.build();
		}

	};

	@JSONExported
	public JsonResponse upload( //
			@Parameter(value = EMAIL_ID) final Long emailId, //
			@Parameter(value = TEMPORARY, required = false) final boolean temporary, //
			@Parameter(value = FILE) final FileItem file //
	) throws JSONException, IOException {
		emailAttachmentsLogic().uploadAttachment(newUpload() //
				.withIdentifier(emailId.toString()) //
				.withTemporaryStatus(temporary) //
				.withDataHandler(dataHandlerOf(file)) //
				.build());
		return JsonResponse.success(file.getName());
	}

	private DataHandler dataHandlerOf(final FileItem file) {
		return new DataHandler(FileItemDataSource.of(file));
	}

	@JSONExported
	public JsonResponse delete( //
			@Parameter(value = EMAIL_ID) final Long emailId, //
			@Parameter(value = TEMPORARY, required = false) final boolean temporary, //
			@Parameter(value = FILE_NAME) final String fileName //
	) {
		emailAttachmentsLogic().deleteAttachment(newDelete() //
				.withIdentifier(emailId.toString()) //
				.withTemporaryStatus(temporary) //
				.withFileName(fileName) //
				.build());
		return JsonResponse.success(null);
	}

	@JSONExported
	public JsonResponse copy(
			//
			@Parameter(value = EMAIL_ID, required = false) final String emailId,
			@Parameter(value = TEMPORARY, required = false) final boolean temporary, //
			@Parameter(ATTACHMENTS) final String jsonAttachments //
	) throws JSONException, JsonParseException, JsonMappingException, IOException {
		final Iterable<JsonAttachment> attachments = mapJsonAttachmentsFromJsonArray(jsonAttachments);
		emailAttachmentsLogic().copyAttachments(newCopy() //
				.withIdentifier(emailId.toString()) //
				.withTemporaryStatus(temporary) //
				.withAllAttachments(from(attachments) //
						.transform(TO_COPIABLE_ATTACHMENTS)) //
				.build());
		return JsonResponse.success(null);
	}

	private Iterable<JsonAttachment> mapJsonAttachmentsFromJsonArray(final String content) throws JsonParseException,
			JsonMappingException, IOException {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue( //
				content, //
				mapper.getTypeFactory() //
						.constructCollectionType(Set.class, JsonAttachment.class));
	}

};

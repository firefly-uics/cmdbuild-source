package org.cmdbuild.servlets.json.email;

import org.cmdbuild.logic.email.EmailAttachmentsLogic;
import org.cmdbuild.logic.email.EmailAttachmentsLogic.*;

import static com.google.common.collect.FluentIterable.*;
import static org.cmdbuild.servlets.json.CommunicationConstants.CARD_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.EMAIL_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILE_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPORARY;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.logic.email.AttachmentImpl;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.utils.FileItemDataSource;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Function;

public class Attachment extends JSONBaseWithSpringContext {

	private static class JsonAttachment extends ForwardingAttachment {

		private EmailAttachmentsLogic.Attachment delegate;

		public JsonAttachment(EmailAttachmentsLogic.Attachment delegate) {
			this.delegate = delegate;
		}

		@Override
		protected EmailAttachmentsLogic.Attachment delegate() {
			return delegate;
		}

		@JsonProperty(CLASS_NAME)
		@Override
		public String getClassName() {
			return super.getClassName();
		}

		@JsonProperty(CARD_ID)
		@Override
		public Long getCardId() {
			return super.getCardId();
		}

		@JsonProperty(FILE_NAME)
		@Override
		public String getFileName() {
			return super.getFileName();
		}

	}

	@JSONExported
	public JsonResponse upload( //
			@Parameter(value = EMAIL_ID) final Long emailId, //
			@Parameter(value = TEMPORARY, required = false) final boolean temporary, //
			@Parameter(value = FILE) final FileItem file //
	) throws Exception {
		emailAttachmentsLogic().upload(emailId, temporary, dataHandlerOf(file));
		return JsonResponse.success(file.getName());
	}

	private DataHandler dataHandlerOf(final FileItem file) {
		return new DataHandler(FileItemDataSource.of(file));
	}

	@JSONExported
	public JsonResponse copy( //
			@Parameter(value = EMAIL_ID) final Long emailId, //
			@Parameter(value = TEMPORARY, required = false) final boolean temporary, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARD_ID) final Long cardId, //
			@Parameter(value = FILE_NAME) final String fileName //
	) throws Exception {
		emailAttachmentsLogic().copy(emailId, temporary, AttachmentImpl.newInstance() //
				.withClassName(className) //
				.withCardId(cardId) //
				.withFileName(fileName) //
				.build());
		return JsonResponse.success(null);
	}

	@JSONExported
	public JsonResponse readAll( //
			@Parameter(value = EMAIL_ID) final Long emailId, //
			@Parameter(value = TEMPORARY, required = false) final boolean temporary //
	) throws Exception {
		final Iterable<EmailAttachmentsLogic.Attachment> attachments = emailAttachmentsLogic().readAll(emailId,
				temporary);
		return JsonResponse.success(from(attachments) //
				.transform(new Function<EmailAttachmentsLogic.Attachment, JsonAttachment>() {

					@Override
					public JsonAttachment apply(final EmailAttachmentsLogic.Attachment input) {
						return new JsonAttachment(input);
					}

				}).toList());
	}

	@JSONExported
	public JsonResponse delete( //
			@Parameter(value = EMAIL_ID) final Long emailId, //
			@Parameter(value = TEMPORARY, required = false) final boolean temporary, //
			@Parameter(value = FILE_NAME) final String fileName //
	) {
		emailAttachmentsLogic().delete(emailId, temporary, fileName);
		return JsonResponse.success(null);
	}

};

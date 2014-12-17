package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.io.BaseEncoding.base64;
import static org.cmdbuild.service.rest.model.Models.newAttachment;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;

import javax.activation.DataHandler;

import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.base.Function;
import com.google.common.collect.ForwardingObject;
import com.google.common.io.BaseEncoding;

public class TranslatingAllInOneCardAttachments extends ForwardingObject implements AllInOneCardAttachments {

	private final Function<Attachment, Attachment> attachmentWithBase64Id = new Function<Attachment, Attachment>() {

		@Override
		public Attachment apply(final Attachment input) {
			return newAttachment(input) //
					.withId(plainToBase64(input.getId())) //
					.build();
		}

	};

	private final AllInOneCardAttachments delegate;
	private final BaseEncoding base64;

	public TranslatingAllInOneCardAttachments(final AllInOneCardAttachments delegate) {
		this.delegate = delegate;
		this.base64 = base64();
	}

	@Override
	protected AllInOneCardAttachments delegate() {
		return delegate;
	}

	private String plainToBase64(final String string) {
		return base64.encode(string.getBytes());
	}

	private String base64ToPlain(final String string) {
		return new String(base64.decode(string));
	}

	@Override
	public ResponseSingle<String> create(final String classId, final Long cardId, final Attachment attachment,
			final DataHandler dataHandler) {
		final ResponseSingle<String> response = delegate().create(classId, cardId, attachment, dataHandler);
		return newResponseSingle(String.class) //
				.withElement(plainToBase64(response.getElement())) //
				.build();
	}

	@Override
	public void update(final String classId, final Long cardId, final String attachmentId, final Attachment attachment,
			final DataHandler dataHandler) {
		delegate().update(classId, cardId, base64ToPlain(attachmentId), attachment, dataHandler);
	}

	@Override
	public ResponseMultiple<Attachment> read(final String classId, final Long cardId) {
		final ResponseMultiple<Attachment> response = delegate().read(classId, cardId);
		return newResponseMultiple(Attachment.class) //
				.withElements(from(response.getElements()) //
						.transform(attachmentWithBase64Id)) //
				.withMetadata(response.getMetadata()) //
				.build();
	}

	@Override
	public ResponseSingle<Attachment> read(final String classId, final Long cardId, final String attachmentId) {
		final ResponseSingle<Attachment> response = delegate().read(classId, cardId, base64ToPlain(attachmentId));
		return newResponseSingle(Attachment.class) //
				.withElement(attachmentWithBase64Id.apply((response.getElement()))) //
				.build();
	}

	@Override
	public DataHandler download(final String classId, final Long cardId, final String attachmentId) {
		return delegate().download(classId, cardId, base64ToPlain(attachmentId));
	}

	@Override
	public void delete(final String classId, final Long cardId, final String attachmentId) {
		delegate().delete(classId, cardId, base64ToPlain(attachmentId));
	}

}

package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.io.BaseEncoding.base64Url;
import static org.cmdbuild.service.rest.model.Models.newAttachment;

import javax.activation.DataHandler;

import org.cmdbuild.service.rest.model.Attachment;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ForwardingObject;
import com.google.common.io.BaseEncoding;

public class TranslatingAttachmentsHelper extends ForwardingObject implements AttachmentsHelper {

	private final Function<Attachment, Attachment> attachmentWithEncodedId = new Function<Attachment, Attachment>() {

		@Override
		public Attachment apply(final Attachment input) {
			return newAttachment(input) //
					.withId(encode(input.getId())) //
					.build();
		}

	};

	private final AttachmentsHelper delegate;
	private final BaseEncoding encoding;

	public TranslatingAttachmentsHelper(final AttachmentsHelper delegate) {
		this.delegate = delegate;
		this.encoding = base64Url().omitPadding();
	}

	@Override
	protected AttachmentsHelper delegate() {
		return delegate;
	}

	private String encode(final String string) {
		return encoding.encode(string.getBytes());
	}

	private String decode(final String string) {
		return new String(encoding.decode(string));
	}

	@Override
	public String create(final String classId, final Long cardId, final String attachmentName,
			final Attachment attachment, final DataHandler dataHandler) throws Exception {
		return encode(delegate.create(classId, cardId, attachmentName, attachment, dataHandler));
	}

	@Override
	public void update(final String classId, final Long cardId, final String attachmentId, final Attachment attachment,
			final DataHandler dataHandler) throws Exception {
		delegate().update(classId, cardId, decode(attachmentId), attachment, dataHandler);
	}

	@Override
	public Iterable<Attachment> search(final String classId, final Long cardId) {
		return from(delegate().search(classId, cardId)) //
				.transform(attachmentWithEncodedId);
	}

	@Override
	public Optional<Attachment> search(final String classId, final Long cardId, final String attachmentId) {
		final Optional<Attachment> response = delegate().search(classId, cardId, decode(attachmentId));
		return response.isPresent() ? Optional.of(attachmentWithEncodedId.apply(response.get())) : response;
	}

	@Override
	public DataHandler download(final String classId, final Long cardId, final String attachmentId) {
		return delegate().download(classId, cardId, decode(attachmentId));
	}

	@Override
	public void delete(final String classId, final Long cardId, final String attachmentId) {
		delegate().delete(classId, cardId, decode(attachmentId));
	}

}

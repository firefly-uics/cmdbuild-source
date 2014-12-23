package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.io.BaseEncoding.base64Url;
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

public class TranslatingAllInOneProcessInstanceAttachments extends ForwardingObject implements
		AllInOneProcessInstanceAttachments {

	private final Function<Attachment, Attachment> attachmentWithbase64UrlId = new Function<Attachment, Attachment>() {

		@Override
		public Attachment apply(final Attachment input) {
			return newAttachment(input) //
					.withId(plainTobase64Url(input.getId())) //
					.build();
		}

	};

	private final AllInOneProcessInstanceAttachments delegate;
	private final BaseEncoding base64Url;

	public TranslatingAllInOneProcessInstanceAttachments(final AllInOneProcessInstanceAttachments delegate) {
		this.delegate = delegate;
		this.base64Url = base64Url().omitPadding();
	}

	@Override
	protected AllInOneProcessInstanceAttachments delegate() {
		return delegate;
	}

	private String plainTobase64Url(final String string) {
		return base64Url.encode(string.getBytes());
	}

	private String base64UrlToPlain(final String string) {
		return new String(base64Url.decode(string));
	}

	@Override
	public ResponseSingle<String> create(final String processId, final Long instanceId, final Attachment attachment,
			final DataHandler dataHandler) {
		final ResponseSingle<String> response = delegate().create(processId, instanceId, attachment, dataHandler);
		return newResponseSingle(String.class) //
				.withElement(plainTobase64Url(response.getElement())) //
				.build();
	}

	@Override
	public void update(final String processId, final Long instanceId, final String attachmentId,
			final Attachment attachment, final DataHandler dataHandler) {
		delegate().update(processId, instanceId, base64UrlToPlain(attachmentId), attachment, dataHandler);
	}

	@Override
	public ResponseMultiple<Attachment> read(final String processId, final Long processInstanceId) {
		final ResponseMultiple<Attachment> response = delegate().read(processId, processInstanceId);
		return newResponseMultiple(Attachment.class) //
				.withElements(from(response.getElements()) //
						.transform(attachmentWithbase64UrlId)) //
				.withMetadata(response.getMetadata()) //
				.build();
	}

	@Override
	public ResponseSingle<Attachment> read(final String processId, final Long processInstanceId,
			final String attachmentId) {
		final ResponseSingle<Attachment> response = delegate().read(processId, processInstanceId,
				base64UrlToPlain(attachmentId));
		return newResponseSingle(Attachment.class) //
				.withElement(attachmentWithbase64UrlId.apply((response.getElement()))) //
				.build();
	}

	@Override
	public DataHandler download(final String processId, final Long processInstanceId, final String attachmentId) {
		return delegate().download(processId, processInstanceId, base64UrlToPlain(attachmentId));
	}

	@Override
	public void delete(final String processId, final Long processInstanceId, final String attachmentId) {
		delegate().delete(processId, processInstanceId, base64UrlToPlain(attachmentId));
	}

}

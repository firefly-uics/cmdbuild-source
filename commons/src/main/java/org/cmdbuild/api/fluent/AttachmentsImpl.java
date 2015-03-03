package org.cmdbuild.api.fluent;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

class AttachmentsImpl implements Attachments {

	private static final String[] NO_NAMES = new String[] {};
	private static final Attachment[] NO_ATTACHMENTS = new Attachment[] {};

	private final FluentApiExecutor executor;
	private final CardDescriptor descriptor;

	AttachmentsImpl(final FluentApiExecutor executor, final CardDescriptor descriptor) {
		this.executor = executor;
		this.descriptor = descriptor;
	}

	@Override
	public Iterable<AttachmentDescriptor> fetch() {
		return executor.fetchAttachments(descriptor);
	}

	@Override
	public SelectedAttachments selectByName(final String... names) {
		return new SelectedAttachmentsImpl(executor, descriptor, newArrayList(defaultIfNull(names, NO_NAMES)));
	}

	@Override
	public SelectedAttachments selectAll() {
		return new SelectedAttachmentsImpl(executor, descriptor);
	}

	@Override
	public void upload(final Attachment... attachments) {
		executor.upload(descriptor, newArrayList(defaultIfNull(attachments, NO_ATTACHMENTS)));
	}

}

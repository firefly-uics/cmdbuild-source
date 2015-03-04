package org.cmdbuild.api.fluent;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;

import com.google.common.base.Predicate;

class SelectedAttachmentsImpl implements SelectedAttachments {

	private final FluentApiExecutor executor;
	private final CardDescriptor descriptor;
	private final Predicate<AttachmentDescriptor> predicate;

	SelectedAttachmentsImpl(final FluentApiExecutor executor, final CardDescriptor descriptor,
			final Predicate<AttachmentDescriptor> predicate) {
		this.executor = executor;
		this.descriptor = descriptor;
		this.predicate = predicate;
	}

	@Override
	public Iterable<AttachmentDescriptor> selected() {
		return from(executor.fetchAttachments(descriptor)) //
				.filter(predicate);
	}

	@Override
	public Iterable<Attachment> download() {
		final Collection<Attachment> downloaded = newArrayList();
		for (final AttachmentDescriptor descriptor : selected()) {
			downloaded.add(executor.download(this.descriptor, descriptor));
		}
		return downloaded;
	}

	@Override
	public void copyTo(final CardDescriptor destination) {
		// TODO Auto-generated method stub
	}

	@Override
	public void moveTo(final CardDescriptor destination) {
		// TODO Auto-generated method stub
	}

	@Override
	public void delete() {
		executor.delete(this.descriptor, selected());
	}

}

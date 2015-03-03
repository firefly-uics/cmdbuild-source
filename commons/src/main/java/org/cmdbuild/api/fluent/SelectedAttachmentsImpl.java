package org.cmdbuild.api.fluent;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;

import com.google.common.base.Predicate;

class SelectedAttachmentsImpl implements SelectedAttachments {

	private static class NamePredicate implements Predicate<AttachmentDescriptor> {

		private final Collection<String> allowed;

		public NamePredicate(final Iterable<String> names) {
			allowed = newArrayList(names);
		}

		@Override
		public boolean apply(final AttachmentDescriptor input) {
			return allowed.contains(input.getName());
		}

	}

	private final FluentApiExecutor executor;
	private final CardDescriptor descriptor;
	private final Predicate<AttachmentDescriptor> predicate;

	SelectedAttachmentsImpl(final FluentApiExecutor executor, final CardDescriptor descriptor) {
		this.executor = executor;
		this.descriptor = descriptor;
		this.predicate = alwaysTrue();
	}

	SelectedAttachmentsImpl(final FluentApiExecutor executor, final CardDescriptor descriptor,
			final Iterable<String> names) {
		this.executor = executor;
		this.descriptor = descriptor;
		this.predicate = new NamePredicate(names);
	}

	@Override
	public Iterable<AttachmentDescriptor> selected() {
		return from(executor.fetchAttachments(descriptor)) //
				.filter(predicate);
	}

	@Override
	public Iterable<Attachment> download() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void copyTo(final CardDescriptor destination) {
		// TODO Auto-generated method stub

	}

	@Override
	public void copyTo(final ProcessInstanceDescriptor destination) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveTo(final CardDescriptor destination) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveTo(final ProcessInstanceDescriptor destination) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete() {
		for (final AttachmentDescriptor descriptor : selected()) {
			executor.delete(this.descriptor, descriptor);
		}
	}

}

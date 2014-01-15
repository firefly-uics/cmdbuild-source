package org.cmdbuild.logic.email.rules;

import static org.cmdbuild.data.store.email.EmailConstants.EMAIL_CLASS_NAME;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailCallbackHandler.RuleAction;
import org.slf4j.Logger;

public class DownloadAttachments implements Rule {

	private static final Logger logger = Logic.logger;

	private final AttachmentStoreFactory attachmentStoreFactory;

	public DownloadAttachments( //
			final AttachmentStoreFactory attachmentStoreFactory //
	) {
		this.attachmentStoreFactory = attachmentStoreFactory;
	}

	@Override
	public boolean applies(final Email email) {
		return true;
	}

	@Override
	public Email adapt(final Email email) {
		return email;
	}

	@Override
	public RuleAction action(final Email email) {
		return new RuleAction() {

			@Override
			public void execute() {
				storeAttachmentsOf(email);
			}

			private void storeAttachmentsOf(final Email email) {
				logger.info("storing attachments for email {}", email);
				final AttachmentStore attachmentStore = attachmentStoreFactory.create(EMAIL_CLASS_NAME, email.getId());
				attachmentStore.store(email.getAttachments());
			}

		};
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.toString();
	}

}

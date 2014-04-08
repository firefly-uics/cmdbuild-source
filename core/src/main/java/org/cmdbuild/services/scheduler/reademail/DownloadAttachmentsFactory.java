package org.cmdbuild.services.scheduler.reademail;

import org.apache.commons.lang3.Validate;

public class DownloadAttachmentsFactory implements RuleFactory<DownloadAttachments> {

	private final AttachmentStoreFactory attachmentStoreFactory;

	public DownloadAttachmentsFactory( //
			final AttachmentStoreFactory attachmentStoreFactory //
	) {
		this.attachmentStoreFactory = attachmentStoreFactory;
	}

	@Override
	public DownloadAttachments create() {
		Validate.notNull(attachmentStoreFactory, "null attachment store factory");
		return new DownloadAttachments(attachmentStoreFactory);
	}

}

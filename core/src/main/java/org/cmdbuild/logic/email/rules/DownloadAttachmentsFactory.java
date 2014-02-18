package org.cmdbuild.logic.email.rules;

import org.apache.commons.lang.Validate;

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

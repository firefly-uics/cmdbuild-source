package org.cmdbuild.services.scheduler.reademail;

import org.cmdbuild.model.email.Attachment;

public interface AttachmentStore {

	void store(Iterable<Attachment> attachments);

}

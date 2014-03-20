package org.cmdbuild.logic.email.rules;

import org.cmdbuild.model.email.Attachment;

public interface AttachmentStore {

	void store(Iterable<Attachment> attachments);

}

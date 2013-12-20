package org.cmdbuild.logic.email.rules;

public interface AttachmentStoreFactory {

	AttachmentStore create(String className, Long id);

}

package org.cmdbuild.service.rest.cxf;

public interface ErrorHandler {

	void alreadyExistingAttachmentName(String name);

	void attachmentNotFound(String attachmentId);

	void cardNotFound(Long id);

	void classNotFound(String id);

	void differentAttachmentName(String name);

	void domainNotFound(String id);

	void invalidType(String id);

	void lookupTypeNotFound(String id);

	void missingAttachmentId();

	void missingAttachmentName();

	void missingAttachmentMetadata();

	void missingFile();

	void missingParam(String name);

	void missingPassword();

	void missingUsername();

	void notAuthorized();

	void processNotFound(String id);

	void processInstanceNotFound(Long id);

	void processActivityNotFound(String id);

	void propagate(Throwable e);

	void roleNotFound(String id);

	void sessionNotFound(String id);

	void userNotFound(String id);

}
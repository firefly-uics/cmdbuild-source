package org.cmdbuild.service.rest.v2.cxf;

public interface ErrorHandler {

	void alreadyExistingAttachmentName(String name);

	void attachmentNotFound(String attachmentId);

	void cardNotFound(Long id);

	void classNotFound(String id);

	void classNotFoundClassIsProcess(String id);

	void differentAttachmentName(String name);

	void domainNotFound(String id);

	void domainTreeNotFound(String id);

	void extensionNotFound(String id);

	void functionNotFound(Long id);

	void invalidType(String id);

	void lookupTypeNotFound(String id);

	void missingAttachmentId();

	void missingAttachmentMetadata();

	void missingAttachmentName();

	void missingFile();

	void missingParam(String name);

	void missingPassword();

	void missingUsername();

	void notAuthorized();

	void processActivityNotFound(String id);

	void processInstanceNotFound(Long id);

	void processNotFound(String id);

	void propagate(Throwable e);

	void relationNotFound(Long id);

	void reportNotFound(Long id);

	void roleNotFound(String id);

	void sessionNotFound(String id);

	void userNotFound(String id);

}
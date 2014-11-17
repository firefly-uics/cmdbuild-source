package org.cmdbuild.service.rest.cxf;

public interface ErrorHandler {

	void cardNotFound(Long id);

	void classNotFound(String id);

	void domainNotFound(String id);

	void invalidType(String id);

	void lookupTypeNotFound(String id);

	void missingAttachment();

	void missingAttachmentName();

	void missingFile();

	void missingParam(String name);

	void missingPassword();

	void missingUsername();

	void processNotFound(String id);

	void processInstanceNotFound(Long id);

	void processActivityNotFound(String id);

	void propagate(Throwable e);

	void sessionNotFound(String id);

	void userNotFound(String id);

}
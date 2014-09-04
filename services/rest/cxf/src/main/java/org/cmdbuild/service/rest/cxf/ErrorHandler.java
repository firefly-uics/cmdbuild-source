package org.cmdbuild.service.rest.cxf;

public interface ErrorHandler {

	void typeNotFound(String id);

	void classNotFound(String id);

	void domainNotFound(String id);

	void processNotFound(String id);

	void processInstanceNotFound(Long id);

	void processActivityNotFound(String id);

	void cardNotFound(Long id);

	void missingParam(String name);

	void invalidParam(String value);

	void propagate(Throwable e);

}
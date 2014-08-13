package org.cmdbuild.service.rest.serialization;

public interface ErrorHandler {

	void entryTypeNotFound(String name);

	void classNotFound(String name);

	void domainNotFound(String name);

	void cardNotFound(Long id);

}
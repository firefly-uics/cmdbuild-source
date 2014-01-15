package org.cmdbuild.service.rest.serialization;

public interface ErrorHandler {

	void classNotFound(String className);

	void domainNotFound(String domainName);

}
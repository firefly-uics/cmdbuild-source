package org.cmdbuild.service.rest.cxf;

public interface ErrorHandler {

	void cardNotFound(Long id);

	/**
	 * @deprecated Use {@link classNotFound(Long)} instead.
	 */
	@Deprecated
	void classNotFound(String id);

	void classNotFound(Long id);

	/**
	 * @deprecated Use {@link domainNotFound(Long)} instead.
	 */
	@Deprecated
	void domainNotFound(String id);

	void domainNotFound(Long id);

	void invalidType(String id);

	/**
	 * @deprecated Use {@link lookupTypeNotFound(Long)} instead.
	 */
	@Deprecated
	void lookupTypeNotFound(String id);

	void lookupTypeNotFound(Long id);

	void missingParam(String name);

	void missingPassword();

	void missingUsername();

	void processNotFound(Long id);

	void processInstanceNotFound(Long id);

	void processActivityNotFound(Long id);

	void propagate(Throwable e);

	void tokenNotFound(String token);

}
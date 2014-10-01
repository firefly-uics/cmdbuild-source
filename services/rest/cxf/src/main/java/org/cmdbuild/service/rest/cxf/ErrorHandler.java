package org.cmdbuild.service.rest.cxf;

public interface ErrorHandler {

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

	void lookupTypeNotFound(Long id);

	/**
	 * @deprecated Use {@link processNotFound(Long)} instead.
	 */
	@Deprecated
	void processNotFound(String id);

	void processNotFound(Long id);

	void processInstanceNotFound(Long id);

	/**
	 * @deprecated Use {@link processNotFound(Long)} instead.
	 */
	@Deprecated
	void processActivityNotFound(String id);

	void processActivityNotFound(Long id);

	void cardNotFound(Long id);

	void missingParam(String name);

	void invalidType(String id);

	void propagate(Throwable e);

}
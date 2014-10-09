package org.cmdbuild.service.rest.cxf;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.cmdbuild.service.rest.logging.LoggingSupport;

public class WebApplicationExceptionErrorHandler implements ErrorHandler, LoggingSupport {

	@Override
	public void cardNotFound(final Long id) {
		logger.error("card not found '{}'", id);
		notFound(id);
	}

	@Override
	public void classNotFound(final String id) {
		logger.error("class not found '{}'", id);
		notFound(id);
	}

	@Override
	public void classNotFound(final Long id) {
		logger.error("class not found '{}'", id);
		notFound(id);
	}

	@Override
	public void domainNotFound(final String id) {
		logger.error("domain not found '{}'", id);
		notFound(id);
	}

	@Override
	public void domainNotFound(final Long id) {
		logger.error("domain not found '{}'", id);
		notFound(id);
	}

	@Override
	public void missingUsername() {
		logger.error("missing username");
		throw new WebApplicationException(Response.status(BAD_REQUEST) //
				.entity("missing username") //
				.build());
	}

	@Override
	public void invalidType(final String id) {
		logger.error("invalid param '{}'", id);
		throw new WebApplicationException(Response.status(BAD_REQUEST) //
				.entity(id) //
				.build());
	}

	@Override
	public void lookupTypeNotFound(final String id) {
		logger.error("lookup type not found '{}'", id);
		notFound(id);
	}

	@Override
	public void lookupTypeNotFound(final Long id) {
		logger.error("lookup type not found '{}'", id);
		notFound(id);
	}

	@Override
	public void missingParam(final String name) {
		logger.error("missing param '{}'", name);
		notFound(name);
	}

	@Override
	public void missingPassword() {
		logger.error("missing password");
		throw new WebApplicationException(Response.status(BAD_REQUEST) //
				.entity("missing password") //
				.build());
	}

	@Override
	public void processNotFound(final Long id) {
		logger.error("process not found '{}'", id);
		notFound(id);
	}

	@Override
	public void processInstanceNotFound(final Long id) {
		logger.error("process instance not found '{}'", id);
		notFound(id);
	}

	@Override
	public void processActivityNotFound(final Long id) {
		logger.error("process instance activity not found '{}'", id);
		notFound(id);
	}

	@Override
	public void propagate(final Throwable e) {
		logger.error("unhandled exception", e);
		throw new WebApplicationException(e, Response.serverError() //
				.entity(e) //
				.build());
	}

	@Override
	public void tokenNotFound(final String token) {
		logger.error("token not found '{}'", token);
		notFound(token);
	}

	private void notFound(final Object entity) {
		throw new WebApplicationException(Response.status(NOT_FOUND) //
				.entity(entity) //
				.build());
	}

}

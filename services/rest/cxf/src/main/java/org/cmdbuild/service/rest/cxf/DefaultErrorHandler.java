package org.cmdbuild.service.rest.cxf;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class DefaultErrorHandler implements ErrorHandler {

	@Override
	public void typeNotFound(final String id) {
		notFound(id);
	}

	@Override
	public void classNotFound(final String id) {
		notFound(id);
	}

	@Override
	public void domainNotFound(final String id) {
		notFound(id);
	}

	@Override
	public void processNotFound(final String id) {
		notFound(id);
	}

	@Override
	public void processInstanceNotFound(final Long id) {
		notFound(id);
	}

	@Override
	public void processActivityNotFound(final String id) {
		notFound(id);
	}

	@Override
	public void cardNotFound(final Long id) {
		notFound(id);
	}

	private void notFound(final Object entity) {
		throw new WebApplicationException(Response.status(Status.NOT_FOUND) //
				.entity(entity) //
				.build());
	}

	@Override
	public void missingParam(final String name) {
		notFound(name);
	}

	@Override
	public void invalidParam(final String value) {
		throw new WebApplicationException(Response.status(Status.BAD_REQUEST) //
				.entity(value) //
				.build());
	}

	@Override
	public void propagate(final Throwable e) {
		throw new WebApplicationException(e, Response.status(Status.INTERNAL_SERVER_ERROR) //
				.build());
	}

}

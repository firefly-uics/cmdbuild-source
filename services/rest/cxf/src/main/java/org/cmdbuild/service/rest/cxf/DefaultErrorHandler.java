package org.cmdbuild.service.rest.cxf;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cmdbuild.service.rest.serialization.ErrorHandler;

public class DefaultErrorHandler implements ErrorHandler {

	@Override
	public void entryTypeNotFound(final String name) {
		throw new WebApplicationException(Response.status(Status.NOT_FOUND) //
				.entity(name) //
				.build());
	}

	@Override
	public void classNotFound(final String name) {
		throw new WebApplicationException(Response.status(Status.NOT_FOUND) //
				.entity(name) //
				.build());
	}

	@Override
	public void domainNotFound(final String name) {
		throw new WebApplicationException(Response.status(Status.NOT_FOUND) //
				.entity(name) //
				.build());
	}

	@Override
	public void cardNotFound(final Long id) {
		throw new WebApplicationException(Response.status(Status.NOT_FOUND) //
				.entity(id) //
				.build());
	}

	@Override
	public void invalidParam(final String value) {
		throw new WebApplicationException(Response.status(Status.BAD_REQUEST) //
				.entity(value) //
				.build());
	}

	@Override
	public void missingParam(final String value) {
		throw new WebApplicationException(Response.status(Status.BAD_REQUEST) //
				.entity(value) //
				.build());
	}

}

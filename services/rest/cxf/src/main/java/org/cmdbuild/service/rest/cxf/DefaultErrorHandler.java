package org.cmdbuild.service.rest.cxf;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cmdbuild.service.rest.serialization.ErrorHandler;

public class DefaultErrorHandler implements ErrorHandler {

	@Override
	public void classNotFound(final String className) {
		throw new WebApplicationException(Response.status(Status.NOT_FOUND) //
				.entity(className) //
				.build());
	}

	@Override
	public void domainNotFound(final String domainName) {
		throw new WebApplicationException(Response.status(Status.NOT_FOUND) //
				.entity(domainName) //
				.build());
	}

	@Override
	public void cardNotFound(final Long id) {
		throw new WebApplicationException(Response.status(Status.NOT_FOUND) //
				.entity(id) //
				.build());
	}

}

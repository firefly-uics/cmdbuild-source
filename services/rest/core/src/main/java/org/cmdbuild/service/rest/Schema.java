package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.Constants.ACTIVE;
import static org.cmdbuild.service.rest.Constants.NAME;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.dto.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.ClassDetailResponse;

@Path("/schema/")
@Produces(APPLICATION_JSON)
public interface Schema {

	@GET
	@Path("/classes/")
	ClassDetailResponse getClasses( //
			@QueryParam(ACTIVE) boolean activeOnly //
	);

	@GET
	@Path("/classes/{name}/attributes/")
	AttributeDetailResponse getAttributes( //
			@PathParam(NAME) String name, //
			@QueryParam(ACTIVE) boolean activeOnly //
	);

}

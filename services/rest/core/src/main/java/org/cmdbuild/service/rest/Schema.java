package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.dto.ClassDetailResponse;

@Path("/schema/")
@Produces(APPLICATION_JSON)
public interface Schema {

	@GET
	@Path("/classes/")
	ClassDetailResponse getClasses();

}

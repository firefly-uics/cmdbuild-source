package org.cmdbuild.service.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.dto.ClassDetailResponse;

@Path("/schema/")
@Produces("application/json")
public interface Schema {

	@GET
	@Path("/classes/")
	ClassDetailResponse getClasses();

}

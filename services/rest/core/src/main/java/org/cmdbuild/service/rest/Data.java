package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.Constants.NAME;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.dto.CardDetailResponse;

@Path("/data/")
@Produces(APPLICATION_JSON)
public interface Data {

	@GET
	@Path("/classes/{name}/")
	CardDetailResponse getCards( //
			@PathParam(NAME) String name //
	);

}

package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.Constants.ACTIVE;
import static org.cmdbuild.service.rest.Constants.LIMIT;
import static org.cmdbuild.service.rest.Constants.NAME;
import static org.cmdbuild.service.rest.Constants.OFFSET;
import static org.cmdbuild.service.rest.Constants.TYPE;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.dto.AttributeDetailResponse;

@Path("attributes/")
@Produces(APPLICATION_JSON)
public interface Attributes {

	@GET
	@Path("{type}/{name}/")
	AttributeDetailResponse getAttributes( //
			@PathParam(TYPE) String type, //
			@PathParam(NAME) String name, //
			@QueryParam(ACTIVE) boolean activeOnly, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(OFFSET) Integer offset //
	);

}

package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.constants.Serialization.ID;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.START;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.service.rest.dto.CardListResponse;
import org.cmdbuild.service.rest.dto.CardResponse;
import org.cmdbuild.service.rest.dto.NewCardResponse;

@Path("classes/{name}/cards/")
@Produces(APPLICATION_JSON)
public interface Cards {

	@POST
	@Path(EMPTY)
	NewCardResponse create( //
			@PathParam(NAME) String name, //
			MultivaluedMap<String, String> formParam //
	);

	@GET
	@Path("{id}/")
	CardResponse read( //
			@PathParam(NAME) String name, //
			@PathParam(ID) Long id //
	);

	@GET
	@Path(EMPTY)
	CardListResponse readAll( //
			@PathParam(NAME) String name, //
			@QueryParam(FILTER) String filter, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@PUT
	@Path("{id}/")
	void update( //
			@PathParam(NAME) String name, //
			@PathParam(ID) Long id, //
			MultivaluedMap<String, String> formParam //
	);

	@DELETE
	@Path("{id}/")
	void delete( //
			@PathParam(NAME) String name, //
			@PathParam(ID) Long id //
	);

}
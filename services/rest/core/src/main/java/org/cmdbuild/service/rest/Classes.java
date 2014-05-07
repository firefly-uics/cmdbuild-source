package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.Constants.ACTIVE;
import static org.cmdbuild.service.rest.Constants.FILTER;
import static org.cmdbuild.service.rest.Constants.ID;
import static org.cmdbuild.service.rest.Constants.LIMIT;
import static org.cmdbuild.service.rest.Constants.NAME;
import static org.cmdbuild.service.rest.Constants.START;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.service.rest.dto.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.CardListResponse;
import org.cmdbuild.service.rest.dto.CardResponse;
import org.cmdbuild.service.rest.dto.ClassListResponse;
import org.cmdbuild.service.rest.dto.ClassResponse;
import org.cmdbuild.service.rest.dto.NewCardResponse;

@Path("classes/")
@Produces(APPLICATION_JSON)
public interface Classes {

	@GET
	@Path("")
	ClassListResponse getClasses( //
			@QueryParam(ACTIVE) boolean activeOnly, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{name}/")
	ClassResponse getClassDetail( //
			@PathParam(NAME) String name //
	);

	@GET
	@Path("{name}/attributes/")
	@Deprecated
	AttributeDetailResponse getAttributes( //
			@PathParam(NAME) String name, //
			@QueryParam(ACTIVE) boolean activeOnly, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{name}/cards/")
	CardListResponse getCards( //
			@PathParam(NAME) String name, //
			@QueryParam(FILTER) String filter, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@POST
	@Path("{name}/cards/")
	NewCardResponse createCard( //
			@PathParam(NAME) String name, //
			MultivaluedMap<String, String> formParam //
	);

	@GET
	@Path("{name}/cards/{id}/")
	CardResponse readCard( //
			@PathParam(NAME) String name, //
			@PathParam(ID) Long id //
	);

	@PUT
	@Path("{name}/cards/{id}/")
	void updateCard( //
			@PathParam(NAME) String name, //
			@PathParam(ID) Long id //
	// TODO pass values as parameters
	);

	@DELETE
	@Path("{name}/cards/{id}/")
	void deleteCard( //
			@PathParam(NAME) String name, //
			@PathParam(ID) Long id //
	);

}

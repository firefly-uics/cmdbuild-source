package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.Constants.ACTIVE;
import static org.cmdbuild.service.rest.Constants.ID;
import static org.cmdbuild.service.rest.Constants.LIMIT;
import static org.cmdbuild.service.rest.Constants.NAME;
import static org.cmdbuild.service.rest.Constants.OFFSET;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.dto.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.CardListResponse;
import org.cmdbuild.service.rest.dto.CardResponse;
import org.cmdbuild.service.rest.dto.ClassDetailResponse;

@Path("classes/")
@Produces(APPLICATION_JSON)
public interface Classes {

	@GET
	@Path("")
	ClassDetailResponse getClasses( //
			@QueryParam(ACTIVE) boolean activeOnly, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(OFFSET) Integer offset //
	);

	@GET
	@Path("{name}/attributes/")
	@Deprecated
	AttributeDetailResponse getAttributes( //
			@PathParam(NAME) String name, //
			@QueryParam(ACTIVE) boolean activeOnly, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(OFFSET) Integer offset //
	);

	@GET
	@Path("{name}/cards/")
	CardListResponse getCards( //
			@PathParam(NAME) String name, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(OFFSET) Integer offset //
	);

	@GET
	@Path("{name}/cards/{id}/")
	CardResponse getCard( //
			@PathParam(NAME) String name, //
			@PathParam(ID) Long id //
	);

}

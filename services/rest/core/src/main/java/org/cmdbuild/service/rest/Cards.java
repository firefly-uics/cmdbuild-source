package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.constants.Serialization.ID;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.START;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.dto.Card;
import org.cmdbuild.service.rest.dto.ResponseMultiple;
import org.cmdbuild.service.rest.dto.ResponseSingle;

@Path("classes/{" + TYPE + "}/cards/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface Cards {

	@POST
	@Path(EMPTY)
	ResponseSingle<Long> create( //
			@PathParam(TYPE) String type, //
			Card card);

	@GET
	@Path("{" + ID + "}/")
	ResponseSingle<Card> read( //
			@PathParam(TYPE) String type, //
			@PathParam(ID) Long id //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<Card> read( //
			@PathParam(TYPE) String type, //
			@QueryParam(FILTER) String filter, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@PUT
	@Path("{" + ID + "}/")
	void update( //
			@PathParam(TYPE) String type, //
			@PathParam(ID) Long id, //
			Card card //
	);

	@DELETE
	@Path("{" + ID + "}/")
	void delete( //
			@PathParam(TYPE) String type, //
			@PathParam(ID) Long id //
	);

}

package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.CARD_ID;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_ID;
import static org.cmdbuild.service.rest.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.SORT;
import static org.cmdbuild.service.rest.constants.Serialization.START;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.model.Card;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

@Path("classes/{" + CLASS_ID + "}/cards/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface Cards {

	@POST
	@Path(EMPTY)
	ResponseSingle<Long> create( //
			@PathParam(CLASS_ID) String classId, //
			Card card);

	@GET
	@Path("{" + CARD_ID + "}/")
	ResponseSingle<Card> read( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long id //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<Card> read( //
			@PathParam(CLASS_ID) String classId, //
			@QueryParam(FILTER) String filter, //
			@QueryParam(SORT) String sort, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@PUT
	@Path("{" + CARD_ID + "}/")
	void update( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long id, //
			Card card //
	);

	@DELETE
	@Path("{" + CARD_ID + "}/")
	void delete( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long id //
	);

}

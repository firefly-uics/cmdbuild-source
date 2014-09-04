package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.ID;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.START;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ADVANCE;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessInstance;
import org.cmdbuild.service.rest.dto.SimpleResponse;

@Path("processes/{" + TYPE + "}/instances/")
@Produces(APPLICATION_JSON)
public interface ProcessInstances {

	@POST
	@Path(EMPTY)
	SimpleResponse<Long> create( //
			@PathParam(TYPE) String type, //
			@FormParam(UNDERSCORED_ADVANCE) boolean advance, //
			MultivaluedMap<String, String> formParam //
	);

	@GET
	@Path("{" + ID + "}")
	SimpleResponse<ProcessInstance> read( //
			@PathParam(TYPE) String type, //
			@PathParam(ID) Long id //
	);

	@GET
	@Path(EMPTY)
	ListResponse<ProcessInstance> read( //
			@PathParam(TYPE) String type, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

}

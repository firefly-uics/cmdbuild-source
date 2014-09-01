package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.INSTANCE;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessActivityShort;

@Path("processes/{" + TYPE + "}/instances/{" + INSTANCE + "}/activities")
@Produces(APPLICATION_JSON)
public interface ProcessInstanceActivities {

	@GET
	@Path(EMPTY)
	ListResponse<ProcessActivityShort> read( //
			@PathParam(TYPE) String type, //
			@PathParam(INSTANCE) Long instance //
	);

}

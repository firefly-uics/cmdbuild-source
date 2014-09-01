package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.ACTIVITY;
import static org.cmdbuild.service.rest.constants.Serialization.INSTANCE;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessActivity;
import org.cmdbuild.service.rest.dto.ProcessActivityDefinition;
import org.cmdbuild.service.rest.dto.SimpleResponse;

@Path("processes/{" + TYPE + "}/instances/{" + INSTANCE + "}/activities/")
@Produces(APPLICATION_JSON)
public interface ProcessInstanceActivities {

	@GET
	@Path(EMPTY)
	ListResponse<ProcessActivity> read( //
			@PathParam(TYPE) String type, //
			@PathParam(INSTANCE) Long instance //
	);

	@GET
	@Path("{" + ACTIVITY + "}/")
	SimpleResponse<ProcessActivityDefinition> read( //
			@PathParam(TYPE) String type, //
			@PathParam(INSTANCE) Long instance, //
			@PathParam(ACTIVITY) String activity //
	);

}

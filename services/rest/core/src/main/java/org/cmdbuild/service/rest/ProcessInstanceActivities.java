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

import org.cmdbuild.service.rest.dto.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.dto.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.dto.ResponseMultiple;
import org.cmdbuild.service.rest.dto.ResponseSingle;

@Path("processes/{" + TYPE + "}/instances/{" + INSTANCE + "}/activities/")
@Produces(APPLICATION_JSON)
public interface ProcessInstanceActivities {

	@GET
	@Path(EMPTY)
	ResponseMultiple<ProcessActivityWithBasicDetails> read( //
			@PathParam(TYPE) String type, //
			@PathParam(INSTANCE) Long instance //
	);

	@GET
	@Path("{" + ACTIVITY + "}/")
	ResponseSingle<ProcessActivityWithFullDetails> read( //
			@PathParam(TYPE) String type, //
			@PathParam(INSTANCE) Long instance, //
			@PathParam(ACTIVITY) String activity //
	);

}

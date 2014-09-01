package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.dto.ProcessActivityDefinition;
import org.cmdbuild.service.rest.dto.SimpleResponse;

@Path("processes/{" + TYPE + "}/start_activity/")
@Produces(APPLICATION_JSON)
public interface ProcessStartActivity {

	@GET
	@Path(EMPTY)
	SimpleResponse<ProcessActivityDefinition> read( //
			@PathParam(TYPE) String type //
	);

}

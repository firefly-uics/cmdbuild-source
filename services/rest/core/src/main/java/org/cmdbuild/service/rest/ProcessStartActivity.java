package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.PROCESS_ID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseSingle;

@Path("processes/{" + PROCESS_ID + "}/start_activity/")
@Produces(APPLICATION_JSON)
public interface ProcessStartActivity {

	@GET
	@Path(EMPTY)
	ResponseSingle<ProcessActivityWithFullDetails> read( //
			@PathParam(PROCESS_ID) String processId //
	);

}

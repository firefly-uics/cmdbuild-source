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
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

@Path("process_activities/")
@Produces(APPLICATION_JSON)
public interface Activities {

	@GET
	@Path(EMPTY)
	ResponseMultiple<ProcessActivityWithBasicDetails> read( //
			@QueryParam(TYPE) String type, //
			@QueryParam(INSTANCE) Long instance //
	);

	@GET
	@Path("{" + ACTIVITY + "}/")
	ResponseSingle<ProcessActivityWithFullDetails> read( //
			@PathParam(ACTIVITY) String activity, //
			@QueryParam(TYPE) String type, //
			@QueryParam(INSTANCE) Long instance //
	);

}

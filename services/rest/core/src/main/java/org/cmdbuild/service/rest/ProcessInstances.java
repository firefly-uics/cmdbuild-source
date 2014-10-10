package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.PROCESS_ID;
import static org.cmdbuild.service.rest.constants.Serialization.PROCESS_INSTANCE_ID;
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

import org.cmdbuild.service.rest.model.ProcessInstance;
import org.cmdbuild.service.rest.model.ProcessInstanceAdvanceable;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

@Path("processes/{" + PROCESS_ID + "}/instances/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface ProcessInstances {

	@POST
	@Path(EMPTY)
	ResponseSingle<Long> create( //
			@PathParam(PROCESS_ID) Long processId, //
			ProcessInstanceAdvanceable processInstance //
	);

	@GET
	@Path("{" + PROCESS_INSTANCE_ID + "}")
	ResponseSingle<ProcessInstance> read( //
			@PathParam(PROCESS_ID) Long processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long instanceId //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<ProcessInstance> read( //
			@PathParam(PROCESS_ID) Long processId, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@PUT
	@Path("{" + PROCESS_INSTANCE_ID + "}")
	void update( //
			@PathParam(PROCESS_ID) Long processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long instanceId, //
			ProcessInstanceAdvanceable processInstance //
	);

	@DELETE
	@Path("{" + PROCESS_INSTANCE_ID + "}")
	void delete( //
			@PathParam(PROCESS_ID) Long processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long instanceId //
	);

}

package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.START;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessInstance;

@Path("processes/{" + NAME + "}/instances/")
@Produces(APPLICATION_JSON)
public interface ProcessInstances {

	@GET
	@Path(EMPTY)
	ListResponse<ProcessInstance> readAll( //
			@PathParam(NAME) String name, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

}

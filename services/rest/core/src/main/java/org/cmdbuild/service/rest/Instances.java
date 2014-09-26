package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.ID;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.START;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ACTIVITY;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ADVANCE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.service.rest.dto.ProcessInstance;
import org.cmdbuild.service.rest.dto.ResponseMultiple;
import org.cmdbuild.service.rest.dto.ResponseSingle;

@Path("process_instances/")
@Produces(APPLICATION_JSON)
public interface Instances {

	@POST
	@Path(EMPTY)
	ResponseSingle<Long> create( //
			MultivaluedMap<String, String> formParams, //
			@FormParam(UNDERSCORED_TYPE) String type, //
			@FormParam(UNDERSCORED_ADVANCE) boolean advance //
	);

	@GET
	@Path("{" + ID + "}")
	ResponseSingle<ProcessInstance> read( //
			@PathParam(ID) Long id, //
			@QueryParam(TYPE) String type //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<ProcessInstance> read( //
			@QueryParam(TYPE) String type, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@PUT
	@Path("{" + ID + "}")
	void update( //
			@PathParam(ID) Long id, //
			@FormParam(UNDERSCORED_TYPE) String type, //
			@FormParam(UNDERSCORED_ACTIVITY) String activity, //
			@FormParam(UNDERSCORED_ADVANCE) boolean advance, //
			MultivaluedMap<String, String> formParams //
	);

	@DELETE
	@Path("{" + ID + "}")
	void delete( //
			@PathParam(ID) Long id, //
			@FormParam(TYPE) String type //
	);

}

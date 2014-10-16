package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.ID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.service.rest.model.Session;

@Path("sessions/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface Sessions {

	@POST
	@Path(EMPTY)
	ResponseSingle<String> create( //
			Session session);

	@GET
	@Path("{" + ID + "}/")
	ResponseSingle<Session> read( //
			@PathParam(ID) String id //
	);

	@PUT
	@Path("{" + ID + "}/")
	void update( //
			@PathParam(ID) String id, //
			Session session //
	);

	@DELETE
	@Path("{" + ID + "}/")
	void delete( //
			@PathParam(ID) String id //
	);

	@GET
	@Path("{" + ID + "}/groups/")
	ResponseMultiple<String> readGroups( //
			@PathParam(ID) String id //
	);

}

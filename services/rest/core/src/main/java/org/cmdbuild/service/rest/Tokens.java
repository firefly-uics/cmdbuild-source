package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.TOKEN;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.model.Credentials;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

@Path("tokens/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface Tokens {

	@POST
	@Path(EMPTY)
	ResponseSingle<String> create( //
			Credentials credentials);

	@GET
	@Path("{" + TOKEN + "}/")
	ResponseSingle<Credentials> read( //
			@PathParam(TOKEN) String token //
	);

	@PUT
	@Path("{" + TOKEN + "}/")
	void update( //
			@PathParam(TOKEN) String token, //
			Credentials credentials //
	);

	@DELETE
	@Path("{" + TOKEN + "}/")
	void delete( //
			@PathParam(TOKEN) String token //
	);

	@GET
	@Path("{" + TOKEN + "}/groups/")
	ResponseMultiple<String> readGroups( //
			@PathParam(TOKEN) String token //
	);

}

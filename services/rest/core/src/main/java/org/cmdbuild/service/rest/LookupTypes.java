package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.Constants.ACTIVE;
import static org.cmdbuild.service.rest.Constants.TYPE;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.dto.LookupDetailResponse;
import org.cmdbuild.service.rest.dto.LookupTypeDetailResponse;

@Path("lookuptypes/")
@Produces(APPLICATION_JSON)
public interface LookupTypes {

	@GET
	@Path("")
	LookupTypeDetailResponse getLookupTypes();

	@GET
	@Path("{type}/")
	LookupDetailResponse getLookups( //
			@PathParam(TYPE) String type, //
			@QueryParam(ACTIVE) boolean activeOnly //
	);

}

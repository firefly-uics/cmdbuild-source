package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.Constants.ACTIVE;
import static org.cmdbuild.service.rest.Constants.TYPE;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.LookupTypeDetail;

@Path("lookuptypes/")
@Produces(APPLICATION_JSON)
public interface LookupTypes {

	@GET
	@Path("")
	ListResponse<LookupTypeDetail> getLookupTypes();

	@GET
	@Path("{type}/")
	ListResponse<LookupDetail> getLookups( //
			@PathParam(TYPE) String type, //
			@QueryParam(ACTIVE) boolean activeOnly //
	);

}

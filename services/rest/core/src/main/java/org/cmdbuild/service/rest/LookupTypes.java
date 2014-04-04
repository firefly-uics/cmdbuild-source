package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.Constants.ACTIVE;
import static org.cmdbuild.service.rest.Constants.ID;
import static org.cmdbuild.service.rest.Constants.LIMIT;
import static org.cmdbuild.service.rest.Constants.START;
import static org.cmdbuild.service.rest.Constants.TYPE;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.LookupTypeDetail;
import org.cmdbuild.service.rest.dto.SimpleResponse;

@Path("lookuptypes/")
@Produces(APPLICATION_JSON)
public interface LookupTypes {

	@GET
	@Path("")
	ListResponse<LookupTypeDetail> getLookupTypes( //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{type}/")
	SimpleResponse<LookupTypeDetail> getLookupType( //
			@PathParam(TYPE) String type //
	);

	@GET
	@Path("{type}/values/")
	ListResponse<LookupDetail> getLookups( //
			@PathParam(TYPE) String type, //
			@QueryParam(ACTIVE) boolean activeOnly, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{type}/values/{id}/")
	SimpleResponse<LookupDetail> getLookup( //
			@PathParam(TYPE) String type, //
			@PathParam(ID) Long id //
	);

}

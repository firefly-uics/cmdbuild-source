package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.DOMAIN_ID;
import static org.cmdbuild.service.rest.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.START;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.model.Relation;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

@Path("domains/{" + DOMAIN_ID + "}/relations/")
@Produces(APPLICATION_JSON)
public interface Relations {

	@POST
	@Path(EMPTY)
	ResponseSingle<Long> create( //
			@PathParam(DOMAIN_ID) String classId, //
			Relation relation);

	@GET
	@Path(EMPTY)
	ResponseMultiple<Relation> read( //
			@PathParam(DOMAIN_ID) String domainId, //
			@QueryParam(FILTER) String filter, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

}

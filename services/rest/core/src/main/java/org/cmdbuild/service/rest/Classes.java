package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.START;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.dto.FullClassDetail;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleClassDetail;
import org.cmdbuild.service.rest.dto.SimpleResponse;

@Path("classes/")
@Produces(APPLICATION_JSON)
public interface Classes {

	@GET
	@Path(EMPTY)
	ListResponse<SimpleClassDetail> readAll( //
			@QueryParam(ACTIVE) boolean activeOnly, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{name}/")
	SimpleResponse<FullClassDetail> read( //
			@PathParam(NAME) String name //
	);

}

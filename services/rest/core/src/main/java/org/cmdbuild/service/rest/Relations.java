package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.CARD_ID;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_ID;
import static org.cmdbuild.service.rest.constants.Serialization.DOMAIN_SOURCE;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.START;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.Relation;

@Path("domains/{" + TYPE + "}/relations/")
@Produces(APPLICATION_JSON)
public interface Relations {

	@GET
	@Path(EMPTY)
	ListResponse<Relation> read( //
			@PathParam(TYPE) String type, //
			@QueryParam(CLASS_ID) String classId, //
			@QueryParam(CARD_ID) Long cardId, //
			@QueryParam(DOMAIN_SOURCE) String domainSource, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

}

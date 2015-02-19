package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.EMAIL_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v2.model.Email;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

@Path("emails/")
@Produces(APPLICATION_JSON)
public interface Emails {

	@GET
	@Path("statuses/")
	ResponseMultiple<String> statuses();

	@POST
	@Path(EMPTY)
	ResponseSingle<Long> create( //
			Email email //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<Long> readAll( //
			@QueryParam(FILTER) String filter, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{" + EMAIL_ID + "}/")
	ResponseSingle<Email> read( //
			@PathParam(EMAIL_ID) Long id //
	);

	@PUT
	@Path("{" + EMAIL_ID + "}/")
	void update( //
			@PathParam(EMAIL_ID) Long id, //
			Email email //
	);

	@DELETE
	@Path("{" + EMAIL_ID + "}/")
	void delete( //
			@PathParam(EMAIL_ID) Long id //
	);

}

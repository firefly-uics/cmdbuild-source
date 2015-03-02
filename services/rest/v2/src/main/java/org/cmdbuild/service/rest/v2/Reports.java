package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v2.model.LongIdAndDescription;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;

@Path("reports/")
@Produces(APPLICATION_JSON)
public interface Reports {

	@GET
	@Path(EMPTY)
	ResponseMultiple<LongIdAndDescription> readAll( //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

}

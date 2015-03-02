package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.REPORT_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;

@Path("reports/{" + REPORT_ID + "}/attributes/")
@Produces(APPLICATION_JSON)
public interface ReportAttributes {

	@GET
	@Path(EMPTY)
	ResponseMultiple<Attribute> readAll( //
			@PathParam(REPORT_ID) Long reportId, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

}

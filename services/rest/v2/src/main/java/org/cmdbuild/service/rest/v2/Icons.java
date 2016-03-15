package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ICON;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ID;

import javax.activation.DataHandler;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.service.rest.v2.model.Icon;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

@Path("icons/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface Icons {

	@POST
	@Path(EMPTY)
	@Consumes(MULTIPART_FORM_DATA)
	@Produces(APPLICATION_JSON)
	ResponseSingle<Icon> create( //
			@Multipart(ICON) Icon icon, //
			@Multipart(FILE) DataHandler dataHandler //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<Icon> read( //
	);

	@GET
	@Path("{" + ID + "}/")
	ResponseSingle<Icon> read( //
			@PathParam(ID) String id //
	);

	@GET
	@Path("{" + ID + "}/{file: [^/]+}")
	@Produces(APPLICATION_OCTET_STREAM)
	DataHandler download( //
			@PathParam(ID) String id //
	);

	@PUT
	@Path("{" + ID + "}/")
	@Consumes(MULTIPART_FORM_DATA)
	@Produces(APPLICATION_JSON)
	void update( //
			@PathParam(ID) String id, //
			@Multipart(FILE) DataHandler dataHandler //
	);

	@DELETE
	@Path("{" + ID + "}/")
	void delete( //
			@PathParam(ID) String id //
	);

}

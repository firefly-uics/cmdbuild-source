package org.cmdbuild.service.rest.cxf;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.ATTACHMENT_ID;
import static org.cmdbuild.service.rest.constants.Serialization.CARD_ID;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_ID;
import static org.cmdbuild.service.rest.constants.Serialization.FILE;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;

import javax.activation.DataHandler;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.service.rest.model.ResponseSingle;

@Path("classes/{" + CLASS_ID + "}/cards/{" + CARD_ID + "}/attachments/")
public interface CardAttachments {

	@POST
	@Path(EMPTY)
	@Consumes(MULTIPART_FORM_DATA)
	@Produces(APPLICATION_JSON)
	ResponseSingle<String> create( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId, //
			@Multipart(NAME) String attachmentName, //
			@Multipart(FILE) DataHandler dataHandler //
	);

	@PUT
	@Path("{" + ATTACHMENT_ID + "}/")
	@Consumes(MULTIPART_FORM_DATA)
	@Produces(APPLICATION_JSON)
	void update( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId, //
			@PathParam(ATTACHMENT_ID) String attachmentId, //
			@Multipart(FILE) DataHandler dataHandler //
	);

}
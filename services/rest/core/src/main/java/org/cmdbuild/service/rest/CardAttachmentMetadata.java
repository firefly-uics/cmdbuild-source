package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.ATTACHMENT_ID;
import static org.cmdbuild.service.rest.constants.Serialization.CARD_ID;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_ID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.model.AttachmentMetadata;
import org.cmdbuild.service.rest.model.ResponseSingle;

@Path("classes/{" + CLASS_ID + "}/cards/{" + CARD_ID + "}/attachments/{" + ATTACHMENT_ID + "}/metadata/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface CardAttachmentMetadata {

	@GET
	@Path(EMPTY)
	ResponseSingle<AttachmentMetadata> read( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId, //
			@PathParam(ATTACHMENT_ID) String attachmentId //
	);

	@PUT
	@Path(EMPTY)
	void update( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId, //
			@PathParam(ATTACHMENT_ID) String attachmentId, //
			AttachmentMetadata attachmentMetadata //
	);

}
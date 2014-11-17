package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.ATTACHMENT_ID;
import static org.cmdbuild.service.rest.constants.Serialization.CARD_ID;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ATTACHMENT;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_FILE;

import javax.activation.DataHandler;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

@Path("classes/{" + CLASS_ID + "}/cards/{" + CARD_ID + "}/attachments/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface CardAttachments {

	@POST
	@Path(EMPTY)
	@Consumes(MULTIPART_FORM_DATA)
	ResponseSingle<String> create( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId, //
			@Multipart(UNDERSCORED_ATTACHMENT) Attachment attachment, //
			@Multipart(UNDERSCORED_FILE) DataHandler dataHandler //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<Attachment> read( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId //
	);

	@GET
	@Path("{" + ATTACHMENT_ID + "}/")
	@Produces(APPLICATION_OCTET_STREAM)
	DataHandler read( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId, //
			@PathParam(ATTACHMENT_ID) String attachmentId //
	);

	@DELETE
	@Path("{" + ATTACHMENT_ID + "}/")
	void delete( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId, //
			@PathParam(ATTACHMENT_ID) String attachmentId //
	);

}
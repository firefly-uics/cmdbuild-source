package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.constants.Serialization.CARD_ID;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_ID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseMultiple;

@Path("classes/{" + CLASS_ID + "}/cards/{" + CARD_ID + "}/attachments/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface CardAttachments {

	@GET
	ResponseMultiple<Attachment> read( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId //
	);

}

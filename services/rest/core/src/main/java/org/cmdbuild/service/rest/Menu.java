package org.cmdbuild.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.dto.MenuDetail;
import org.cmdbuild.service.rest.dto.ResponseSingle;

@Path("menu/")
@Produces(APPLICATION_JSON)
public interface Menu {

	@GET
	@Path(EMPTY)
	ResponseSingle<MenuDetail> read();

}

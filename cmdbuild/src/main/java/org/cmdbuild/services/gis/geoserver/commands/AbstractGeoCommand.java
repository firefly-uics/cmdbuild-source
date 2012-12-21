package org.cmdbuild.services.gis.geoserver.commands;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.config.GisProperties;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.cmdbuild.logger.Log;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public abstract class AbstractGeoCommand {

	protected static final Map<String, String> atomNS;

	static {
		atomNS = new HashMap<String, String>();
		atomNS.put("atom", "http://www.w3.org/2005/Atom");
	}

	private final ClientResource createClient(final String url) {
		ClientResource cr = new ClientResource(url);
		cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, getGeoServerAdminUser(), getGeoServerAdminPassword());
		return cr;
	}

	protected final void put(InputStream data, String url, MediaType mime) {
		ClientResource cr = createClient(url);
		Representation input = new InputRepresentation(data, mime);
		Log.REST.debug("PUT REQUEST " + url);
		cr.put(input);
	}

	protected final Document get(String url) {
		Document response;
		try {
			ClientResource cr = createClient(url);
			StringWriter sw = new StringWriter();
			Log.REST.debug("GET REQUEST " + url);
			cr.get(MediaType.TEXT_XML).write(sw);
			response = DocumentHelper.parseText(sw.toString());
		} catch (Exception e) {
			throw NotFoundExceptionType.SERVICE_UNAVAILABLE.createException();
		}
		return response;
	}

	protected final void delete(String url) {
		ClientResource cr = createClient(url);
		Log.REST.debug("DELETE REQUEST " + url);
		cr.delete();
	}

	protected final String getGeoServerURL() {
		return GisProperties.getInstance().getGeoServerUrl();
	}

	protected final String getGeoServerWorkspace() {
		return GisProperties.getInstance().getGeoServerWorkspace();
	}

	protected final String getGeoServerAdminUser() {
		return GisProperties.getInstance().getGeoServerAdminUser();
	}

	protected final String getGeoServerAdminPassword() {
		return GisProperties.getInstance().getGeoServerAdminPassword();
	}
}

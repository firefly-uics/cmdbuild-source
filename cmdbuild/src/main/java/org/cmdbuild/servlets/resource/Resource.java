package org.cmdbuild.servlets.resource;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * Marker interface for restful resource objects
 */
public interface Resource {

	boolean match(String baseUri);
	String baseURI();
	void init(ServletContext ctxt,ServletConfig config);
}

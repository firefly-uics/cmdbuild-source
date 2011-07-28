package org.cmdbuild.servlets.resource;


public abstract class AbstractResource implements Resource {

	public boolean match(String baseUri) {
		return baseUri.equals(this.baseURI());
	}

}

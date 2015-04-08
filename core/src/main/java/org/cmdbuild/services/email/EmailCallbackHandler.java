package org.cmdbuild.services.email;

/**
 * Handler for {@link Email} reception.
 */
public interface EmailCallbackHandler {

	void handle(Email email);

}
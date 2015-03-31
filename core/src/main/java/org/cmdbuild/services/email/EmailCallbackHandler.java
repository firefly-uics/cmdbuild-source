package org.cmdbuild.services.email;

import org.cmdbuild.data.store.email.Email;

/**
 * Handler for {@link Email} reception.
 */
public interface EmailCallbackHandler {

	void handle(Email email);

}
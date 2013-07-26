package org.cmdbuild.services.email;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.model.email.Email;

public interface EmailPersistence {

	/**
	 * Creates a new {@link Email} and returns the stored one (so with
	 * {@code Id} also).
	 * 
	 * @param email
	 *            is the {@link Email} that needs to be created.
	 * 
	 * @return the created {@link Email}.
	 */
	Email create(Email email);

	CMCard getActivityCardFrom(String subject);

}

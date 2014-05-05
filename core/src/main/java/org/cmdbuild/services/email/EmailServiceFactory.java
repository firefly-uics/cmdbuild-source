package org.cmdbuild.services.email;

import org.cmdbuild.data.store.email.EmailAccount;

/**
 * {@link EmailService} factory class.
 */
public interface EmailServiceFactory {

	/**
	 * Creates a new {@link EmailService}.
	 * 
	 * @return the created {@link EmailService}.
	 */
	EmailService create();

	/**
	 * Creates a new {@link EmailService} with the specific {@link EmailAccount}
	 * .
	 * 
	 * @param account
	 * 
	 * @return the created {@link EmailService}.
	 */
	EmailService create(EmailAccount account);

}

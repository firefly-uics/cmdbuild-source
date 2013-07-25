package org.cmdbuild.services.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import org.cmdbuild.config.EmailConfiguration;

public class BasicAuthenticator extends Authenticator {

	private final EmailConfiguration configuration;

	public BasicAuthenticator(final EmailConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(configuration.getEmailUsername(), configuration.getEmailPassword());
	}

}

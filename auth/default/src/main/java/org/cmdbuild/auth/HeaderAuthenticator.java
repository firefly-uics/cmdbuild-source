package org.cmdbuild.auth;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticates a user based on the presence of a header parameter.
 * It can be used when a Single Sign-On proxy adds the header.
 */
public class HeaderAuthenticator implements ClientRequestAuthenticator {

	public interface Configuration {
		String getHeaderAttributeName();
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());

	final Configuration conf;

	/**
	 * @param userHeader Header that contains the user that should be logged in
	 */
	public HeaderAuthenticator(final Configuration conf) {
		Validate.notNull(conf);
		this.conf = conf;
	}

	@Override
	public Response authenticate(final ClientRequest request) {
		final String loginString = request.getHeader(conf.getHeaderAttributeName());
		if (loginString != null) {
			final Login login = Login.newInstance(loginString);
			logger.info("Authenticated user " + loginString);
			return Response.newLoginResponse(login);
		} else {
			return null;
		}
	}

}

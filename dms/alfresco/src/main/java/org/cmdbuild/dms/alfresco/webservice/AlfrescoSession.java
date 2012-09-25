package org.cmdbuild.dms.alfresco.webservice;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.alfresco.webservice.authentication.AuthenticationFault;
import org.alfresco.webservice.util.AuthenticationUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AlfrescoSession {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private final String username;
	private final String password;
	private boolean started;

	public AlfrescoSession(final String username, final String password) {
		Validate.isTrue(isNotBlank(username), format("invalid username '%s'", username));
		Validate.isTrue(isNotBlank(password), format("invalid username '%s'", password));
		this.username = username;
		this.password = password;
	}

	public synchronized void start() {
		final String ticket = AuthenticationUtils.getTicket();
		if (ticket == null) {
			try {
				AuthenticationUtils.startSession(username, password);
				started = true;
			} catch (final AuthenticationFault e) {
				logger.warn("error while connecting to Alfresco", e);
				started = false;
			}
		} else {
			started = true;
		}
	}

	public synchronized void end() {
		if (started) {
			AuthenticationUtils.endSession();
			started = false;
		}
	}

	public synchronized boolean isStarted() {
		return started;
	}

}

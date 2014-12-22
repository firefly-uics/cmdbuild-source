package org.cmdbuild.common.api.mail.javax.mail;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.IMAPS;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_DEBUG;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_IMAPS_HOST;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_IMAPS_PORT;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_IMAP_HOST;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_IMAP_PORT;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_IMAP_SOCKET_FACTORY_CLASS;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_STORE_PROTOCOL;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.NO_AUTENTICATION;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.SSL_FACTORY;
import static org.cmdbuild.common.api.mail.javax.mail.Utils.propertiesPlusSystemOnes;

import java.io.PrintStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.io.output.WriterOutputStream;
import org.cmdbuild.common.api.mail.Configuration;
import org.cmdbuild.common.api.mail.MailException;
import org.slf4j.Logger;

class InputTemplate {

	public static interface Hooks {

		void connected(Store store);

	}

	private final Configuration.Input configuration;
	private final Logger logger;
	private final PrintStream debugOutput;

	public InputTemplate(final Configuration.Input configuration) {
		this.configuration = configuration;
		this.logger = configuration.getLogger();
		this.debugOutput = new PrintStream(new WriterOutputStream(new LoggerWriter(logger)), true);
	}

	public void execute(final Hooks hooks) {
		Store store = null;
		try {
			final Session session = createSession();
			store = session.getStore();
			store.connect();
			hooks.connected(store);
			store.close();
		} catch (final MessagingException e) {
			logger.error("error while connecting/connected to store", e);
			throw MailException.input(e);
		} finally {
			if ((store != null) && store.isConnected()) {
				try {
					store.close();
				} catch (final MessagingException e) {
					logger.error("error while closing connection with store", e);
				}
			}
		}
	}

	private Session createSession() {
		final Properties imapProps = createConfigurationProperties();
		final Authenticator auth = getAutenticator();
		final Session session = Session.getInstance(imapProps, auth);
		session.setDebugOut(debugOutput);
		return session;
	}

	private Properties createConfigurationProperties() {
		final Properties properties = new Properties();
		properties.setProperty(MAIL_DEBUG, Boolean.toString(configuration.isDebug()));
		properties.setProperty(MAIL_STORE_PROTOCOL, configuration.getInputProtocol());
		if (sslRequired()) {
			properties.setProperty(MAIL_IMAPS_HOST, configuration.getInputHost());
			if (configuration.getInputPort() != null) {
				properties.setProperty(MAIL_IMAPS_PORT, configuration.getInputPort().toString());
			}
			properties.setProperty(MAIL_IMAP_SOCKET_FACTORY_CLASS, SSL_FACTORY);
		} else {
			properties.setProperty(MAIL_IMAP_HOST, configuration.getInputHost());
			if (configuration.getInputPort() != null) {
				properties.setProperty(MAIL_IMAP_PORT, configuration.getInputPort().toString());
			}
		}
		logger.trace("properties: {}", properties);
		return propertiesPlusSystemOnes(properties);
	}

	private boolean sslRequired() {
		return IMAPS.equals(configuration.getInputProtocol());
	}

	private boolean authenticationRequired() {
		return isNotBlank(configuration.getInputUsername());
	}

	private Authenticator getAutenticator() {
		return authenticationRequired() ? PasswordAuthenticator.from(configuration) : NO_AUTENTICATION;
	}

}

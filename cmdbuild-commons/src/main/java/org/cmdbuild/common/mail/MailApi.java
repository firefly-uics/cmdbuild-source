package org.cmdbuild.common.mail;

import java.util.List;

import org.slf4j.Logger;

/**
 * Mail API class.
 */
public interface MailApi {

	/**
	 * Common configuration interface for {@link MailApi}.
	 */
	public interface CommonConfiguration {

		/**
		 * Returns if the mail subsystem must be used in debug mode.
		 *
		 * @return {@code true} if debug mode is active, {@code false}
		 *         otherwise.
		 */
		boolean isDebug();

		/**
		 * Returns the logger that can be used.
		 *
		 * @return the {@link Logger}.
		 */
		Logger getLogger();

	}

	/**
	 * Output configuration interface for {@link MailApi}.
	 */
	public interface OutputConfiguration extends CommonConfiguration {

		/**
		 * Returns the protocol.
		 *
		 * Can be {@code"smtp"} or {@code "smtps"}.
		 *
		 * @return the protocol.
		 */
		String getOutputProtocol();

		/**
		 * Returns the host.
		 *
		 * @return the host.
		 */
		String getOutputHost();

		/**
		 * Returns the port.
		 *
		 * @return the port.
		 */
		Integer getOutputPort();

		/**
		 * Returns {@code true} if StartTLS is enabled, {@code false} otherwise.
		 *
		 * @return {@code true} if StartTLS is enabled, {@code false} otherwise.
		 */
		boolean isStartTlsEnabled();

		/**
		 * Returns the username.
		 *
		 * @return the username, can be {@code null}, empty or blank if
		 *         authentication is not required.
		 */
		String getOutputUsername();

		/**
		 * Returns the password.
		 *
		 * @return the password.
		 */
		String getOutputPassword();

		/**
		 * Returns the addresses of the sender.
		 *
		 * @return the addresses of the sender.
		 */
		List<String> getOutputFromRecipients();

	}

	/**
	 * Configuration interface for {@link MailApi}.
	 */
	public interface Configuration extends OutputConfiguration {

	}

	/**
	 * Creates a new mail.
	 *
	 * @return a new mail object
	 */
	public NewMail newMail();

}

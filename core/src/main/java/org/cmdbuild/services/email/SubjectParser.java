package org.cmdbuild.services.email;

/**
 * Parses a string that should be the mail's subject according with an expected
 * format.
 */
public interface SubjectParser {

	/**
	 * The result of the {@link SubjectParser.parse(String)} operation.
	 */
	interface ParsedSubject {

		/**
		 * Returns {@code true} if the subject has the expected format.
		 * 
		 * @return {@code true} if the subject has expected format,
		 *         {@code false} otherwise.
		 */
		boolean hasExpectedFormat();

		/**
		 * Returns the activity class name.
		 * 
		 * @return the activity class name.
		 */
		String getActivityClassName();

		/**
		 * Returns the activity id.
		 * 
		 * @return the activity id.
		 */
		Integer getActivityId();

		/**
		 * Returns the identifier of the notification e-mail.
		 * 
		 * @return the identifier of the notification e-mail.
		 */
		String getNotification();

		/**
		 * Returns the "real" subject.<br>
		 * <br>
		 * For example: from "{@code [foo 42] bar}" it returns "{@code bar}"
		 * (trimmed).
		 * 
		 * @return the "real" subject.
		 */
		String getRealSubject();

	}

	ParsedSubject parse(String subject);

}

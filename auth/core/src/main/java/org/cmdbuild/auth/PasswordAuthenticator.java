package org.cmdbuild.auth;

public interface PasswordAuthenticator {

	interface PasswordChanger {

		/**
		 * Change user password
		 * 
		 * @param login login informations
		 * @param oldPassword old password
		 * @param newPassword new password
		 */
		void changePassword(Login login, String oldPassword, String newPassword);
	}

	/**
	 * 
	 * @param login login informations
	 * @param password unencrypted password
	 * @return if the password mached
	 */
	boolean checkPassword(Login login, String password);

	/**
	 * 
	 * @param login
	 * @return the unencrypted password or null
	 */
	String fetchUnencryptedPassword(Login login);
}

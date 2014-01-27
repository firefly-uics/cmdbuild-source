package org.cmdbuild.logic.email;

public interface EmailAccountLogic {

	interface Account {

		Long getId();

		String getName();

		boolean isDefault();

		String getUsername();

		String getPassword();

		String getAddress();

		String getSmtpServer();

		Integer getSmtpPort();

		boolean isSmtpSsl();

		String getImapServer();

		Integer getImapPort();

		boolean isImapSsl();

		String getInputFolder();

		String getProcessedFolder();

		String getRejectedFolder();

		boolean isRejectNotMatching();

	}

	/**
	 * Creates the specified account.
	 * 
	 * @param account
	 *            is the {@link Account} that needs to be created.
	 * 
	 * @return the created {@link Account}.
	 * 
	 * @throws RuntimeException
	 *             if something goes wrong.
	 */

	Account createAccount(Account account);

	/**
	 * Updates the specified account.
	 * 
	 * @param account
	 *            is the {@link Account} that needs to be updated.
	 * 
	 * @return the updated {@link Account}.
	 * 
	 * @throws RuntimeException
	 *             if something goes wrong.
	 */
	Account updateAccount(Account account);

	/**
	 * Gets all available accounts.
	 * 
	 * @return all {@link Account}s.
	 */
	Iterable<Account> getAllAccounts();

	/**
	 * Gets the {@link Account} for the specified identifier.
	 * 
	 * @param id
	 * 
	 * @return the {@link Account} for the specified id.
	 * 
	 * @throws RuntimeException
	 *             if the identifier is not found.
	 */
	Account getAccount(Long id);

	/**
	 * Deletes the {@link Account} for the specified identifier.
	 * 
	 * @param id
	 * 
	 * @throws RuntimeException
	 *             if the identifier is not found.
	 */
	void deleteAccount(Long id);

}

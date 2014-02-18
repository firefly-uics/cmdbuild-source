package org.cmdbuild.servlets.json.schema;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.servlets.json.ComunicationConstants.ADDRESS;
import static org.cmdbuild.servlets.json.ComunicationConstants.ELEMENTS;
import static org.cmdbuild.servlets.json.ComunicationConstants.ENABLE_MOVE_REJECTED_NOT_MATCHING;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.IMAP_PORT;
import static org.cmdbuild.servlets.json.ComunicationConstants.IMAP_SERVER;
import static org.cmdbuild.servlets.json.ComunicationConstants.IMAP_SSL;
import static org.cmdbuild.servlets.json.ComunicationConstants.INCOMING_FOLDER;
import static org.cmdbuild.servlets.json.ComunicationConstants.IS_DEFAULT;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.PASSWORD;
import static org.cmdbuild.servlets.json.ComunicationConstants.PROCESSED_FOLDER;
import static org.cmdbuild.servlets.json.ComunicationConstants.REJECTED_FOLDER;
import static org.cmdbuild.servlets.json.ComunicationConstants.REJECT_NOT_MATCHING;
import static org.cmdbuild.servlets.json.ComunicationConstants.SMTP_PORT;
import static org.cmdbuild.servlets.json.ComunicationConstants.SMTP_SERVER;
import static org.cmdbuild.servlets.json.ComunicationConstants.SMTP_SSL;
import static org.cmdbuild.servlets.json.ComunicationConstants.USER_NAME;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.logic.email.EmailAccountLogic.Account;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class EmailAccount extends JSONBaseWithSpringContext {

	private static class AccountDetails implements Account {

		private Long id;
		private String name;
		private boolean isDefault;
		private String username;
		private String password;
		private String address;
		private String smtpServer;
		private Integer smtpPort;
		private boolean smtpSsl;
		private String imapServer;
		private Integer imapPort;
		private boolean imapSsl;
		private String incomingFolder;
		private String processedFolder;
		private String rejectedFolder;
		private boolean isRejectNotMatching;

		@Override
		@JsonProperty(ID)
		public Long getId() {
			return id;
		}

		public void setId(final Long id) {
			this.id = id;
		}

		@Override
		@JsonProperty(NAME)
		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@Override
		@JsonProperty(IS_DEFAULT)
		public boolean isDefault() {
			return isDefault;
		}

		public void setDefault(final Boolean isDefault) {
			this.isDefault = (isDefault == null) ? false : isDefault;
		}

		@Override
		@JsonProperty(ADDRESS)
		public String getAddress() {
			return address;
		}

		public void setAddress(final String address) {
			this.address = address;
		}

		@Override
		@JsonProperty(USER_NAME)
		public String getUsername() {
			return username;
		}

		public void setUsername(final String username) {
			this.username = username;
		}

		@Override
		@JsonProperty(PASSWORD)
		public String getPassword() {
			return password;
		}

		public void setPassword(final String password) {
			this.password = password;
		}

		@Override
		@JsonProperty(SMTP_SERVER)
		public String getSmtpServer() {
			return smtpServer;
		}

		public void setSmtpServer(final String smtpServer) {
			this.smtpServer = smtpServer;
		}

		@Override
		@JsonProperty(SMTP_PORT)
		public Integer getSmtpPort() {
			return smtpPort;
		}

		public void setSmtpPort(final Integer smtpPort) {
			this.smtpPort = smtpPort;
		}

		@Override
		@JsonProperty(SMTP_SSL)
		public boolean isSmtpSsl() {
			return smtpSsl;
		}

		public void setSmtpSsl(final Boolean smtpSsl) {
			this.smtpSsl = (smtpSsl == null) ? false : smtpSsl;
		}

		@Override
		@JsonProperty(IMAP_SERVER)
		public String getImapServer() {
			return imapServer;
		}

		public void setImapServer(final String imapServer) {
			this.imapServer = imapServer;
		}

		@Override
		@JsonProperty(IMAP_PORT)
		public Integer getImapPort() {
			return imapPort;
		}

		public void setImapPort(final Integer imapPort) {
			this.imapPort = imapPort;
		}

		@Override
		@JsonProperty(IMAP_SSL)
		public boolean isImapSsl() {
			return imapSsl;
		}

		public void setImapSsl(final Boolean imapSsl) {
			this.imapSsl = (imapSsl == null) ? false : imapSsl;
		}

		@Override
		@JsonProperty(INCOMING_FOLDER)
		public String getInputFolder() {
			return incomingFolder;
		}

		public void setInputFolder(final String incomingFolder) {
			this.incomingFolder = incomingFolder;
		}

		@Override
		@JsonProperty(PROCESSED_FOLDER)
		public String getProcessedFolder() {
			return processedFolder;
		}

		public void setProcessedFolder(final String processedFolder) {
			this.processedFolder = processedFolder;
		}

		@Override
		@JsonProperty(REJECTED_FOLDER)
		public String getRejectedFolder() {
			return rejectedFolder;
		}

		public void setRejectedFolder(final String rejectedFolder) {
			this.rejectedFolder = rejectedFolder;
		}

		@Override
		@JsonProperty(ENABLE_MOVE_REJECTED_NOT_MATCHING)
		public boolean isRejectNotMatching() {
			return isRejectNotMatching;
		}

		public void setRejectedNotMatching(final Boolean isRejectNotMatching) {
			this.isRejectNotMatching = (isRejectNotMatching == null) ? false : isRejectNotMatching;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class Accounts {

		private List<? super AccountDetails> elements;

		@JsonProperty(ELEMENTS)
		public List<? super AccountDetails> getElements() {
			return elements;
		}

		public void setElements(final Iterable<? extends AccountDetails> elements) {
			this.elements = Lists.newArrayList(elements);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static Function<Account, AccountDetails> ACCOUNT_TO_ACCOUNT_DETAILS = new Function<Account, AccountDetails>() {

		@Override
		public AccountDetails apply(final Account input) {
			return new AccountDetails() {
				{
					setId(input.getId());
					setName(input.getName());
					setDefault(input.isDefault());
					setUsername(input.getUsername());
					setPassword(input.getPassword());
					setAddress(input.getAddress());
					setSmtpServer(input.getSmtpServer());
					setSmtpPort(input.getSmtpPort());
					setSmtpSsl(input.isSmtpSsl());
					setImapServer(input.getImapServer());
					setImapPort(input.getImapPort());
					setImapSsl(input.isImapSsl());
					setInputFolder(input.getInputFolder());
					setProcessedFolder(input.getProcessedFolder());
					setRejectedFolder(input.getRejectedFolder());
					setRejectedNotMatching(input.isRejectNotMatching());
				}
			};
		}

	};

	@JSONExported
	@Admin
	public JsonResponse delete( //
			@Parameter(ID) final Long id //
	) throws JSONException {
		emailAccountLogic().deleteAccount(id);

		return JsonResponse.success();
	}

	@JSONExported
	@Admin
	public JsonResponse get( //
			@Parameter(ID) final Long id //
	) throws JSONException {
		final Account emailAccount = emailAccountLogic().getAccount(id);

		final AccountDetails element = ACCOUNT_TO_ACCOUNT_DETAILS.apply(emailAccount);

		return JsonResponse.success(element);
	}

	@JSONExported
	@Admin
	public JsonResponse getAll() throws JSONException {
		final Iterable<Account> emailAccounts = emailAccountLogic().getAllAccounts();

		final Iterable<AccountDetails> elements = from(emailAccounts) //
				.transform(ACCOUNT_TO_ACCOUNT_DETAILS);
		final Accounts accounts = new Accounts();
		accounts.setElements(elements);

		return JsonResponse.success(accounts);
	}

	@JSONExported
	@Admin
	public JsonResponse post( //
			@Parameter(NAME) final String name, //
			@Parameter(IS_DEFAULT) final Boolean isDefault, //
			@Parameter(USER_NAME) final String username, //
			@Parameter(PASSWORD) final String password, //
			@Parameter(ADDRESS) final String address, //
			@Parameter(SMTP_SERVER) final String smtpServer, //
			@Parameter(SMTP_PORT) final Integer smtpPort, //
			@Parameter(SMTP_SSL) final Boolean smtpSsl, //
			@Parameter(IMAP_SERVER) final String imapServer, //
			@Parameter(IMAP_PORT) final Integer imapPort, //
			@Parameter(IMAP_SSL) final Boolean imapSsl, //
			@Parameter(INCOMING_FOLDER) final String incomingFolder, //
			@Parameter(PROCESSED_FOLDER) final String processedFolder, //
			@Parameter(REJECTED_FOLDER) final String rejectedFolder, //
			@Parameter(REJECT_NOT_MATCHING) final boolean rejectNotMatching //
	) throws JSONException {
		final AccountDetails accountDetails = new AccountDetails() {
			{
				setName(name);
				setDefault(isDefault);
				setUsername(username);
				setPassword(password);
				setAddress(address);
				setSmtpServer(smtpServer);
				setSmtpPort(smtpPort);
				setSmtpSsl(smtpSsl);
				setImapServer(imapServer);
				setImapPort(imapPort);
				setImapSsl(imapSsl);
				setInputFolder(incomingFolder);
				setProcessedFolder(processedFolder);
				setRejectedFolder(rejectedFolder);
				setRejectedNotMatching(rejectNotMatching);
			}
		};

		final Account emailAccounts = emailAccountLogic().createAccount(accountDetails);

		final AccountDetails element = ACCOUNT_TO_ACCOUNT_DETAILS.apply(emailAccounts);

		return JsonResponse.success(element);
	}

	@JSONExported
	@Admin
	public JsonResponse put( //
			@Parameter(ID) final Long id, //
			@Parameter(NAME) final String name, //
			@Parameter(IS_DEFAULT) final Boolean isDefault, //
			@Parameter(USER_NAME) final String username, //
			@Parameter(PASSWORD) final String password, //
			@Parameter(ADDRESS) final String address, //
			@Parameter(SMTP_SERVER) final String smtpServer, //
			@Parameter(SMTP_PORT) final Integer smtpPort, //
			@Parameter(SMTP_SSL) final Boolean smtpSsl, //
			@Parameter(IMAP_SERVER) final String imapServer, //
			@Parameter(IMAP_PORT) final Integer imapPort, //
			@Parameter(IMAP_SSL) final Boolean imapSsl, //
			@Parameter(INCOMING_FOLDER) final String incomingFolder, //
			@Parameter(PROCESSED_FOLDER) final String processedFolder, //
			@Parameter(REJECTED_FOLDER) final String rejectedFolder, //
			@Parameter(REJECT_NOT_MATCHING) final boolean rejectNotMatching //
	) throws JSONException {
		final AccountDetails accountDetails = new AccountDetails() {
			{
				setId(id);
				setName(name);
				setDefault(isDefault);
				setUsername(username);
				setPassword(password);
				setAddress(address);
				setSmtpServer(smtpServer);
				setSmtpPort(smtpPort);
				setSmtpSsl(smtpSsl);
				setImapServer(imapServer);
				setImapPort(imapPort);
				setImapSsl(imapSsl);
				setInputFolder(incomingFolder);
				setProcessedFolder(processedFolder);
				setRejectedFolder(rejectedFolder);
				setRejectedNotMatching(rejectNotMatching);
			}
		};

		final Account emailAccounts = emailAccountLogic().updateAccount(accountDetails);

		final AccountDetails element = ACCOUNT_TO_ACCOUNT_DETAILS.apply(emailAccounts);

		return JsonResponse.success(element);
	}

}
package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.ComunicationConstants.*;

import java.util.List;
import java.util.Map;

import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONException;

import com.google.common.collect.Lists;

public class Email extends JSONBaseWithSpringContext {

	private static class AccountShortDatas {
		private String address;
		private long id;
		private boolean isActive;
		private boolean isDefault;
		private String name;

		@JsonProperty(ID)
		public long getId() {
			return id;
		}

		public void setId(final long id) {
			this.id = id;
		}

		@JsonProperty(NAME)
		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@JsonProperty(IS_DEFAULT)
		public boolean isDefault() {
			return isDefault;
		}

		public void setDefault(final boolean isDefault) {
			this.isDefault = isDefault;
		}

		@JsonProperty(IS_ACTIVE)
		public boolean isActive() {
			return isActive;
		}

		public void setActive(final boolean isActive) {
			this.isActive = isActive;
		}

		@JsonProperty(ADDRESS)
		public String getAddress() {
			return address;
		}

		public void setAddress(final String address) {
			this.address = address;
		}
	}

	private static class AccountDetails extends AccountShortDatas{
		private boolean enableMoveRejectedNotMatching;
		private long imapPort;
		private String imapServer;
		private boolean imapSsl;
		private String incomingFolder;
		private String password;
		private String processedFolder;
		private String rejectedFolder;
		private long smtpPort;
		private String smtpServer;
		private boolean smtpSsl;
		private String username;

		@JsonProperty(USER_NAME)
		public String getUsername() {
			return username;
		}

		public void setUsername(final String username) {
			this.username = username;
		}

		@JsonProperty(PASSWORD)
		public String getPassword() {
			return password;
		}

		public void setPassword(final String password) {
			this.password = password;
		}

		@JsonProperty(SMTP_SERVER)
		public String getSmtpServer() {
			return smtpServer;
		}

		public void setSmtpServer(final String smtpServer) {
			this.smtpServer = smtpServer;
		}

		@JsonProperty(SMTP_PORT)
		public long getSmtpPort() {
			return smtpPort;
		}

		public void setSmtpPort(final long smtpPort) {
			this.smtpPort = smtpPort;
		}

		@JsonProperty(SMTP_SSL)
		public boolean getSmtpSsl() {
			return smtpSsl;
		}

		public void setSmtpSsl(final boolean smtpSsl) {
			this.smtpSsl = smtpSsl;
		}

		@JsonProperty(IMAP_SERVER)
		public String getImapServer() {
			return imapServer;
		}

		public void setImapServer(final String imapServer) {
			this.imapServer = imapServer;
		}

		@JsonProperty(IMAP_PORT)
		public long getImapPort() {
			return imapPort;
		}

		public void setImapPort(final long imapPort) {
			this.imapPort = imapPort;
		}

		@JsonProperty(IMAP_SSL)
		public boolean getImapSsl() {
			return imapSsl;
		}

		public void setImapSsl(final boolean imapSsl) {
			this.imapSsl = imapSsl;
		}

		@JsonProperty(INCOMING_FOLDER)
		public String getIncomingFolder() {
			return incomingFolder;
		}

		public void setIncomingFolder(final String incomingFolder) {
			this.incomingFolder = incomingFolder;
		}

		@JsonProperty(PROCESSED_FOLDER)
		public String getProcessedFolder() {
			return processedFolder;
		}

		public void setProcessedFolder(final String processedFolder) {
			this.processedFolder = processedFolder;
		}

		@JsonProperty(REJECTED_FOLDER)
		public String getRejectedFolder() {
			return rejectedFolder;
		}

		public void setRejectedFolder(final String rejectedFolder) {
			this.rejectedFolder = rejectedFolder;
		}

		@JsonProperty(ENABLE_MOVE_REJECTED_NOT_MATCHING)
		public boolean getEnableMoveRejectedNotMatching() {
			return enableMoveRejectedNotMatching;
		}

		public void setEnableMoveRejectedNotMatching(final boolean enableMoveRejectedNotMatching) {
			this.enableMoveRejectedNotMatching = enableMoveRejectedNotMatching;
		}
	}

	private static class Accounts {

		private List<? super AccountShortDatas> elements;

		@JsonProperty(ELEMENTS)
		public List<? super AccountShortDatas> getElements() {
			return elements;
		}

		public void setElements(List<? super AccountShortDatas> elements) {
			this.elements = elements;
		}

	}

	@JSONExported
	@Admin
	public JsonResponse delete( //
			@Parameter(ID) final Long id //
	) throws JSONException {
		// TODO: real implementation

		return null;
	}

	@JSONExported
	@Admin
	public JsonResponse get( //
			@Parameter(ID) final Long id //
	) throws JSONException {
		// TODO: real implementation using account id to retun just single account's datas only

		final Accounts accounts = new Accounts();
		final List<? super AccountShortDatas> elements = Lists.newArrayList();
		elements.add(new AccountDetails() {
			{
				setId(69);
				setName("Email Account 69");
				setDefault(true);
				setActive(true);
				setUsername("username1");
				setPassword("password1");
				setAddress("email1@tecnoteca.com");
				setSmtpServer("path/to/smtp/server69/");
				setSmtpPort(1234);
				setSmtpSsl(true);
				setImapServer("path/to/imap/server69/");
				setImapPort(4321);
				setImapSsl(true);
				setIncomingFolder("path/to/incoming/folder69/");
				setProcessedFolder("path/to/processed/folder69/");
				setRejectedFolder("path/to/rejected/folder69/");
				setEnableMoveRejectedNotMatching(true);
			}
		});
		accounts.setElements(elements);

		return JsonResponse.success(accounts);
	}

	@JSONExported
	@Admin
	public JsonResponse getAllAccounts() throws JSONException {
		// TODO: real implementation

		final Accounts accounts = new Accounts();
		final List<? super AccountShortDatas> elements = Lists.newArrayList();
		elements.add(new AccountShortDatas() {
			{
				setId(1);
				setName("Email Account 1");
				setDefault(true);
				setActive(true);
				setAddress("email1@tecnoteca.com");
			}
		});
		elements.add(new AccountShortDatas() {
			{
				setId(2);
				setName("Email Account 2");
				setDefault(false);
				setActive(false);
				setAddress("email2@tecnoteca.com");
			}
		});
		elements.add(new AccountShortDatas() {
			{
				setId(6969);
				setName("Email Account3");
				setDefault(false);
				setActive(false);
				setAddress("email3@tecnoteca.com");
			}
		});
		accounts.setElements(elements);

		return JsonResponse.success(accounts);
	}

	@JSONExported
	@Admin
	public JsonResponse post( //
			final Map<String, Object> attributes
	) throws JSONException {
		// TODO: real implementation

		final Accounts accounts = new Accounts();
		final List<? super AccountShortDatas> elements = Lists.newArrayList();
		elements.add(new AccountDetails() {
			{
				setId(6969);
				setName("Email Account 6969");
				setDefault(true);
				setActive(true);
				setUsername("username1");
				setPassword("password1");
				setAddress("email1@tecnoteca.com");
				setSmtpServer("path/to/smtp/server6969/");
				setSmtpPort(1234);
				setSmtpSsl(true);
				setImapServer("path/to/imap/server69/");
				setImapPort(4321);
				setImapSsl(true);
				setIncomingFolder("path/to/incoming/folder6969/");
				setProcessedFolder("path/to/processed/folder6969/");
				setRejectedFolder("path/to/rejected/folder6969/");
				setEnableMoveRejectedNotMatching(true);
			}
		});
		accounts.setElements(elements);

		return JsonResponse.success(accounts);
	}

	@JSONExported
	@Admin
	public JsonResponse put( //
			@Parameter(ID) final Long id, //
			final Map<String, Object> attributes
	) throws JSONException {
		// TODO: real implementation

		final Accounts accounts = new Accounts();
		final List<? super AccountShortDatas> elements = Lists.newArrayList();
		elements.add(new AccountDetails() {
			{
				setId(2);
				setName("Email Account 6969");
				setDefault(true);
				setActive(true);
				setUsername("username1");
				setPassword("password1");
				setAddress("email1@tecnoteca.com");
				setSmtpServer("path/to/smtp/server6969/");
				setSmtpPort(1234);
				setSmtpSsl(true);
				setImapServer("path/to/imap/server69/");
				setImapPort(4321);
				setImapSsl(true);
				setIncomingFolder("path/to/incoming/folder6969/");
				setProcessedFolder("path/to/processed/folder6969/");
				setRejectedFolder("path/to/rejected/folder6969/");
				setEnableMoveRejectedNotMatching(true);
			}
		});
		accounts.setElements(elements);

		return JsonResponse.success(accounts);
	}
}
package org.cmdbuild.data.store.email;

import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;

import com.google.common.collect.Maps;

public class EmailAccountStorableConverter extends BaseStorableConverter<EmailAccount> {

	private static final String EMAIL_ACCOUNT = "_EmailAccount";

	private static final String IS_DEFAULT = "IsDefault";
	private static final String CODE = Constants.CODE_ATTRIBUTE;
	private static final String ADDRESS = "Address";
	private static final String USERNAME = "Username";
	private static final String PASSWORD = "Password";
	private static final String SMTP_SERVER = "SmtpServer";
	private static final String SMTP_PORT = "SmtpPort";
	private static final String SMTP_SSL = "SmtpSsl";
	private static final String IMAP_SERVER = "ImapServer";
	private static final String IMAP_PORT = "ImapPort";
	private static final String IMAP_SSL = "ImapSsl";
	private static final String INPUT_FOLDER = "InputFolder";
	private static final String PROCESSED_FOLDER = "ProcessedFolder";
	private static final String REJECTED_FOLDER = "RejectedFolder";
	private static final String REJECT_NOT_MATCHING = "RejectNotMatching";

	@Override
	public String getClassName() {
		return EMAIL_ACCOUNT;
	}

	@Override
	public EmailAccount convert(final CMCard card) {
		final EmailAccount emailAccount = new EmailAccount(card.getId());
		emailAccount.setDefault(card.get(IS_DEFAULT, Boolean.class));
		emailAccount.setName(card.get(CODE, String.class));
		emailAccount.setAddress(card.get(ADDRESS, String.class));
		emailAccount.setUsername(card.get(USERNAME, String.class));
		emailAccount.setPassword(card.get(PASSWORD, String.class));
		emailAccount.setSmtpServer(card.get(SMTP_SERVER, String.class));
		emailAccount.setSmtpPort(card.get(SMTP_PORT, Integer.class));
		emailAccount.setSmtpSsl(card.get(SMTP_SSL, Boolean.class));
		emailAccount.setImapServer(card.get(IMAP_SERVER, String.class));
		emailAccount.setImapPort(card.get(IMAP_PORT, Integer.class));
		emailAccount.setImapSsl(card.get(IMAP_SSL, Boolean.class));
		emailAccount.setInputFolder(card.get(INPUT_FOLDER, String.class));
		emailAccount.setProcessedFolder(card.get(PROCESSED_FOLDER, String.class));
		emailAccount.setRejectedFolder(card.get(REJECTED_FOLDER, String.class));
		emailAccount.setRejectNotMatching(card.get(REJECT_NOT_MATCHING, Boolean.class));
		return emailAccount;
	}

	@Override
	public Map<String, Object> getValues(final EmailAccount emailAccount) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(IS_DEFAULT, emailAccount.isDefault());
		values.put(CODE, emailAccount.getName());
		values.put(ADDRESS, emailAccount.getAddress());
		values.put(USERNAME, emailAccount.getUsername());
		values.put(PASSWORD, emailAccount.getPassword());
		values.put(SMTP_SERVER, emailAccount.getSmtpServer());
		values.put(SMTP_PORT, emailAccount.getSmtpPort());
		values.put(SMTP_SSL, emailAccount.isSmtpSsl());
		values.put(IMAP_SERVER, emailAccount.getImapServer());
		values.put(IMAP_PORT, emailAccount.getImapPort());
		values.put(IMAP_SSL, emailAccount.isImapSsl());
		values.put(INPUT_FOLDER, emailAccount.getInputFolder());
		values.put(PROCESSED_FOLDER, emailAccount.getProcessedFolder());
		values.put(REJECTED_FOLDER, emailAccount.getRejectedFolder());
		values.put(REJECT_NOT_MATCHING, emailAccount.isRejectNotMatching());
		return values;
	}

}

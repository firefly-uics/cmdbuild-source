package org.cmdbuild.logic.setup;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultString;

import java.util.Map;

import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.logic.setup.SetUpLogic.Module;

import com.google.common.collect.Maps;

public class EmailModule implements Module {

	// TODO make it all private
	public static final String EMAIL_ADDRESS = "email.address";
	public static final String SMTP_SERVER = "email.smtp.server";
	public static final String SMTP_PORT = "email.smtp.port";
	public static final String SMTP_SSL = "email.smtp.ssl";
	public static final String IMAP_SERVER = "email.imap.server";
	public static final String IMAP_PORT = "email.imap.port";
	public static final String IMAP_SSL = "email.imap.ssl";
	public static final String EMAIL_USERNAME = "email.username";
	public static final String EMAIL_PASSWORD = "email.password";

	private final Store<EmailAccount> store;

	public EmailModule(final Store<EmailAccount> store) {
		this.store = store;
	}

	@Override
	public Map<String, String> retrieve() throws Exception {
		final Map<String, String> values = Maps.newHashMap();
		EmailAccount emailAccount = new EmailAccount();
		for (final EmailAccount account : store.list()) {
			if (account.isDefault()) {
				emailAccount = account;
			}
		}
		values.put(EMAIL_ADDRESS, defaultString(emailAccount.getAddress()));
		values.put(EMAIL_USERNAME, defaultString(emailAccount.getUsername()));
		values.put(EMAIL_PASSWORD, defaultString(emailAccount.getPassword()));
		values.put(EMAIL_PASSWORD, defaultString(emailAccount.getPassword()));
		values.put(SMTP_SERVER, defaultString(emailAccount.getSmtpServer()));
		values.put(SMTP_PORT, defaultInt(emailAccount.getSmtpPort()));
		values.put(SMTP_SSL, defaultBoolean(emailAccount.isSmtpSsl()));
		values.put(IMAP_SERVER, defaultString(emailAccount.getImapServer()));
		values.put(IMAP_PORT, defaultInt(emailAccount.getImapPort()));
		values.put(IMAP_SSL, defaultBoolean(emailAccount.isImapSsl()));
		return values;
	}

	private String defaultInt(final Integer value) {
		return (value == null) ? EMPTY : value.toString();
	}

	private String defaultBoolean(final Boolean value) {
		return (value == null) ? EMPTY : value.toString();
	}

	@Override
	public void store(final Map<String, String> values) throws Exception {
		EmailAccount emailAccount = null;
		for (final EmailAccount account : store.list()) {
			if (account.isDefault()) {
				emailAccount = account;
			}
		}
		final boolean exists;
		if (emailAccount == null) {
			emailAccount = new EmailAccount();
			exists = false;
		} else {
			exists = true;
		}
		emailAccount.setDefault(true);
		emailAccount.setName("default");
		emailAccount.setAddress(values.get(EMAIL_ADDRESS));
		emailAccount.setUsername(values.get(EMAIL_USERNAME));
		emailAccount.setPassword(values.get(EMAIL_PASSWORD));
		emailAccount.setPassword(values.get(EMAIL_PASSWORD));
		emailAccount.setSmtpServer(values.get(SMTP_SERVER));
		emailAccount.setSmtpPort(parseInt(values.get(SMTP_PORT)));
		emailAccount.setSmtpSsl(Boolean.parseBoolean(values.get(SMTP_SSL)));
		emailAccount.setImapServer(values.get(IMAP_SERVER));
		emailAccount.setImapPort(parseInt(values.get(IMAP_PORT)));
		emailAccount.setImapSsl(Boolean.parseBoolean(values.get(IMAP_SSL)));
		emailAccount.setInputFolder("Inbox");
		emailAccount.setProcessedFolder("Processed");
		emailAccount.setRejectedFolder("Rejected");
		if (exists) {
			store.update(emailAccount);
		} else {
			store.create(emailAccount);
		}
	};

	private Integer parseInt(final String value) {
		try {
			return Integer.parseInt(value);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

}

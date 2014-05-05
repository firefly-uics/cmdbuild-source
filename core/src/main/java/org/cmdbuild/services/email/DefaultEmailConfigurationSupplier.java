package org.cmdbuild.services.email;

import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

import com.google.common.base.Supplier;

public class DefaultEmailConfigurationSupplier implements Supplier<EmailConfiguration> {

	private static final Logger logger = Log.EMAIL;

	private final Store<EmailAccount> store;

	public DefaultEmailConfigurationSupplier(final Store<EmailAccount> store) {
		this.store = store;
	}

	@Override
	public EmailConfiguration get() {
		logger.debug("getting default email account");
		for (final EmailAccount emailAccount : store.list()) {
			if (emailAccount.isDefault()) {
				return EmailAccountWrapper.of(emailAccount);
			}
		}
		throw new IllegalArgumentException("default account not found");
	}

}

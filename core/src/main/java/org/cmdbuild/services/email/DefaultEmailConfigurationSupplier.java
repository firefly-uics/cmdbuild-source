package org.cmdbuild.services.email;

import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.StorableEmailAccount;
import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

import com.google.common.base.Supplier;

public class DefaultEmailConfigurationSupplier implements Supplier<StorableEmailAccount> {

	private static final Logger logger = Log.EMAIL;

	private final Store<StorableEmailAccount> store;

	public DefaultEmailConfigurationSupplier(final Store<StorableEmailAccount> store) {
		this.store = store;
	}

	@Override
	public StorableEmailAccount get() {
		logger.debug("getting default email account");
		for (final StorableEmailAccount emailAccount : store.list()) {
			if (emailAccount.isDefault()) {
				return emailAccount;
			}
		}
		throw new IllegalArgumentException("default account not found");
	}

}

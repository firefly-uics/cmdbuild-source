package org.cmdbuild.services.email;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.StorableEmailAccount;
import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class PredicateEmailAccountSupplier implements Supplier<EmailAccount> {

	private static final Logger logger = Log.EMAIL;

	public static PredicateEmailAccountSupplier of(final Store<StorableEmailAccount> store,
			final Predicate<EmailAccount> predicate) {
		return new PredicateEmailAccountSupplier(store, predicate);
	}

	private final Store<StorableEmailAccount> store;
	private final Predicate<EmailAccount> predicate;

	private PredicateEmailAccountSupplier(final Store<StorableEmailAccount> store,
			final Predicate<EmailAccount> predicate) {
		this.store = store;
		this.predicate = predicate;
	}

	@Override
	public EmailAccount get() {
		logger.debug("getting default email account");
		return from(store.list()) //
				.filter(EmailAccount.class) //
				.filter(predicate) //
				.first() //
				.get();
	}

}

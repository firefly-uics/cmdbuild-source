package org.cmdbuild.logic.email;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.data.store.StorableUtils.storableById;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;

import com.google.common.base.Function;

public class DefaultEmailAccountLogic implements EmailAccountLogic {

	private static class ForwardingAccount implements Account {

		private final Account delegate;

		public ForwardingAccount(final Account delegate) {
			this.delegate = delegate;
		}

		@Override
		public Long getId() {
			return delegate.getId();
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public boolean isDefault() {
			return delegate.isDefault();
		}

		@Override
		public String getUsername() {
			return delegate.getUsername();
		}

		@Override
		public String getPassword() {
			return delegate.getPassword();
		}

		@Override
		public String getAddress() {
			return delegate.getAddress();
		}

		@Override
		public String getSmtpServer() {
			return delegate.getSmtpServer();
		}

		@Override
		public Integer getSmtpPort() {
			return delegate.getSmtpPort();
		}

		@Override
		public boolean isSmtpSsl() {
			return delegate.isSmtpSsl();
		}

		@Override
		public String getImapServer() {
			return delegate.getImapServer();
		}

		@Override
		public Integer getImapPort() {
			return delegate.getImapPort();
		}

		@Override
		public boolean isImapSsl() {
			return delegate.isImapSsl();
		}

		@Override
		public String getInputFolder() {
			return delegate.getInputFolder();
		}

		@Override
		public String getProcessedFolder() {
			return delegate.getProcessedFolder();
		}

		@Override
		public String getRejectedFolder() {
			return delegate.getRejectedFolder();
		}

		@Override
		public boolean isRejectNotMatching() {
			return delegate.isRejectNotMatching();
		}

	}

	private static class AlwaysDefault extends ForwardingAccount {

		public static AlwaysDefault of(final Account account) {
			return new AlwaysDefault(account);
		}

		public AlwaysDefault(final Account delegate) {
			super(delegate);
		}

		@Override
		public boolean isDefault() {
			return true;
		}

	}

	private static class AccountWrapper implements Account {

		private final EmailAccount delegate;

		public AccountWrapper(final EmailAccount delegate) {
			this.delegate = delegate;
		}

		@Override
		public Long getId() {
			return Long.valueOf(delegate.getIdentifier());
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public boolean isDefault() {
			return delegate.isDefault();
		}

		@Override
		public String getUsername() {
			return delegate.getUsername();
		}

		@Override
		public String getPassword() {
			return delegate.getPassword();
		}

		@Override
		public String getAddress() {
			return delegate.getAddress();
		}

		@Override
		public String getSmtpServer() {
			return delegate.getSmtpServer();
		}

		@Override
		public Integer getSmtpPort() {
			return delegate.getSmtpPort();
		}

		@Override
		public boolean isSmtpSsl() {
			return delegate.isSmtpSsl();
		}

		@Override
		public String getImapServer() {
			return delegate.getImapServer();
		}

		@Override
		public Integer getImapPort() {
			return delegate.getImapPort();
		}

		@Override
		public boolean isImapSsl() {
			return delegate.isImapSsl();
		}

		@Override
		public String getInputFolder() {
			return delegate.getInputFolder();
		}

		@Override
		public String getProcessedFolder() {
			return delegate.getProcessedFolder();
		}

		@Override
		public String getRejectedFolder() {
			return delegate.getRejectedFolder();
		}

		@Override
		public boolean isRejectNotMatching() {
			return delegate.isRejectNotMatching();
		}

	}

	private static final Function<EmailAccount, Account> EMAIL_ACCOUNT_TO_ACCOUNT = new Function<org.cmdbuild.data.store.email.EmailAccount, EmailAccountLogic.Account>() {

		@Override
		public Account apply(final EmailAccount input) {
			return new AccountWrapper(input);
		};

	};

	private static final Function<Account, EmailAccount> ACCOUNT_TO_EMAIL_ACCOUNT = new Function<Account, org.cmdbuild.data.store.email.EmailAccount>() {

		@Override
		public EmailAccount apply(final Account input) {
			return EmailAccount.newInstance() //
					.withId(input.getId()) //
					.withDefaultStatus(input.isDefault()) //
					.withName(input.getName()) //
					.withAddress(input.getAddress()) //
					.withUsername(input.getUsername()) //
					.withPassword(input.getPassword()) //
					.withSmtpServer(input.getSmtpServer()) //
					.withSmtpPort(input.getSmtpPort()) //
					.withSmtpSsl(input.isSmtpSsl()) //
					.withImapServer(input.getImapServer()) //
					.withImapPort(input.getImapPort()) //
					.withImapSsl(input.isImapSsl()) //
					.withInputFolder(input.getInputFolder()) //
					.withProcessedFolder(input.getProcessedFolder()) //
					.withRejectedFolder(input.getRejectedFolder()) //
					.withRejectNotMatchingStatus(input.isRejectNotMatching()) //
					.build();
		};

	};

	private final Store<org.cmdbuild.data.store.email.EmailAccount> store;

	public DefaultEmailAccountLogic( //
			final Store<org.cmdbuild.data.store.email.EmailAccount> store //
	) {
		this.store = store;
	}

	@Override
	public Account createAccount(final Account account) {
		Validate.isTrue(account.getId() == null, "cannot create an account with an id");
		for (final EmailAccount element : store.list()) {
			Validate.isTrue(!element.getName().equals(account.getName()), "duplicate name");
		}
		final Account readyAccount = isEmpty(store.list()) ? AlwaysDefault.of(account) : account;

		// TODO there must be one default account only

		final EmailAccount emailAccount = ACCOUNT_TO_EMAIL_ACCOUNT.apply(readyAccount);
		final Storable created = store.create(emailAccount);
		final EmailAccount createdEmailAccount = store.read(created);

		return EMAIL_ACCOUNT_TO_ACCOUNT.apply(createdEmailAccount);
	}

	@Override
	public Account updateAccount(final Account account) {
		Validate.isTrue(account.getId() != null, "cannot update an account with a missing id");
		for (final EmailAccount element : store.list()) {
			Validate.isTrue(!element.getName().equals(account.getName()), "duplicate name");
		}

		// TODO there must be one default account only

		final EmailAccount emailAccount = ACCOUNT_TO_EMAIL_ACCOUNT.apply(account);
		store.update(emailAccount);

		return account;
	}

	@Override
	public Iterable<Account> getAllAccounts() {
		final List<EmailAccount> elements = store.list();

		return from(elements) //
				.transform(EMAIL_ACCOUNT_TO_ACCOUNT);
	}

	@Override
	public Account getAccount(final Long id) {
		final Storable storable = storableById(id);
		final EmailAccount readed = store.read(storable);

		return EMAIL_ACCOUNT_TO_ACCOUNT.apply(readed);
	}

	@Override
	public void deleteAccount(final Long id) {
		final Storable storable = storableById(id);
		store.delete(storable);
	}

}

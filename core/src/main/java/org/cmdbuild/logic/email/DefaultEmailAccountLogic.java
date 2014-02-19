package org.cmdbuild.logic.email;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;

public class DefaultEmailAccountLogic implements EmailAccountLogic {

	private static final Marker marker = MarkerFactory.getMarker(DefaultEmailAccountLogic.class.getName());

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

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class AccountWrapper implements Account {

		private final EmailAccount delegate;

		public AccountWrapper(final EmailAccount delegate) {
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

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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

	private static Function<? super EmailAccount, String> TO_NAME = new Function<EmailAccount, String>() {

		@Override
		public String apply(final EmailAccount input) {
			return input.getName();
		}

	};

	private final Store<org.cmdbuild.data.store.email.EmailAccount> store;

	public DefaultEmailAccountLogic( //
			final Store<org.cmdbuild.data.store.email.EmailAccount> store //
	) {
		this.store = store;
	}

	@Override
	public void create(final Account account) {
		logger.info(marker, "creating account '{}'", account);
		final Iterable<EmailAccount> elements = store.list();
		final boolean existing = from(elements) //
				.transform(TO_NAME) //
				.contains(account.getName());
		Validate.isTrue(!existing, "already existing element");
		final Account readyAccount = isEmpty(elements) ? AlwaysDefault.of(account) : account;

		// TODO there must be one default account only

		final EmailAccount emailAccount = ACCOUNT_TO_EMAIL_ACCOUNT.apply(readyAccount);
		store.create(emailAccount);
	}

	@Override
	public void update(final Account account) {
		logger.info(marker, "updating account '{}'", account);
		final int count = from(store.list()) //
				.transform(TO_NAME) //
				.size();
		Validate.isTrue(!(count == 0), "element not found");
		Validate.isTrue(!(count > 1), "multiple elements found");
		store.update(ACCOUNT_TO_EMAIL_ACCOUNT.apply(account));
	}

	@Override
	public Iterable<Account> getAll() {
		logger.info(marker, "getting all accounts");
		final List<EmailAccount> elements = store.list();

		return from(elements) //
				.transform(EMAIL_ACCOUNT_TO_ACCOUNT);
	}

	@Override
	public Account getAccount(final String name) {
		logger.info(marker, "getting account '{}'", name);
		final int count = from(store.list()) //
				.transform(TO_NAME) //
				.size();
		Validate.isTrue(!(count == 0), "element not found");
		Validate.isTrue(!(count > 1), "multiple elements found");
		final EmailAccount account = EmailAccount.newInstance() //
				.withName(name) //
				.build();
		final EmailAccount readed = store.read(account);
		return EMAIL_ACCOUNT_TO_ACCOUNT.apply(readed);
	}

	@Override
	public void delete(final String name) {
		logger.info(marker, "deleting account '{}'", name);
		final int count = from(store.list()) //
				.transform(TO_NAME) //
				.size();
		Validate.isTrue(!(count == 0), "element not found");
		Validate.isTrue(!(count > 1), "multiple elements found");
		final EmailAccount account = EmailAccount.newInstance() //
				.withName(name) //
				.build();
		store.delete(account);
	}

}

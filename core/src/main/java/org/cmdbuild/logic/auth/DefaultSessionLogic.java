package org.cmdbuild.logic.auth;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.auth.UserStores.inMemory;
import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.AnonymousWhenMissingUserStore;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.ForwardingUserStore;
import org.cmdbuild.auth.TokenGenerator;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.session.Session;

public class DefaultSessionLogic extends ForwardingAuthenticationLogic implements SessionLogic {

	private static class SessionImpl implements Session {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<SessionImpl> {

			private String identifier;
			private OperationUser user;
			private OperationUser impersonated;

			/**
			 * Use factory method.
			 */
			private Builder() {
			}

			@Override
			public SessionImpl build() {
				validate();
				return new SessionImpl(this);
			}

			private void validate() {
				Validate.notBlank(identifier, "invalid identifier '%s'", identifier);
			}

			public Builder withIdentifier(final String identifier) {
				this.identifier = identifier;
				return this;
			}

			public Builder withUser(final OperationUser user) {
				this.user = user;
				return this;
			}

			public Builder withImpersonated(final OperationUser impersonated) {
				this.impersonated = impersonated;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final String identifier;
		private final OperationUser user;
		private final OperationUser impersonated;

		public SessionImpl(final Builder builder) {
			this.identifier = builder.identifier;
			this.user = builder.user;
			this.impersonated = builder.impersonated;
		}

		@Override
		public String getIdentifier() {
			return identifier;
		}

		@Override
		public OperationUser getUser() {
			return user;
		}

		@Override
		public OperationUser getImpersonated() {
			return impersonated;
		}

	}

	private static final OperationUser ANONYMOUS = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(),
			new NullGroup());

	private static final ThreadLocal<String> current = new ThreadLocal<>();

	private final AuthenticationLogic delegate;
	private final UserStore userStore;
	private final Store<Session> sessionStore;
	private final TokenGenerator tokenGenerator;

	public DefaultSessionLogic(final AuthenticationLogic delegate, final UserStore userStore,
			final Store<Session> sessionStore, final TokenGenerator tokenGenerator) {
		this.delegate = delegate;
		this.userStore = userStore;
		this.sessionStore = sessionStore;
		this.tokenGenerator = tokenGenerator;
	}

	@Override
	protected AuthenticationLogic delegate() {
		return delegate;
	}

	@Override
	public String create(final LoginDTO login) {
		final AtomicReference<String> output = new AtomicReference<>();
		delegate().login(login, new ForwardingUserStore() {

			private final UserStore delegate = new AnonymousWhenMissingUserStore(inMemory());

			@Override
			protected UserStore delegate() {
				return delegate;
			}

			@Override
			public void setUser(final OperationUser user) {
				final String identifier = tokenGenerator.generate(user.getAuthenticatedUser().getUsername());
				sessionStore.create(SessionImpl.newInstance() //
						.withIdentifier(identifier) //
						.withUser(user) //
						.build());
				output.set(identifier);
				super.setUser(user);
			}

		});
		return output.get();
	}

	@Override
	public ClientAuthenticationResponse create(final ClientRequest request, final Callback callback) {
		return delegate().login(request, new ForwardingUserStore() {

			private final UserStore delegate = new AnonymousWhenMissingUserStore(inMemory());

			@Override
			protected UserStore delegate() {
				return delegate;
			}

			@Override
			public void setUser(final OperationUser user) {
				final String identifier = tokenGenerator.generate(user.getAuthenticatedUser().getUsername());
				sessionStore.create(SessionImpl.newInstance() //
						.withIdentifier(identifier) //
						.withUser(user) //
						.build());
				callback.sessionCreated(identifier);
				super.setUser(user);
			}

		});

	}

	@Override
	public boolean exists(final String id) {
		return getUserOrAnonymousIfMissing(id) != null;
	}

	@Override
	public void update(final String id, final LoginDTO login) {
		final Session existing = sessionStore.read(SessionImpl.newInstance() //
				.withIdentifier(id) //
				.build());
		final UserStore temporary = inMemory(existing.getUser());
		delegate().login(login, new ForwardingUserStore() {

			@Override
			protected UserStore delegate() {
				return temporary;
			}

			@Override
			public void setUser(final OperationUser user) {
				sessionStore.update(SessionImpl.newInstance() //
						.withIdentifier(existing.getIdentifier()) //
						.withUser(user) //
						.build());
				super.setUser(user);
			}

		});
	}

	@Override
	public void delete(final String id) {
		sessionStore.delete(SessionImpl.newInstance() //
				.withIdentifier(id) //
				.build());
	}

	@Override
	public void impersonate(final String id, final String username) {
		final Session existing = sessionStore.read(SessionImpl.newInstance() //
				.withIdentifier(id) //
				.build());
		final OperationUser current = existing //
				.getUser();
		if (username != null) {
			if (!current.hasAdministratorPrivileges() && !current.getAuthenticatedUser().isService()
					&& !current.getAuthenticatedUser().isPrivileged()) {
				throw new IllegalStateException("cannot impersonate from current user");
			}
			final UserStore temporary = inMemory(ANONYMOUS);
			login(LoginDTO.newInstance() //
					.withLoginString(username) //
					.withNoPasswordRequired() //
					.build(), temporary);
			sessionStore.update(SessionImpl.newInstance() //
					.withIdentifier(existing.getIdentifier()) //
					.withUser(current) //
					.withImpersonated(temporary.getUser()) //
					.build());
		} else {
			sessionStore.update(SessionImpl.newInstance() //
					.withIdentifier(existing.getIdentifier()) //
					.withUser(current) //
					.withImpersonated(null) //
					.build());
		}
	}

	@Override
	public String getCurrent() {
		return current.get();
	}

	@Override
	public void setCurrent(final String id) {
		current.set(id);
		userStore.setUser((id == null) ? null : getUserOrAnonymousIfMissing(id));
	}

	private OperationUser getUserOrAnonymousIfMissing(final String id) {
		try {
			return getUser(id);
		} catch (final NoSuchElementException e) {
			return ANONYMOUS;
		}
	}

	@Override
	public boolean isValidUser(final String id) {
		return (id == null) ? false : getUserOrAnonymousIfMissing(id).isValid();
	}

	@Override
	public OperationUser getUser(final String id) {
		final Session existing = sessionStore.read(SessionImpl.newInstance() //
				.withIdentifier(id) //
				.build());
		return defaultIfNull(existing.getImpersonated(), existing.getUser());
	}

}

package org.cmdbuild.logic.auth;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.auth.UserStores.inMemory;
import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;

import org.cmdbuild.auth.AnonymousWhenMissingUserStore;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.ForwardingUserStore;
import org.cmdbuild.auth.TokenGenerator;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.session.Session;
import org.cmdbuild.data.store.session.SessionStore;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class DefaultSessionLogic extends ForwardingAuthenticationLogic implements SessionLogic {

	private static final OperationUser ANONYMOUS = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(),
			new NullGroup());

	private static final ThreadLocal<String> current = new ThreadLocal<>();

	private final AuthenticationLogic delegate;
	private final UserStore userStore;
	private final SessionStore sessionStore;
	private final TokenGenerator tokenGenerator;
	private final Cache<String, OperationUser> cache;
	private final Cache<String, OperationUser> impersonatedCache;

	public DefaultSessionLogic(final AuthenticationLogic delegate, final UserStore userStore,
			final SessionStore sessionStore, final TokenGenerator tokenGenerator,
			final CmdbuildConfiguration configuration) {
		this.delegate = delegate;
		this.userStore = userStore;
		this.sessionStore = sessionStore;
		this.tokenGenerator = tokenGenerator;
		// TODO needs reboot, we should avoid it
		this.cache = this.impersonatedCache = CacheBuilder.newBuilder() //
				.expireAfterAccess((configuration.getSessionTimoutOrZero() == 0) ? Long.MAX_VALUE
						: configuration.getSessionTimoutOrZero(), SECONDS) //
				.build();
	}

	@Override
	protected AuthenticationLogic delegate() {
		return delegate;
	}

	private Session createSession(final String value) {
		return new Session() {

			private final String identifier = tokenGenerator.generate(value);

			@Override
			public String getIdentifier() {
				return identifier;
			}

		};
	}

	@Override
	public String create(final LoginDTO login) {
		final Session session = createSession(login.getLoginString());
		delegate().login(login, new ForwardingUserStore() {

			private final UserStore delegate = new AnonymousWhenMissingUserStore(inMemory());

			@Override
			protected UserStore delegate() {
				return delegate;
			}

			@Override
			public void setUser(final OperationUser user) {
				final Storable created = sessionStore.create(session);
				final Session stored = sessionStore.read(created);
				sessionStore.set(stored, user);
				cache.put(session.getIdentifier(), user);
				super.setUser(user);
			}

		});
		return session.getIdentifier();
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
				final Session session = createSession(user.getAuthenticatedUser().getUsername());
				final Storable created = sessionStore.create(session);
				final Session stored = sessionStore.read(created);
				sessionStore.set(stored, user);
				cache.put(session.getIdentifier(), user);
				callback.sessionCreated(created.getIdentifier());
				super.setUser(user);
			}

		});

	}

	@Override
	public boolean exists(final String id) {
		return getUser(id) != null;
	}

	@Override
	public void update(final String id, final LoginDTO login) {
		final UserStore temporary = inMemory(cache.getIfPresent(id));
		delegate().login(login, new ForwardingUserStore() {

			@Override
			protected UserStore delegate() {
				return temporary;
			}

			@Override
			public void setUser(final OperationUser user) {
				final String identifier = tokenGenerator.generate(user.getAuthenticatedUser().getUsername());
				final Storable created = sessionStore.create(new Session() {

					@Override
					public String getIdentifier() {
						return identifier;
					}

				});
				final Session session = sessionStore.read(created);
				sessionStore.set(session, user);
				cache.put(identifier, user);
				super.setUser(user);
			}

		});
	}

	@Override
	public void delete(final String id) {
		cache.invalidate(id);
		impersonatedCache.invalidate(id);
	}

	@Override
	public void impersonate(final String id, final String username) {
		if (username != null) {
			final OperationUser current = cache.getIfPresent(id);
			if (!current.hasAdministratorPrivileges() && !current.getAuthenticatedUser().isService()
					&& !current.getAuthenticatedUser().isPrivileged()) {
				throw new IllegalStateException("cannot impersonate from current user");
			}
			final UserStore temporary = inMemory(ANONYMOUS);
			login(LoginDTO.newInstance() //
					.withLoginString(username) //
					.withNoPasswordRequired() //
					.build(), temporary);
			impersonatedCache.put(id, temporary.getUser());
		} else {
			impersonatedCache.invalidate(id);
		}
	}

	@Override
	public String getCurrent() {
		return current.get();
	}

	@Override
	public void setCurrent(final String id) {
		current.set(id);
		userStore.setUser((id == null) ? null : getUser(id));
	}

	@Override
	public boolean isValidUser(final String id) {
		return (id == null) ? false : getUser(id).isValid();
	}

	@Override
	public OperationUser getUser(final String id) {
		return defaultIfNull(impersonatedCache.getIfPresent(id), cache.getIfPresent(id));
	}

}

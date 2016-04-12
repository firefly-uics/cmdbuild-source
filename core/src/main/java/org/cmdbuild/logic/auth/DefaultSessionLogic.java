package org.cmdbuild.logic.auth;

import static java.lang.Integer.valueOf;

import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.ForwardingUserStore;
import org.cmdbuild.auth.TokenGenerator;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.session.Session;
import org.cmdbuild.data.store.session.SessionStore;

public class DefaultSessionLogic extends ForwardingAuthenticationLogic implements SessionLogic {

	private final AuthenticationLogic delegate;
	private final UserStore userStore;
	private final SessionStore sessionStore;
	private final TokenGenerator tokenGenerator;

	public DefaultSessionLogic(final AuthenticationLogic delegate, final UserStore userStore,
			final SessionStore sessionStore, final TokenGenerator tokenGenerator) {
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
	public boolean isValidUser() {
		return userStore.getUser().isValid();
	}

	@Override
	public ClientAuthenticationResponse login(final ClientRequest request) {
		return delegate().login(request, new ForwardingUserStore() {

			@Override
			protected UserStore delegate() {
				return userStore;
			}

			@Override
			public void setUser(final OperationUser user) {
				final Storable created = sessionStore.create(new Session() {

					private final String identifier;

					{
						// TODO do it better
						identifier = tokenGenerator.generate(valueOf(hashCode()).toString());
					}

					@Override
					public String getIdentifier() {
						return identifier;
					}

				});
				final Session session = sessionStore.read(created);
				sessionStore.set(session, user);
				super.setUser(user);
			}

		});
	}

}

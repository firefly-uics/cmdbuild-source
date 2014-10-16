package org.cmdbuild.service.rest.cxf;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;
import static org.cmdbuild.service.rest.model.Builders.newSession;

import org.cmdbuild.service.rest.Sessions;
import org.cmdbuild.service.rest.cxf.service.TokenGenerator;
import org.cmdbuild.service.rest.cxf.service.TokenStore;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.service.rest.model.Session;

import com.google.common.base.Optional;

public class CxfSessions implements Sessions {

	private final ErrorHandler errorHandler;
	private final TokenGenerator tokenGenerator;
	private final TokenStore tokenStore;

	public CxfSessions(final ErrorHandler errorHandler, final TokenGenerator tokenGenerator, final TokenStore tokenStore) {
		this.errorHandler = errorHandler;
		this.tokenGenerator = tokenGenerator;
		this.tokenStore = tokenStore;
	}

	@Override
	public ResponseSingle<String> create(final Session session) {
		if (isBlank(session.getUsername())) {
			errorHandler.missingUsername();
		}
		if (isBlank(session.getPassword())) {
			errorHandler.missingPassword();
		}
		final String token = tokenGenerator.generate(session.getUsername());
		tokenStore.put(token, newSession(session) //
				.withId(token) //
				.build());
		return newResponseSingle(String.class) //
				.withElement(token) //
				.build();
	}

	@Override
	public ResponseSingle<Session> read(final String id) {
		final Optional<Session> credentials = tokenStore.get(id);
		if (!credentials.isPresent()) {
			errorHandler.tokenNotFound(id);
		}
		return newResponseSingle(Session.class) //
				.withElement(newSession(credentials.get()) //
						.withPassword(null) //
						.build()) //
				.build();
	}

	@Override
	public void update(final String id, final Session session) {
		final Optional<Session> oldCredentials = tokenStore.get(id);
		if (!oldCredentials.isPresent()) {
			errorHandler.tokenNotFound(id);
		}
		tokenStore.put(id, newSession(oldCredentials.get()) //
				.withGroup(session.getGroup()) //
				.build());
	}

	@Override
	public void delete(final String id) {
		final Optional<Session> credentials = tokenStore.get(id);
		if (!credentials.isPresent()) {
			errorHandler.tokenNotFound(id);
		}
		tokenStore.remove(id);
	}

	@Override
	public ResponseMultiple<String> readGroups(final String id) {
		// TODO Auto-generated method stub
		return null;
	}

}

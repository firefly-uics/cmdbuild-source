package org.cmdbuild.service.rest.cxf;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.service.rest.model.Builders.newCredentials;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;

import org.cmdbuild.service.rest.Tokens;
import org.cmdbuild.service.rest.cxf.service.TokenGenerator;
import org.cmdbuild.service.rest.cxf.service.TokenStore;
import org.cmdbuild.service.rest.model.Credentials;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.base.Optional;

public class CxfTokens implements Tokens {

	private final ErrorHandler errorHandler;
	private final TokenGenerator tokenGenerator;
	private final TokenStore tokenStore;

	public CxfTokens(final ErrorHandler errorHandler, final TokenGenerator tokenGenerator, final TokenStore tokenStore) {
		this.errorHandler = errorHandler;
		this.tokenGenerator = tokenGenerator;
		this.tokenStore = tokenStore;
	}

	@Override
	public ResponseSingle<String> create(final Credentials credentials) {
		if (isBlank(credentials.getUsername())) {
			errorHandler.missingUsername();
		}
		if (isBlank(credentials.getPassword())) {
			errorHandler.missingPassword();
		}
		final String token = tokenGenerator.generate(credentials.getUsername());
		tokenStore.put(token, newCredentials(credentials) //
				.withToken(token) //
				.build());
		return newResponseSingle(String.class) //
				.withElement(token) //
				.build();
	}

	@Override
	public ResponseSingle<Credentials> read(final String token) {
		final Optional<Credentials> credentials = tokenStore.get(token);
		if (!credentials.isPresent()) {
			errorHandler.tokenNotFound(token);
		}
		return newResponseSingle(Credentials.class) //
				.withElement(newCredentials(credentials.get()) //
						.withPassword(null) //
						.build()) //
				.build();
	}

	@Override
	public void update(final String token, final Credentials credentials) {
		final Optional<Credentials> oldCredentials = tokenStore.get(token);
		if (!oldCredentials.isPresent()) {
			errorHandler.tokenNotFound(token);
		}
		tokenStore.put(token, newCredentials(oldCredentials.get()) //
				.withGroup(credentials.getGroup()) //
				.build());
	}

	@Override
	public void delete(final String token) {
		final Optional<Credentials> credentials = tokenStore.get(token);
		if (!credentials.isPresent()) {
			errorHandler.tokenNotFound(token);
		}
		tokenStore.remove(token);
	}

	@Override
	public ResponseMultiple<String> readGroups(final String token) {
		// TODO Auto-generated method stub
		return null;
	}

}
